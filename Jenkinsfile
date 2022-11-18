pipeline {
	agent any
	options {
		throttleJobProperty(
			categories: ['runeduniverse-rogm'],
			throttleEnabled: true,
			throttleOption: 'category'
		)
    }
	tools {
		maven 'maven-latest'
		jdk 'java-1.8.0'
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

		CHANGES_MVN_PARENT = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change pom.xml mvn-parent'
			)}"""
		CHANGES_ROGM_BOM = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-bom/pom.xml rogm-bom'
			)}"""
		CHANGES_ROGM_SOURCES_BOM = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-sources-bom/pom.xml rogm-sources-bom'
			)}"""
		CHANGES_ROGM_CORE = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-core/pom.xml rogm-core'
			)}"""
		CHANGES_ROGM_PARSER_JSON = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-parser-json/pom.xml rogm-parser-json'
			)}"""
		CHANGES_ROGM_LANG_CYPHER = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-lang-cypher/pom.xml rogm-lang-cypher'
			)}"""
		CHANGES_ROGM_MODULE_NEO4J = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-module-neo4j/pom.xml rogm-module-neo4j'
			)}"""
		CHANGES_ROGM_MODULE_DECORATOR = """${sh(
				returnStdout: true,
				script: '.build/git-check-for-change rogm-module-decorator/pom.xml rogm-module-decorator'
			)}"""
	}
	stages {
		stage('Initialize') {
			steps {
				sh 'echo "PATH = ${PATH}"'
				sh 'echo "M2_HOME = ${M2_HOME}"'
				sh 'printenv | sort'
			}
		}
		stage('Update Maven Repo') {
			steps {
				sh 'mvn -P ${REPOS} dependency:resolve --non-recursive'
				sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
				sh 'ls -l target'
			}
		}
		stage('Install Bill of Sources') {
			steps {
				dir(path: 'rogm-sources-bom') {
					sh 'mvn -P ${REPOS} dependency:resolve  --non-recursive'
					sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
					sh 'ls -l target'
				}
			}
		}
		stage('Install Bill of Materials') {
			steps {
				dir(path: 'rogm-bom') {
					sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
					sh 'ls -l target'
				}
			}
		}
		stage('Build CORE') {
			steps {
				dir(path: 'rogm-core') {
					sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
					sh 'ls -l target'
				}
			}
		}
		stage('Build Parser') {
			parallel {
				stage('JSON') {
					steps {
						dir(path: 'rogm-parser-json') {
							sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
							sh 'ls -l target'
						}
					}
				}
			}
		}
		stage('Build Languages') {
			parallel {
				stage('Cypher') {
					steps {
						dir(path: 'rogm-lang-cypher') {
							sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
							sh 'ls -l target'
						}
					}
				}
			}
		}
		stage('Build Module') {
			parallel {
				stage('Neo4J') {
					steps {
						dir(path: 'rogm-module-neo4j') {
							sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
							sh 'ls -l target'
						}
					}
				}
				//stage('Decorator') {
				//	steps {
				//		dir(path: 'rogm-module-decorator') {
				//			sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,install --non-recursive'
				//		}
				//	}
				//}
			}
		}

		stage('License Check') {
			steps {
				sh 'mvn -P ${REPOS},license-check,license-prj-utils-approve,license-apache2-approve'
			}
		}

		stage('System Test') {
			steps {
				sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,test-junit-jupiter,test-system'
			}
			post {
				always {
					junit '*/target/surefire-reports/*.xml'
				}
				failure {
					archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
				}
			}
		}
		stage('Database Test') {
			parallel {
			
				stage('Neo4J') {
					steps{
						script {
							docker.image('neo4j:latest').withRun(
									'-p 172.16.0.1:7474:7474 ' +
									'-p 172.16.0.1:7687:7687 ' +
									'--volume=${WORKSPACE}/src/test/resources/neo4j/conf:/var/lib/neo4j/conf:z ' +
									'--volume=/var/run/neo4j-jenkins-rogm:/run:z'
								) { c ->

								/* Wait until database service is up */
								sh 'echo waiting for Neo4J to start'
								sh 'until $(curl --output /dev/null --silent --head --fail http://172.16.0.1:7474); do sleep 5; done'

								docker.image('neo4j:latest').inside("--link ${c.id}:database") {
									/* Prepare Database */
										sh	'echo Neo4J online > setting up database'
										sh	'JAVA_HOME=/opt/java/openjdk cypher-shell -a "neo4j://database:7687" -u neo4j -p neo4j -f "./src/test/resources/neo4j/setup/setup.cypher"'
									}

								/* Run tests */
								sh 'echo database loaded > starting tests'
								sh 'printenv | sort'
								sh 'mvn -P ${REPOS},toolchain-openjdk-1-8-0,test-junit-jupiter,test-db-neo4j -Ddbhost=172.16.0.1 -Ddbuser=neo4j -Ddbpw=neo4j'
							}
						}
					}
				}

			}
			post {
				always {
					junit '*/target/surefire-reports/*.xml'
				}
				failure {
					archiveArtifacts artifacts: '*/target/surefire-reports/*.xml'
				}
			}
		}

		stage('Deploy') {
			parallel {
				stage('Development') {
					steps {
						sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						dir(path: 'rogm-sources-bom') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-bom') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-core') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-parser-json') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-lang-cypher') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-module-neo4j') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
						dir(path: 'rogm-module-decorator') {
							sh 'mvn -P ${REPOS},dist-repo-development,deploy --non-recursive'
						}
					}
				}
				stage('Release') {
					when {
						branch 'master'
					}
					steps {
						sh 'mvn -P ${REPOS},dist-repo-releases,deploy-pom-signed --non-recursive'
						dir(path: 'rogm-sources-bom') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-pom-signed --non-recursive'
						}
						dir(path: 'rogm-bom') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-pom-signed --non-recursive'
						}
						dir(path: 'rogm-core') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-signed --non-recursive'
						}
						dir(path: 'rogm-parser-json') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-signed --non-recursive'
						}
						dir(path: 'rogm-lang-cypher') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-signed --non-recursive'
						}
						dir(path: 'rogm-module-neo4j') {
							sh 'mvn -P ${REPOS},dist-repo-releases,deploy-signed --non-recursive'
						}
						//dir(path: 'rogm-module-decorator') {
						//	sh 'mvn -P ${REPOS},dist-repo-releases,deploy-signed --non-recursive'
						//}
					}
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*/target/*.pom', fingerprint: true
					archiveArtifacts artifacts: '*/target/*.jar', fingerprint: true
					archiveArtifacts artifacts: '*/target/*.asc', fingerprint: true
				}
			}
		}

		stage('Stage at Maven-Central') {
			when {
				branch 'master'
			}
			steps {
				// never add : -P ${REPOS} => this is ment to fail here
				sh 'mvn -P dist-repo-maven-central,deploy-pom-signed --non-recursive'
				dir(path: 'rogm-sources-bom') {
					sh 'mvn -P dist-repo-maven-central,deploy-pom-signed --non-recursive'
				}
				dir(path: 'rogm-bom') {
					sh 'mvn -P dist-repo-maven-central,deploy-pom-signed --non-recursive'
				}
				dir(path: 'rogm-core') {
					sh 'mvn -P dist-repo-maven-central,deploy-signed --non-recursive'
				}
				dir(path: 'rogm-parser-json') {
					sh 'mvn -P dist-repo-maven-central,deploy-signed --non-recursive'
				}
				dir(path: 'rogm-lang-cypher') {
					sh 'mvn -P dist-repo-maven-central,deploy-signed --non-recursive'
				}
				dir(path: 'rogm-module-neo4j') {
					sh 'mvn -P dist-repo-maven-central,deploy-signed --non-recursive'
				}
				//dir(path: 'rogm-module-decorator') {
				//	sh 'mvn -P dist-repo-maven-central,deploy-signed --non-recursive'
				//}
			}
		}
	}
	post {
		cleanup {
			cleanWs()
		}
	}
}
