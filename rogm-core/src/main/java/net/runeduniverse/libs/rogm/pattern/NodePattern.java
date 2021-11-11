/*
 * Copyright © 2021 Pl4yingNight (pl4yingnight@gmail.com)
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
package net.runeduniverse.libs.rogm.pattern;

import static net.runeduniverse.libs.utils.StringUtils.isBlank;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import net.runeduniverse.libs.rogm.annotations.Direction;
import net.runeduniverse.libs.rogm.annotations.NodeEntity;
import net.runeduniverse.libs.rogm.annotations.PreDelete;
import net.runeduniverse.libs.rogm.annotations.PreSave;
import net.runeduniverse.libs.rogm.buffer.IBuffer;
import net.runeduniverse.libs.rogm.buffer.BufferTypes;
import net.runeduniverse.libs.rogm.pipeline.chain.data.SaveContainer;
import net.runeduniverse.libs.rogm.querying.IDataContainer;
import net.runeduniverse.libs.rogm.querying.IFilter;
import net.runeduniverse.libs.rogm.querying.IQueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder.NodeQueryBuilder;
import net.runeduniverse.libs.rogm.querying.QueryBuilder.RelationQueryBuilder;

// deprecation = internal use
@SuppressWarnings("deprecation")
public class NodePattern extends APattern<NodeQueryBuilder>
		implements INodePattern<NodeQueryBuilder>, BufferTypes {

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

	@Override
	public void validate() throws Exception {
		for (FieldPattern fieldPattern : this.fields.values())
			if (fieldPattern instanceof RelatedFieldPattern)
				this.relFields.add((RelatedFieldPattern) fieldPattern);
		super.validate();
	}

	public PatternType getPatternType() {
		return PatternType.NODE;
	}

	public NodeQueryBuilder search(boolean lazy) throws Exception {
		return this.completeSearch(this.archive.getQueryBuilder()
				.node()
				.setAutoGenerated(true)
				.where(this.type)
				.setLazy(lazy)
				.setReturned(true));
	}

	public NodeQueryBuilder search(Serializable id, boolean lazy) throws Exception {
		return this.completeSearch(this.archive.getQueryBuilder()
				.node()
				.setAutoGenerated(true)
				.where(this.type)
				.whereId(id)
				.setLazy(lazy)
				.setReturned(true));
	}

	@Deprecated
	public NodeQueryBuilder search(RelationQueryBuilder caller, boolean lazy) {
		// includes ONLY the caller-relation filter
		NodeQueryBuilder nodeBuilder = this.archive.getQueryBuilder()
				.node()
				.setAutoGenerated(true)
				.where(this.type)
				.setReturned(true)
				.setOptional(true)
				.setLazy(lazy)
				.addRelation(caller);
		try {
			return this.completeSearch(nodeBuilder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodeBuilder;
	}

	public NodeQueryBuilder completeSearch(NodeQueryBuilder nodeBuilder) throws Exception {
		nodeBuilder.storePattern(this);
		if (!nodeBuilder.isLazy())
			for (RelatedFieldPattern field : this.relFields)
				nodeBuilder.addRelation(field.queryRelation(nodeBuilder));
		return nodeBuilder;
	}

	@Override
	public SaveContainer save(Object entity, Integer depth) throws Exception {
		return new SaveContainer(includedData -> (IDataContainer) this.save(entity, includedData, depth)
				.getResult(), NodePattern::calcEffectedFilters);
	}

	/***
	 * implementation of FunctionalInterface
	 * <code>SaveContainer.EffectedFilterCalculator</code>
	 * 
	 * @param archive
	 * @param buffer
	 * @param includedData
	 * @return Set<IFilter> of IFilter Objects describing the effected Entities of
	 *         the save procedure
	 * @throws Exception
	 */
	private static final Set<IFilter> calcEffectedFilters(final Archive archive, final IBuffer buffer,
			final Map<Object, IQueryBuilder<?, ?, ? extends IFilter>> includedData) throws Exception {
		Set<IFilter> set = new HashSet<>();
		for (Object object : includedData.keySet()) {
			if (!includedData.get(object)
					.persist())
				continue;
			IEntry entry = buffer.getEntry(object);
			if (entry == null || entry.getLoadState() == LoadState.LAZY)
				continue;
			set.add(archive.search(entry.getType(), entry.getId(), false)
					.getResult());
		}
		return set;
	}

	public NodeQueryBuilder save(Object entity, Map<Object, IQueryBuilder<?, ?, ? extends IFilter>> includedData,
			Integer depth) throws Exception {
		if (entity == null)
			return null;

		boolean readonly = depth == -1;
		boolean persist = 0 < depth;
		IQueryBuilder<?, ?, ? extends IFilter> container = includedData.get(entity);
		NodeQueryBuilder nodeBuilder = null;

		if (container != null) {
			if (!(!readonly && container.isReadonly()))
				return (NodeQueryBuilder) container;
			else
				nodeBuilder = (NodeQueryBuilder) container;
		} else {
			nodeBuilder = this.archive.getQueryBuilder()
					.node()
					.where(this.type)
					.storeData(entity)
					.setPersist(persist);

			if (this.isIdSet(entity)) {
				// update (id)
				nodeBuilder.whereId(this.getId(entity))
						.asUpdate();
			} else {
				// create (!id)
				nodeBuilder.asWrite();
			}
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
	public IDeleteContainer delete(final Serializable id, Object entity) throws Exception {
		this.archive.callMethod(entity.getClass(), PreDelete.class, entity);
		QueryBuilder qryBuilder = this.archive.getQueryBuilder();
		return new DeleteContainer(this, entity, id, qryBuilder.relation()
				.setStart(qryBuilder.node()
						.whereId(id))
				.setTarget(qryBuilder.node()
						.setReturned(true))
				.setReturned(true)
				.getResult(),
				qryBuilder.node()
						.whereId(id)
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
