package net.runeduniverse.libs.rogm;

import net.runeduniverse.libs.rogm.lang.Language;
import net.runeduniverse.libs.rogm.modules.Module;
import net.runeduniverse.libs.rogm.parser.Parser;

public abstract class ATest {

	protected final DatabaseType dbType;
	// Builder
	protected final Parser parser;
	protected final Module module;
	// Instances
	protected final Parser.Instance iParser;
	protected final Module.Instance<?> iModule;
	protected final Language iLanguage;

	public ATest(DatabaseType dbType) {
		this.dbType = dbType;
		Configuration cnf = new Configuration(dbType, "");
		// Builder
		this.parser = dbType.getParser();
		this.module = dbType.getModule();
		// Instances
		this.iParser = this.parser.build(cnf);
		this.iModule = this.module.build(cnf);
		this.iLanguage = dbType.getLang();
	}
	
	public ATest(Configuration cnf) {
		this.dbType = cnf.getDbType();
		// Builder
		this.parser = this.dbType.getParser();
		this.module = this.dbType.getModule();
		// Instances
		this.iParser = this.parser.build(cnf);
		this.iModule = this.module.build(cnf);
		this.iLanguage = this.dbType.getLang();
	}

}
