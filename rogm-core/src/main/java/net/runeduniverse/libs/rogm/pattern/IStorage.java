package net.runeduniverse.libs.rogm.pattern;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import net.runeduniverse.libs.rogm.Configuration;
import net.runeduniverse.libs.rogm.buffer.IBuffer;
import net.runeduniverse.libs.rogm.buffer.IBuffer.Entry;
import net.runeduniverse.libs.rogm.logging.UniversalLogger;
import net.runeduniverse.libs.rogm.parser.Parser;
import net.runeduniverse.libs.rogm.pattern.IPattern.IDataRecord;
import net.runeduniverse.libs.rogm.pattern.IPattern.IDeleteContainer;
import net.runeduniverse.libs.rogm.pattern.IPattern.ISaveContainer;
import net.runeduniverse.libs.rogm.querying.IFilter;

public interface IStorage {

	Configuration getConfig();

	Parser.Instance getParser();

	IBuffer getBuffer();
	
	UniversalLogger getLogger();

	IPattern getPattern(Class<?> clazz) throws Exception;

	IFilter search(Class<?> clazz, boolean lazy) throws Exception;

	IFilter search(Object entity, boolean lazy) throws Exception;

	IFilter search(Class<?> clazz, Serializable id, boolean lazy) throws Exception;

	ISaveContainer save(Object entity, Integer depth) throws Exception;

	IDeleteContainer delete(Object entity) throws Exception;

	<T> Collection<T> parse(Class<T> type, IDataRecord record, Set<Entry> lazyEntries) throws Exception;

	void update(Object entity, IDataRecord record, Set<Entry> relatedEntities) throws Exception;

	FilterFactory getFactory();

	INodePattern getNode(Class<?> clazz);

	IRelationPattern getRelation(Class<?> clazz);
}
