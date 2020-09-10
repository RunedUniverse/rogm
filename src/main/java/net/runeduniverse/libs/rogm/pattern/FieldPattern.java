package net.runeduniverse.libs.rogm.pattern;

import static net.runeduniverse.libs.util.StringUtils.isBlank;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

import lombok.Data;
import net.runeduniverse.libs.rogm.annotations.Direction;
import net.runeduniverse.libs.rogm.annotations.NodeEntity;
import net.runeduniverse.libs.rogm.annotations.Relationship;
import net.runeduniverse.libs.rogm.annotations.RelationshipEntity;
import net.runeduniverse.libs.rogm.pattern.FilterFactory.IDataNode;
import net.runeduniverse.libs.rogm.pattern.FilterFactory.IDataRelation;
import net.runeduniverse.libs.rogm.pattern.FilterFactory.Relation;
import net.runeduniverse.libs.rogm.querying.FilterType;
import net.runeduniverse.libs.rogm.querying.IDataContainer;
import net.runeduniverse.libs.rogm.querying.IFNode;
import net.runeduniverse.libs.rogm.querying.IFRelation;

@Data
public class FieldPattern {

	private final PatternStorage storage;
	private final Field field;
	private final Class<?> type;
	private final String label;
	private final Direction direction;
	private final boolean collection;
	private final boolean defined;

	public FieldPattern(PatternStorage storage, Field field) throws Exception {
		this.storage = storage;
		this.field = field;
		this.field.setAccessible(true);
		Relationship fieldAnno = this.field.getAnnotation(Relationship.class);
		this.direction = fieldAnno.direction();
		Class<?> clazz = this.field.getType();
		this.collection = Collection.class.isAssignableFrom(clazz);
		if (this.collection)
			clazz = (Class<?>) ((ParameterizedType) this.field.getGenericType()).getActualTypeArguments()[0];
		this.type = clazz;

		String label = null;
		if (this.type.isAnnotationPresent(NodeEntity.class))
			this.defined = false;
		else if (clazz.isAnnotationPresent(RelationshipEntity.class)) {
			label = this.storage.getRelation(clazz).getLabel();
			this.defined = true;
		} else
			throw new Exception("Unsupported Class<" + clazz.getName() + "> as @Relation found!");
		if (isBlank(label))
			label = isBlank(fieldAnno.label()) ? this.field.getName() : fieldAnno.label();
		this.label = label;
	}

	public IFRelation queryRelation(IFNode origin) throws Exception {
		Relation relation = null;
		if (this.defined)
			relation = this.storage.getRelation(this.type).createFilter(origin, this.direction);
		else {
			relation = this.storage.getFactory().createRelation(this.direction);
			relation.setStart(origin);
			relation.setTarget(this._getNode(this.type, relation));
		}

		if (relation.getLabels().isEmpty())
			relation.getLabels().add(this.label);

		relation.setReturned(true);
		relation.setOptional(true);
		return relation;
	}

	private IFNode _getNode(Class<?> type, IFRelation relation) throws Exception {
		NodePattern node = this.storage.getNode(type);
		if (node == null)
			throw new Exception("Unsupported Class<" + type.getName() + "> as @Relation found!");
		return node.search(relation, true);
	}

	public void saveRelation(Object entity, IDataNode node, Map<Object, IDataContainer> includedData, Integer depth)
			throws Exception {
		if (entity == null)
			return;
		if (this.collection)
			// Collection
			for (Object relNode : (Collection<?>) this.field.get(entity))
				_addRelation(node, relNode, includedData, depth);
		else
			// Variable
			_addRelation(node, this.field.get(entity), includedData, depth);
	}

	private void _addRelation(IDataNode node, Object relEntity, Map<Object, IDataContainer> includedData, Integer depth)
			throws Exception {
		if (relEntity == null)
			return;

		IDataRelation relation = null;
		// clazz could be substituted with this.type but isn't in case the entities type
		// is a child of this.type
		Class<?> clazz = relEntity.getClass();
		if (clazz.isAnnotationPresent(RelationshipEntity.class)) {
			relation = this.storage.getRelation(clazz).save(relEntity, node, this.direction, includedData, depth);
			if (relation == null)
				return;
		} else {
			relation = this.storage.getFactory().createDataRelation(this.direction, null);
			relation.setFilterType(FilterType.UPDATE);
			relation.setStart(node);
			relation.setTarget(this.storage.getNode(clazz).save(relEntity, includedData, depth));
		}

		if (relation.getLabels().isEmpty())
			relation.getLabels().add(this.label);

		node.getRelations().add(relation);
	}

	public void setValue(Object holder, Object value) throws IllegalArgumentException {
		try {
			this.field.set(holder, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void putValue(Object entity, Object value) throws IllegalArgumentException {
		try {
			if (this.collection) {
				((Collection<Object>) this.field.get(entity)).add(value);
				return;
			}
			this.field.set(entity, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Object getValue(Object holder) throws IllegalArgumentException {
		try {
			return this.field.get(holder);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void removeValues(Object entity, Collection<Object> deletedEntities) {
		try {
			if (this.collection) {
				((Collection<Object>) this.field.get(entity)).removeAll(deletedEntities);
				return;
			}
			this.field.set(entity, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
