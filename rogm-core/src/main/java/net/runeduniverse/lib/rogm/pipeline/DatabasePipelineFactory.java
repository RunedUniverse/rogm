/*
 * Copyright © 2022 Pl4yingNight (pl4yingnight@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.lib.rogm.pipeline;

import net.runeduniverse.lib.rogm.Configuration;
import net.runeduniverse.lib.rogm.buffer.IBuffer;
import net.runeduniverse.lib.rogm.errors.ScannerException;
import net.runeduniverse.lib.rogm.info.SessionInfo;
import net.runeduniverse.lib.rogm.lang.Language;
import net.runeduniverse.lib.rogm.modules.Module;
import net.runeduniverse.lib.rogm.modules.PassiveModule;
import net.runeduniverse.lib.rogm.parser.Parser;
import net.runeduniverse.lib.rogm.pattern.Archive;
import net.runeduniverse.lib.rogm.pipeline.chain.AssemblyLayers;
import net.runeduniverse.lib.rogm.pipeline.chain.LookupLayers;
import net.runeduniverse.lib.rogm.pipeline.chain.ReduceLayer;
import net.runeduniverse.lib.utils.chain.ChainManager;
import net.runeduniverse.lib.utils.logging.UniversalLogger;

public class DatabasePipelineFactory extends APipelineFactory<DatabaseChainRouter> {

	protected final Configuration cnf;
	protected final IBuffer buffer;

	protected final Parser parser;
	protected final Language lang;
	protected final Module module;

	protected final Parser.Instance parserInstance;
	protected final Language.Instance langInstance;
	protected final Module.Instance<?> moduleInstance;

	public DatabasePipelineFactory(Configuration config) {
		this(config, new UniversalLogger(DatabasePipelineFactory.class, config.getLogger()));
	}

	public DatabasePipelineFactory(Configuration config, UniversalLogger logger) {
		super(config.getPackageInfo(), config.getModule(), new DatabaseChainRouter(logger), logger);
		this.cnf = config;

		this.buffer = this.cnf.getBuffer();
		this.parser = this.cnf.getParser();
		this.lang = this.cnf.getLang();
		this.module = this.cnf.getModule();

		this.parserInstance = this.parser.build(this.logger, this.module);
		this.langInstance = this.lang.build(this.logger, this.module, this.parserInstance);
		this.moduleInstance = this.module.build(this.logger, this.parserInstance);

		this.router.initialize(this.buffer, this.lang, this.parserInstance, this.langInstance, this.moduleInstance);
	}

	// SETUP / CONNECTION

	@Override
	protected void setupCallOrder() throws Exception {
		super.setupCallOrder();

		if (!this.moduleInstance.connect(this.cnf.getConnectionInfo()))
			this.logger.warning("Failed to establish initial database-connection!");
	}

	@Override
	protected void setupArchive(Archive archive) throws ScannerException {
		for (PassiveModule module : this.cnf.getPassiveModules())
			module.configure(archive);
	}

	@Override
	protected void setupChainManager(ChainManager chainManager) throws Exception {
		chainManager.addChainLayers(AssemblyLayers.class);
		chainManager.addChainLayers(LookupLayers.class);
		chainManager.addChainLayers(ReduceLayer.class);
		this.lang.setupChainManager(chainManager);
		this.buffer.setupChainManager(chainManager);
	}

	@Override
	public boolean isConnected() {
		return this.moduleInstance.isConnected();
	}

	@Override
	public void closeConnections() {
		this.moduleInstance.disconnect();
	}

	// GETTER

	@Override
	public SessionInfo getSessionInfo() {
		return new SessionInfo(DatabasePipelineFactory.class, this.cnf.getBuffer()
				.getClass(), this.cnf.getPackageInfo(), this.cnf.getConnectionInfo());
	}

}
