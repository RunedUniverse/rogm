package net.runeduniverse.libs.rogm.pipeline;

import net.runeduniverse.libs.rogm.Configuration;
import net.runeduniverse.libs.rogm.error.ScannerException;
import net.runeduniverse.libs.rogm.info.SessionInfo;
import net.runeduniverse.libs.rogm.lang.Language;
import net.runeduniverse.libs.rogm.logging.UniversalLogger;
import net.runeduniverse.libs.rogm.modules.Module;
import net.runeduniverse.libs.rogm.modules.PassiveModule;
import net.runeduniverse.libs.rogm.parser.Parser;
import net.runeduniverse.libs.rogm.pattern.Archive;
import net.runeduniverse.libs.rogm.pipeline.chain.AssemblyLayers;
import net.runeduniverse.libs.rogm.pipeline.chain.BufferLayers;
import net.runeduniverse.libs.rogm.pipeline.chain.LookupLayers;
import net.runeduniverse.libs.rogm.pipeline.chain.ReduceLayer;
import net.runeduniverse.libs.rogm.pipeline.chain.sys.ChainManager;

public class DatabasePipelineFactory extends APipelineFactory<DatabaseChainRouter> {

	protected final Configuration cnf;
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
		super(config.getPackageInfo(), config.getModule(), new DatabaseChainRouter(), logger);
		this.cnf = config;

		this.parser = this.cnf.getParser();
		this.lang = this.cnf.getLang();
		this.module = this.cnf.getModule();

		this.parserInstance = this.parser.build(this.cnf);
		this.moduleInstance = this.module.build(this.cnf);
		this.langInstance = this.lang.build(this.parserInstance, this.module);

		this.router.initialize(this.parserInstance, this.langInstance, this.moduleInstance);
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
		chainManager.addChainLayers(BufferLayers.class);
		chainManager.addChainLayers(LookupLayers.class);
		chainManager.addChainLayers(ReduceLayer.class);
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void closeConnections() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closePipeline(Pipeline closingPipeline) {
		this.closeConnections();
	}

	// GETTER

	@Override
	public SessionInfo getSessionInfo() {
		return new SessionInfo(DatabasePipelineFactory.class, this.cnf.getBuffer()
				.getClass(), this.cnf.getPackageInfo(), this.cnf.getConnectionInfo());
	}

}