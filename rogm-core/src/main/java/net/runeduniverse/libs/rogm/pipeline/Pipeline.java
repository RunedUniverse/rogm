package net.runeduniverse.libs.rogm.pipeline;

import net.runeduniverse.libs.rogm.Session;
import net.runeduniverse.libs.rogm.logging.PipelineLogger;
import net.runeduniverse.libs.rogm.pipeline.transaction.ATransactionFactory;

public class Pipeline {
	private final ATransactionFactory factory;
	private final PipelineLogger logger;

	public Pipeline(ATransactionFactory transactionBuilder) {
		this.factory = transactionBuilder;
		this.logger = new PipelineLogger(Pipeline.class, this.factory.getLogger());
	}

	public Session buildSession() {
		return new SessionWrapper(this.factory, this.logger, this.factory.getSessionInfo());
	}

	// FLOW
	// factory.setup();
	// factory.isConnected();
	// factory.stop();
}
