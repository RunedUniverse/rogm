package net.runeduniverse.libs.rogm.pattern;

import static net.runeduniverse.libs.utils.StringUtils.isBlank;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.runeduniverse.libs.rogm.pattern.FilterFactory.DataNode;
import net.runeduniverse.libs.rogm.pattern.FilterFactory.IDataNode;
import net.runeduniverse.libs.rogm.pattern.FilterFactory.Node;
import net.runeduniverse.libs.rogm.querying.FilterType;
import net.runeduniverse.libs.rogm.querying.IDataContainer;
import net.runeduniverse.libs.rogm.querying.IFNode;
import net.runeduniverse.libs.rogm.querying.IFRelation;
import net.runeduniverse.libs.rogm.querying.IFilter;

public class NodePattern extends APattern implements INodePattern {

	@Getter
	private Set<String> labels = new HashSet<>();
	private Set<RelatedFieldPattern> relFields = new HashSet<>();

	public NodePattern(IStorage factory, String pkg, ClassLoader loader, Class<?> type) {
		super(factory, pkg, loader, type);

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

	public IFilter search(boolean lazy) throws Exception {
		return this._search(this.factory.getFactory()
				.createNode(this.labels, new ArrayList<>()), lazy, false);
	}

	public IFilter search(Serializable id, boolean lazy) throws Exception {
		return this._search(this.factory.getFactory()
				.createIdNode(this.labels, new ArrayList<>(), id, this.idConverter), lazy, false);
	}

	public IFNode search(IFRelation caller, boolean lazy) {
		// includes ONLY the caller-relation filter
		Node node = this.factory.getFactory()
				.createNode(this.labels, Arrays.asList(caller));
		try {
			return this._search(node, lazy, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return node;
	}

	private IFNode _search(Node node, boolean lazy, boolean optional) throws Exception {
		node.setPattern(this);
		node.setReturned(true);
		if (optional)
			node.setOptional(true);
		if (lazy)
			node.setLazy(true);
		else
			for (RelatedFieldPattern field : this.relFields)
				node.getRelations()
						.add(field.queryRelation(node));
		return node;
	}

	@Override
	public ISaveContainer save(Object entity, Integer depth) throws Exception {
		Map<Object, IDataContainer> includedData = new HashMap<>();
		return new ISaveContainer() {

			@Override
			public IDataContainer getDataContainer() throws Exception {
				return NodePattern.this.save(entity, includedData, depth);
			}

			@Override
			public void postSave() {
				for (Object object : includedData.keySet())
					if (object != null)
						try {
							factory.getPattern(object.getClass())
									.callMethod(PostSave.class, object);
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
					Entry entry = factory.getBuffer()
							.getEntry(object);
					if (entry == null || entry.getLoadState() == LoadState.LAZY)
						continue;
					set.add(entry.getPattern()
							.search(entry.getId(), false));
				}
				return set;
			}
		};
	}

	public IDataNode save(Object entity, Map<Object, IDataContainer> includedData, Integer depth) throws Exception {
		if (entity == null)
			return null;

		boolean readonly = depth == -1;
		boolean persist = 0 < depth;
		IDataContainer container = includedData.get(entity);
		DataNode node = null;

		if (container != null) {
			if (!(!readonly && container.isReadonly()))
				return (IDataNode) container;
			else
				node = (DataNode) container;
		} else if (this.isIdSet(entity)) {
			// update (id)
			node = this.factory.getFactory()
					.createIdDataNode(this.labels, new ArrayList<>(), this.getId(entity), this.idConverter, entity,
							persist);
			node.setFilterType(FilterType.UPDATE);
		} else {
			// create (!id)
			node = this.factory.getFactory()
					.createDataNode(this.labels, new ArrayList<>(), entity, persist);
			node.setFilterType(FilterType.CREATE);
		}

		this.callMethod(PreSave.class, entity);

		node.setReturned(true);
		node.setReadonly(readonly);
		includedData.put(entity, node);

		if (persist) {
			depth = depth - 1;
			for (RelatedFieldPattern field : this.relFields)
				field.saveRelation(entity, node, includedData, depth);
		}

		return node;
	}

	@Override
	public IDeleteContainer delete(Object entity) throws Exception {
		IBuffer.Entry entry = this.factory.getBuffer()
				.getEntry(entity);
		if (entry == null)
			throw new Exception("Node-Entity of type<" + entity.getClass()
					.getName() + "> is not loaded!");

		this.callMethod(PreDelete.class, entity);

		Node node = this.factory.getFactory()
				.createIdNode(null, null, entry.getId(), null);
		node.setReturned(true);
		return new DeleteContainer(this, entity, entry.getId(), this.factory.getFactory()
				.createEffectedFilter(entry.getId()), node);
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
