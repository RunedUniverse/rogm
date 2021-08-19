package net.runeduniverse.libs.rogm.pattern;

import static net.runeduniverse.libs.utils.StringUtils.isBlank;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import net.runeduniverse.libs.rogm.annotations.Direction;
import net.runeduniverse.libs.rogm.annotations.NodeEntity;
import net.runeduniverse.libs.rogm.annotations.PostSave;
import net.runeduniverse.libs.rogm.annotations.PreDelete;
import net.runeduniverse.libs.rogm.annotations.PreSave;
import net.runeduniverse.libs.rogm.buffer.IBuffer;
import net.runeduniverse.libs.rogm.buffer.IBuffer.Entry;
import net.runeduniverse.libs.rogm.buffer.IBuffer.LoadState;
import net.runeduniverse.libs.rogm.querying.IDataContainer;
import net.runeduniverse.libs.rogm.querying.IFilter;
import net.runeduniverse.libs.rogm.querying.IQueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder.RelationQueryBuilder;

public class NodePattern extends APattern implements INodePattern {

	@Getter
	private Set<String> labels = new HashSet<>();
	private Set<RelatedFieldPattern> relFields = new HashSet<>();

	public NodePattern(Archive archive, String pkg, ClassLoader loader, Class<?> type) {
		super(archive, pkg, loader, type);

		NodeEntity typeAnno = type.getAnnotation(NodeEntity.class);
		String label = null;
		if (typeAnno != null)
			label = typeAnno.label();
		if (isBlank(label) && !Modifier.isAbstract(type.getModifiers()))
			label = type.getSimpleName();
		if (!isBlank(label))
			this.labels.add(label);
	}

	public PatternType getPatternType() {
		return PatternType.NODE;
	}

	public NodeQueryBuilder search(boolean lazy) throws Exception {
		return this._search(this.archive.getQueryBuilder()
				.node()
				.where(this.type), lazy, false);
	}

	public NodeQueryBuilder search(Serializable id, boolean lazy) throws Exception {
		return this._search(this.archive.getQueryBuilder()
				.node()
				.where(this.type)
				.whereId(id), lazy, false);
	}

	@SuppressWarnings("deprecation")
	public NodeQueryBuilder search(RelationQueryBuilder caller, boolean lazy) {
		// includes ONLY the caller-relation filter
		NodeQueryBuilder nodeBuilder = this.archive.getQueryBuilder()
				.node()
				.where(this.type)
				.addRelation(caller);
		try {
			return this._search(nodeBuilder, lazy, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodeBuilder;
	}

	@SuppressWarnings("deprecation")
	private NodeQueryBuilder _search(NodeQueryBuilder nodeBuilder, boolean lazy, boolean optional) throws Exception {
		nodeBuilder.storePattern(this)
				.setReturned(true)
				.setOptional(optional);
		if (lazy)
			nodeBuilder.setLazy(true);
		else
			for (RelatedFieldPattern field : this.relFields)
				nodeBuilder.addRelation(field.queryRelation(nodeBuilder));
		return nodeBuilder;
	}

	@Override
	public ISaveContainer save(final IBuffer buffer, Object entity, Integer depth) throws Exception {
		Map<Object, IQueryBuilder<?, ? extends IFilter>> includedData = new HashMap<>();
		return new ISaveContainer() {

			@Override
			public IDataContainer getDataContainer() throws Exception {
				return (IDataContainer) NodePattern.this.save(entity, includedData, depth)
						.getResult();
			}

			@Override
			public void postSave() {
				for (Object object : includedData.keySet())
					if (object != null)
						try {
							archive.callMethod(object.getClass(), PostSave.class, object);
						} catch (Exception e) {
							e.printStackTrace();
						}
			}

			@Override
			public Set<IFilter> getRelatedFilter() throws Exception {
				Set<IFilter> set = new HashSet<>();
				for (Object object : includedData.keySet()) {
					if (!includedData.get(object)
							.persist())
						continue;
					Entry entry = buffer.getEntry(object);
					if (entry == null || entry.getLoadState() == LoadState.LAZY)
						continue;
					set.add(entry.getPattern()
							.search(entry.getId(), false)
							.getResult());
				}
				return set;
			}
		};
	}

	public NodeQueryBuilder save(Object entity, Map<Object, IQueryBuilder<?, ? extends IFilter>> includedData,
			Integer depth) throws Exception {
		if (entity == null)
			return null;

		boolean readonly = depth == -1;
		boolean persist = 0 < depth;
		IQueryBuilder<?, ? extends IFilter> container = includedData.get(entity);
		NodeQueryBuilder nodeBuilder = null;

		if (container != null) {
			if (!(!readonly && container.isReadonly()))
				return (NodeQueryBuilder) container;
			else
				nodeBuilder = (NodeQueryBuilder) container;
		} else if (this.isIdSet(entity)) {
			// update (id)
			nodeBuilder = this.archive.getQueryBuilder()
					.node()
					.where(this.type)
					.whereId(this.getId(entity))
					.storeData(entity)
					.setPersist(persist)
					.asUpdate();
		} else {
			// create (!id)
			nodeBuilder = this.archive.getQueryBuilder()
					.node()
					.where(this.type)
					.storeData(entity)
					.setPersist(persist)
					.asWrite();
		}

		this.callMethod(PreSave.class, entity);

		nodeBuilder.setReturned(true)
				.setReadonly(readonly);
		includedData.put(entity, nodeBuilder);

		if (persist) {
			depth = depth - 1;
			for (RelatedFieldPattern field : this.relFields)
				field.saveRelation(entity, nodeBuilder, includedData, depth);
		}

		return nodeBuilder;
	}

	@Override
	public IDeleteContainer delete(final IBuffer buffer, Object entity) throws Exception {
		IBuffer.Entry entry = buffer.getEntry(entity);
		if (entry == null)
			throw new Exception("Node-Entity of type<" + entity.getClass()
					.getName() + "> is not loaded!");

		this.callMethod(PreDelete.class, entity);

		QueryBuilder qryBuilder = this.archive.getQueryBuilder();
		return new DeleteContainer(this, entity, entry.getId(), qryBuilder.relation()
				.setStart(qryBuilder.node()
						.whereId(entry.getId()))
				.setTarget(qryBuilder.node()
						.setReturned(true))
				.setReturned(true)
				.getResult(),
				qryBuilder.node()
						.whereId(entry.getId())
						.setReturned(true)
						.asDelete()
						.getResult());
	}

	public void setRelation(Direction direction, String label, Object entity, Object value) {
		for (RelatedFieldPattern field : this.relFields)
			if (field.getDirection()
					.equals(direction)
					&& field.getLabel()
							.equals(label)
					&& field.getType()
							.isAssignableFrom(value.getClass())) {
				field.putValue(entity, value);
				return;
			}
	}

	public void deleteRelations(Object entity) {
		for (RelatedFieldPattern field : this.relFields)
			field.clearValue(entity);
	}

	@Override
	public void deleteRelations(Object entity, Collection<Object> delEntries) {
		for (RelatedFieldPattern field : this.relFields)
			field.removeValues(entity, delEntries);
	}
}
