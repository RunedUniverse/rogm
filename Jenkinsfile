pipeline {
	agent any
	tools {
		maven 'maven-latest'
	}
	environment {
		PATH = """${sh(
				returnStdout: true,
				script: 'chmod +x $WORKSPACE/.build/*; printf $WORKSPACE/.build:$PATH'
			)}"""

		GLOBAL_MAVEN_SETTINGS = """${sh(
				returnStdout: true,
				script: 'printf /srv/jenkins/.m2/global-settings.xml'
			)}"""
		MAVEN_SETTINGS = """${sh(
				returnStdout: true,
				script: 'printf $WORKSPACE/.mvn/settings.xml'
			)}"""
		MAVEN_TOOLCHAINS = """${sh(
				returnStdout: true,
				script: 'printf $WORKSPACE/.mvn/toolchains.xml'
			)}"""
		REPOS = """${sh(
				returnStdout: true,
				script: 'REPOS=repo-releases; if [ $GIT_BRANCH != master ]; then REPOS=$REPOS,repo-development; fi; printf $REPOS'
			)}"""
	}
	stages {
		stage('Initialize') {
			steps {
				sh 'echo "PATH = ${PATH}"'
				sh 'echo "M2_HOME = ${M2_HOME}"'
				sh 'mvn-dev -P ${REPOS},install --non-recursive'
				script {
					env.CHANGES_ROGM_PARENT = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-parent .'
					)
					env.CHANGES_ROGM_BOM = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-bom rogm-bom'
					)
					env.CHANGES_ROGM_SOURCES_BOM = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-sources-bom rogm-sources-bom'
					)
					env.CHANGES_ROGM_CORE = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-core rogm-core'
					)
					env.CHANGES_ROGM_PARSER_JSON = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-parser-json rogm-parser-json'
					)
					env.CHANGES_ROGM_LANG_CYPHER = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-lang-cypher rogm-lang-cypher'
					)
					env.CHANGES_ROGM_MODULE_NEO4J = sh(
						returnStdout: true,
						script: 'git-check-version-tag rogm-module-neo4j rogm-module-neo4j'
					)
					//env.CHANGES_ROGM_MODULE_DECORATOR = sh(
					//		returnStdout: true,
					//		script: 'git-check-version-tag rogm-module-decorator rogm-module-decorator'
					//)
					env.RESULT_PATH = "${WORKSPACE}/target/result/"
					env.ARCHIVE_PATH = "${WORKSPACE}/target/archive/"
				}
				sh 'printenv | sort'
				sh 'mkdir -p ${RESULT_PATH}'
				sh 'mkdir -p ${ARCHIVE_PATH}'
			}
		}
		stage('Update Maven Repo') {
			when {
				anyOf {
					environment name: 'CHANGES_ROGM_PARENT', value: '1'
					environment name: 'CHANGES_ROGM_BOM', value: '1'
					environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
					environment name: 'CHANGES_ROGM_CORE', value: '1'
					environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
				}
			}
			steps {
				sh 'mvn-dev -P ${REPOS} dependency:purge-local-repository -DactTransitively=false -DreResolve=false --non-recursive'
				sh 'mvn-dev -P ${REPOS} dependency:resolve --non-recursive'
			}
		}
		stage('Install ROGM Parent') {
			when {
				environment name: 'CHANGES_ROGM_PARENT', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
				sh 'ls -l target/'
			}
			post {
				always {
					dir(path: 'target') {
						sh 'cp *.pom *.asc ${RESULT_PATH}'
					}
				}
			}
		}
		stage('Install Bill of Sources') {
			when {
				environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS} dependency:resolve -pl rogm-sources-bom'
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-sources-bom'
			}
			post {
				always {
					dir(path: 'rogm-sources-bom/target') {
						sh 'ls -l'
						sh 'cp *.pom *.asc ${RESULT_PATH}'
					}
				}
			}
		}
		stage('Install Bill of Materials') {
			when {
				environment name: 'CHANGES_ROGM_BOM', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-bom'
			}
			post {
				always {
					dir(path: 'rogm-bom/target') {
						sh 'ls -l'
						sh 'cp *.pom *.asc ${RESULT_PATH}'
					}
				}
			}
		}
		stage('Build CORE') {
			when {
				environment name: 'CHANGES_ROGM_CORE', value: '1'
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-core'
			}
			post {
				always {
					dir(path: 'rogm-core/target') {
						sh 'ls -l'
						sh 'cp *.pom *.jar *.asc ${RESULT_PATH}'
					}
				}
			}
		}
		stage('Build Parser') {
			parallel {
				stage('JSON') {
					when {
						environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					}
					steps {
						sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-parser-json'
					}
					post {
						always {
							dir(path: 'rogm-parser-json/target') {
								sh 'ls -l'
								sh 'cp *.pom *.jar *.asc ${RESULT_PATH}'
							}
						}
					}
				}
			}
		}
		stage('Build Languages') {
			parallel {
				stage('Cypher') {
					when {
						environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					}
					steps {
						sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-lang-cypher'
					}
					post {
						always {
							dir(path: 'rogm-lang-cypher/target') {
								sh 'ls -l'
								sh 'cp *.pom *.jar *.asc ${RESULT_PATH}'
							}
						}
					}
				}
			}
		}
		stage('Build Module') {
			parallel {
				stage('Neo4J') {
					when {
						environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					}
					steps {
						sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-module-neo4j'
					}
					post {
						always {
							dir(path: 'rogm-module-neo4j/target') {
								sh 'ls -l'
								sh 'cp *.pom *.jar *.asc ${RESULT_PATH}'
							}
						}
					}
				}
				//stage('Decorator') {
				//	when {
				//		environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
				//	}
				//	steps {
				//		sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,install -pl rogm-module-decorator'
				//	}
				//	post {
				//		always {
				//			dir(path: 'rogm-module-decorator/target') {
				//				sh 'ls -l'
				//				sh 'cp *.pom *.jar *.asc ${RESULT_PATH}'
				//			}
				//		}
				//	}
				//}
			}
		}

		stage('Code Validation') {
			when {
				anyOf {
					environment name: 'CHANGES_ROGM_PARENT', value: '1'
					environment name: 'CHANGES_ROGM_BOM', value: '1'
					environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
					environment name: 'CHANGES_ROGM_CORE', value: '1'
					environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
				}
			}
			steps {
				sh 'mvn-dev -P ${REPOS},validate,license-prj-utils-approve,license-apache2-approve'
			}
		}

		stage('Package Build Result') {
			when {
				anyOf {
					environment name: 'CHANGES_ROGM_PARENT', value: '1'
					environment name: 'CHANGES_ROGM_BOM', value: '1'
					environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
					environment name: 'CHANGES_ROGM_CORE', value: '1'
					environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
				}
			}
			steps {
				dir(path: "${env.RESULT_PATH}") {
					sh 'ls -l'
					sh 'tar -I "pxz -9" -cvf ${ARCHIVE_PATH}rogm.tar.xz *'
					sh 'zip -9 ${ARCHIVE_PATH}rogm.zip *'
				}
			}
			post {
				always {
					dir(path: "${env.RESULT_PATH}") {
						archiveArtifacts artifacts: '*', fingerprint: true
					}
					dir(path: "${env.ARCHIVE_PATH}") {
						archiveArtifacts artifacts: '*', fingerprint: true
					}
				}
			}
		}

		stage('System Test') {
			when {
				anyOf {
					environment name: 'CHANGES_ROGM_PARENT', value: '1'
					environment name: 'CHANGES_ROGM_BOM', value: '1'
					environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
					environment name: 'CHANGES_ROGM_CORE', value: '1'
					environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
				}
			}
			steps {
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,build-tests'
				sh 'mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,test-junit-jupiter,test-system'
			}
			post {
				success {
					junit '*/target/surefire-reports/*.xml'
				}
				failure {
					junit '*/target/surefire-reports/*.xml'
					archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
				}
			}
		}
		stage('Database Test') {
			parallel {
			
				stage('Neo4J') {
					when {
						anyOf {
							environment name: 'CHANGES_ROGM_PARENT', value: '1'
							environment name: 'CHANGES_ROGM_BOM', value: '1'
							environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
							environment name: 'CHANGES_ROGM_CORE', value: '1'
							environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
							environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
							environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
							environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
						}
					}
					steps{
						script {
							docker.image('docker.io/library/neo4j:4.4').withRun(
									'--rm --network podman ' +
									'--volume=${WORKSPACE}/src/test/resources/neo4j/conf:/var/lib/neo4j/conf:z ' +
									'--volume=${WORKSPACE}/src/test/resources/neo4j/setup/:/var/lib/neo4j/setup/:z '
								) { c ->
								script {
									def dbIp = sh(
										returnStdout: true,
										script: ("docker container inspect -f \"{{.NetworkSettings.IPAddress}}\" ${c.id} 2> cat")
									)
									echo "Neo4j started with IP: ${dbIp}"

									/* Wait until database service is up */
									echo 'waiting for Neo4J to start'
									sh "until \$(./src/test/resources/neo4j/toolkit/db-ping-web.sh ${dbIp}); do sleep 5; done"

									/* Prepare Database */
									echo 'Neo4J online > setting up database'
									sh "docker exec ${c.id} cypher-shell -u neo4j -p neo4j -f \"/var/lib/neo4j/setup/setup.cypher\""

									/* Run tests */
									echo 'database loaded > starting tests'
									sh 'printenv | sort'
									sh "mvn-dev -P ${REPOS},toolchain-openjdk-1-8-0,test-junit-jupiter,test-db-neo4j -Ddbuser=neo4j -Ddbpw=neo4j -Ddbhost=${dbIp}"
								}
							}
						}
					}
				}

			}
			post {
				success {
					junit '*/target/surefire-reports/*.xml'
				}
				failure {
					junit '*/target/surefire-reports/*.xml'
					archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
				}
			}
		}

		stage('Deploy') {
			parallel {
				stage('Develop') {
					stages {
						stage('rogm-parent') {
							when {
								environment name: 'CHANGES_ROGM_PARENT', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy --non-recursive'
							}
						}
						stage('rogm-bom') {
							when {
								environment name: 'CHANGES_ROGM_BOM', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-bom'
							}
						}
						stage('rogm-sources-bom') {
							when {
								environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-sources-bom'
							}
						}
						stage('rogm-core') {
							when {
								environment name: 'CHANGES_ROGM_CORE', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-core'
							}
						}
						stage('rogm-parser-json') {
							when {
								environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-parser-json'
							}
						}
						stage('rogm-lang-cypher') {
							when {
								environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-lang-cypher'
							}
						}
						stage('rogm-module-neo4j') {
							when {
								environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-module-neo4j'
							}
						}
						stage('rogm-module-decorator') {
							when {
								environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-development,deploy -pl rogm-module-decorator'
							}
						}
					}
				}

				stage('Release') {
					when {
						branch 'master'
					}
					stages {
						stage('rogm-parent') {
							when {
								environment name: 'CHANGES_ROGM_PARENT', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-pom-signed --non-recursive'
							}
						}
						stage('rogm-bom') {
							when {
								environment name: 'CHANGES_ROGM_BOM', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-pom-signed -pl rogm-bom'
							}
						}
						stage('rogm-sources-bom') {
							when {
								environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-pom-signed -pl rogm-sources-bom'
							}
						}
						stage('rogm-core') {
							when {
								environment name: 'CHANGES_ROGM_CORE', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl rogm-core'
							}
						}
						stage('rogm-parser-json') {
							when {
								environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl rogm-parser-json'
							}
						}
						stage('rogm-lang-cypher') {
							when {
								environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl rogm-lang-cypher'
							}
						}
						stage('rogm-module-neo4j') {
							when {
								environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl rogm-module-neo4j'
							}
						}
						stage('rogm-module-decorator') {
							when {
								environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
							}
							steps {
								sh 'mvn-dev -P ${REPOS},dist-repo-releases,deploy-signed -pl rogm-module-decorator'
							}
						}
					}
				}
			}
		}

		stage('Stage at Maven-Central') {
			when {
				branch 'master'
			}
			stages {
				// never add : -P ${REPOS} => this is ment to fail here
				stage('rogm-parent') {
					when {
						environment name: 'CHANGES_ROGM_PARENT', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-pom-signed --non-recursive'
					}
				}
				stage('rogm-bom') {
					when {
						environment name: 'CHANGES_ROGM_BOM', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-pom-signed -pl rogm-bom'
					}
				}
				stage('rogm-sources-bom') {
					when {
						environment name: 'CHANGES_ROGM_SOURCES_BOM', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-pom-signed -pl rogm-sources-bom'
					}
				}
				stage('rogm-core') {
					when {
						environment name: 'CHANGES_ROGM_CORE', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl rogm-core'
					}
				}
				stage('rogm-parser-json') {
					when {
						environment name: 'CHANGES_ROGM_PARSER_JSON', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl rogm-parser-json'
					}
				}
				stage('rogm-lang-cypher') {
					when {
						environment name: 'CHANGES_ROGM_LANG_CYPHER', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl rogm-lang-cypher'
					}
				}
				stage('rogm-module-neo4j') {
					when {
						environment name: 'CHANGES_ROGM_MODULE_NEO4J', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl rogm-module-neo4j'
					}
				}
				stage('rogm-module-decorator') {
					when {
						environment name: 'CHANGES_ROGM_MODULE_DECORATOR', value: '1'
					}
					steps {
						sh 'mvn-dev -P repo-releases,dist-repo-maven-central,deploy-signed -pl rogm-module-decorator'
					}
				}
			}
		}
	}
	post {
		cleanup {
			cleanWs()
		}
	}
}
