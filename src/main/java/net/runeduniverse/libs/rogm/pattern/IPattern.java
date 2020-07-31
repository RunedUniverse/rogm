package net.runeduniverse.libs.rogm.pattern;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.runeduniverse.libs.rogm.annotations.IConverter;
import net.runeduniverse.libs.rogm.querying.IDataContainer;
import net.runeduniverse.libs.rogm.querying.IFRelation;
import net.runeduniverse.libs.rogm.querying.IFilter;

public interface IPattern {
	boolean isIdSet(Object entity);

	Serializable getId(Object entity);

	Class<?> getType();

	IConverter<?> getIdConverter();

	IFilter search(boolean lazy) throws Exception;

	// search exactly 1 node / querry deeper layers for node
	IFilter search(Serializable id, boolean lazy) throws Exception;

	ISaveContainer save(Object entity, boolean lazy) throws Exception;

	IDeleteContainer delete(Object entity) throws Exception;

	void deleteRelations(Object entity, Collection<Object> deletedEntities);

	Object setId(Object entity, Serializable id) throws IllegalArgumentException;

	Serializable prepareEntityId(Serializable id, Serializable entityId);

	Object parse(IData data) throws Exception;

	void preSave(Object entity);

	void preDelete(Object entity);

	void postLoad(Object entity);

	void postSave(Object entity);

	void postDelete(Object entity);

	public interface IPatternContainer extends IFilter {
		IPattern getPattern();

		public static boolean identify(IFilter filter) {
			return filter instanceof IPatternContainer && ((IPatternContainer) filter).getPattern() != null;
		}
	}

	public interface IData {
		Serializable getId();

		Serializable getEntityId();

		void setEntityId(Serializable entityId);

		Set<String> getLabels();

		String getData();

		IFilter getFilter();
	}

	public interface IDataRecord {
		IPatternContainer getPrimaryFilter();

		Set<Serializable> getIds();

		List<Set<IData>> getData();
	}

	public interface ISaveContainer {
		IDataContainer getDataContainer() throws Exception;

		IFilter getRelatedFilter() throws Exception;

		void postSave();
	}

	public interface IDeleteContainer {
		IFRelation getEffectedFilter();

		IFilter getDeleteFilter();

		Serializable getDeletedId();
	}
}
