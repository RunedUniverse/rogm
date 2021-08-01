package net.runeduniverse.libs.rogm.querying;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runeduniverse.libs.rogm.annotations.Direction;
import net.runeduniverse.libs.rogm.pattern.Archive;
import net.runeduniverse.libs.rogm.pattern.IPattern;
import net.runeduniverse.libs.rogm.querying.builder.AProxyFilter;
import net.runeduniverse.libs.rogm.querying.builder.DataContainerHandler;
import net.runeduniverse.libs.rogm.querying.builder.IdentifiedHandler;
import net.runeduniverse.libs.rogm.querying.builder.LazyLoadingHandler;
import net.runeduniverse.libs.rogm.querying.builder.NodeFilter;
import net.runeduniverse.libs.rogm.querying.builder.OptionalHandler;
import net.runeduniverse.libs.rogm.querying.builder.ParamHandler;
import net.runeduniverse.libs.rogm.querying.builder.PatternContainerHandler;
import net.runeduniverse.libs.rogm.querying.builder.RelationFilter;
import net.runeduniverse.libs.rogm.querying.builder.ReturnedHandler;

public final class QueryBuilder {
	private final Archive archive;

	public QueryBuilder(Archive archive) {
		this.archive = archive;
	}

	public NodeQueryBuilder node() {
		return new NodeQueryBuilder(this.archive);
	}

	public RelationQueryBuilder relation() {
		return new RelationQueryBuilder(this.archive);
	}

	public static final class NodeQueryBuilder extends AQueryBuilder<NodeQueryBuilder, NodeFilter, IFNode> {

		private Set<RelationQueryBuilder> relationBuilders = new HashSet<>();
		private Set<IFRelation> relations = new HashSet<>();

		public NodeQueryBuilder(Archive archive) {
			super(archive, new NodeFilter());
			super.instance = this;
		}

		public NodeQueryBuilder addRelation(RelationQueryBuilder relation) {
			this.relationBuilders.add(relation);
			return this;
		}

		public NodeQueryBuilder setLazy(boolean lazy) {
			if (lazy)
				this.handler.put(ILazyLoading.class, new LazyLoadingHandler(lazy));
			else
				this.handler.remove(ILazyLoading.class);
			return this;
		}

		public IFNode build() {
			super.prebuild();

			for (RelationQueryBuilder rqb : this.relationBuilders)
				this.relations.add(rqb.getResult());

			this.proxyFilter.getRelations()
					.addAll(this.relations);

			return (IFNode) Proxy.newProxyInstance(QueryBuilder.class.getClassLoader(),
					this.proxyFilter.buildInvocationHandler(), this.proxyFilter);
		}
	}

	public static final class RelationQueryBuilder
			extends AQueryBuilder<RelationQueryBuilder, RelationFilter, IFRelation> {

		private Direction direction = Direction.BIDIRECTIONAL;
		private NodeQueryBuilder startNodeBuilder = null;
		private NodeQueryBuilder targetNodeBuilder = null;

		public RelationQueryBuilder(Archive archive) {
			super(archive, new RelationFilter());
		}

		public RelationQueryBuilder whereDirection(Direction direction) {
			this.direction = direction == null ? Direction.BIDIRECTIONAL : direction;
			return this;
		}

		public RelationQueryBuilder setStart(NodeQueryBuilder start) {
			this.startNodeBuilder = start;
			return this;
		}

		public RelationQueryBuilder setTarget(NodeQueryBuilder target) {
			this.targetNodeBuilder = target;
			return this;
		}

		public NodeQueryBuilder getStart() {
			return this.startNodeBuilder;
		}

		public NodeQueryBuilder getTarget() {
			return this.targetNodeBuilder;
		}

		public IFRelation build() {
			super.prebuild();

			this.proxyFilter.setDirection(this.direction);
			this.proxyFilter.setStart(this.startNodeBuilder.getResult());
			this.proxyFilter.setTarget(this.targetNodeBuilder.getResult());

			return (IFRelation) Proxy.newProxyInstance(QueryBuilder.class.getClassLoader(),
					this.proxyFilter.buildInvocationHandler(), this.proxyFilter);
		}
	}

	protected static abstract class AQueryBuilder<BUILDER extends AQueryBuilder<?, PROXY_FILTER, RESULT>, PROXY_FILTER extends AProxyFilter<?>, RESULT extends IFilter>
			implements IQueryBuilder<BUILDER, RESULT> {

		protected final Archive archive;
		protected final PROXY_FILTER proxyFilter;
		protected final Map<Class<?>, Object> handler;

		// BUILDER instance:
		// must be configured in child-constructor for chaining to work!
		protected BUILDER instance;
		protected RESULT result = null;
		protected Class<?> type = null;
		protected Serializable id = null;

		protected AQueryBuilder(Archive archive, PROXY_FILTER proxyFilter) {
			this.archive = archive;
			this.proxyFilter = proxyFilter;
			this.handler = this.proxyFilter.getHandler();
		}

		public BUILDER where(Class<?> type) {
			this.type = type;
			for (IPattern p : this.archive.getPatterns(type))
				proxyFilter.addLabels(p.getLabels());
			return this.instance;
		}

		@Override
		public BUILDER whereParam(String label, Object value) {
			this.addParam(label, value);
			return this.instance;
		}

		@Override
		public BUILDER whereId(Serializable id) {
			this.id = id;
			return this.instance;
		}

		@Override
		public BUILDER storePattern(IPattern pattern) {
			this.handler.put(IPattern.IPatternContainer.class, new PatternContainerHandler(pattern));
			return this.instance;
		}

		@Override
		public BUILDER storeData(Object data) {
			AQueryBuilder.ensure(this.handler, IDataContainer.class, new DataContainerHandler())
					.setData(data);
			return this.instance;
		}

		@Override
		public BUILDER setOptional(boolean optional) {
			if (optional)
				this.handler.put(IOptional.class, new OptionalHandler(optional));
			else
				this.handler.remove(IOptional.class);
			return this.instance;
		}

		@Override
		public BUILDER setReturned(boolean returned) {
			if (returned)
				this.handler.put(IReturned.class, new ReturnedHandler(returned));
			else
				this.handler.remove(IReturned.class);
			return this.instance;
		}

		@Override
		public BUILDER setPersist(boolean persist) {
			AQueryBuilder.ensure(this.handler, IDataContainer.class, new DataContainerHandler())
					.setPersist(persist);
			return this.instance;
		}

		@Override
		public BUILDER setReadonly(boolean readonly) {
			AQueryBuilder.ensure(this.handler, IDataContainer.class, new DataContainerHandler())
					.setReadonly(readonly);
			return this.instance;
		}

		public Set<String> getLabels() {
			return this.proxyFilter.getLabels();
		}

		@Override
		public boolean persist() {
			DataContainerHandler container = (DataContainerHandler) handler.get(IDataContainer.class);
			if (container == null)
				return false;
			return container.persist();
		}

		@Override
		public boolean isReadonly() {
			DataContainerHandler container = (DataContainerHandler) handler.get(IDataContainer.class);
			if (container == null)
				return false;
			return container.isReadonly();
		}

		@Override
		public BUILDER asRead() {
			this.proxyFilter.setFilterType(FilterType.MATCH);
			return this.instance;
		}

		@Override
		public BUILDER asWrite() {
			this.proxyFilter.setFilterType(FilterType.CREATE);
			return this.instance;
		}

		@Override
		public BUILDER asUpdate() {
			this.proxyFilter.setFilterType(FilterType.UPDATE);
			return this.instance;
		}

		@Override
		public BUILDER asDelete() {
			this.proxyFilter.setFilterType(FilterType.DELETE);
			return this.instance;
		}

		public void prebuild() {
			if (this.id == null)
				return;
			if (this.archive.getCnf()
					.getModule()
					.checkIdType(this.id.getClass()))
				this.handler.put(IdentifiedHandler.class, new IdentifiedHandler(this.id));
			else
				this.addParam(this.archive.getCnf()
						.getModule()
						.getIdAlias(),
						this.archive.getIdFieldConverter(this.type)
								.toProperty(this.id));
		}

		@Override
		public abstract RESULT build();

		@Override
		public RESULT getResult() {
			if (this.result == null)
				this.result = this.build();
			return this.result;
		}

		protected void addParam(String label, Object value) {
			AQueryBuilder.ensure(this.handler, IParameterized.class, new ParamHandler())
					.addParam(label, value);
		}

		@SuppressWarnings("unchecked")
		protected static <T> T ensure(final Map<Class<?>, Object> handler, Class<?> keyInterface, T instance) {
			if (!handler.containsKey(keyInterface))
				handler.put(keyInterface, instance);
			return (T) handler.get(keyInterface);
		}
	}
}
