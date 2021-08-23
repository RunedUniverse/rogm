package net.runeduniverse.libs.rogm.pipeline.chain.sys;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.libs.rogm.error.ExceptionSurpression;

@SuppressWarnings("deprecation")
public class ChainRuntime<R> {

	// runtime
	@Getter
	protected final ChainRuntime<?> root;
	protected final ChainContainer container;
	protected final Store store;
	@Getter
	@Setter
	protected boolean canceled = false;
	// result
	@Getter
	private final Class<R> resultType;
	@Getter
	private R result;
	// execution
	private final Iterator iterator = new Iterator(Integer.MIN_VALUE);

	protected ChainRuntime(ChainContainer container, Class<R> resultType, Object[] args) {
		this(null, container, resultType, null, args);
	}

	protected ChainRuntime(ChainRuntime<?> root, ChainContainer container, Class<R> resultType,
			Map<Class<?>, Object> sourceDataMap, Object[] args) {
		this.root = root;
		this.container = container;
		this.store = new Store(this, sourceDataMap, args);
		this.resultType = resultType;
	}

	public <S> S callSubChain(String label, Class<S> resultType, Object... args) throws Exception {
		return container.getManager()
				.callChain(label, resultType, this, null, args);
	}

	public <S> S callSubChainWithSourceData(String label, Class<S> resultType, Object... args) throws Exception {
		return container.getManager()
				.callChain(label, resultType, this, this.store.getSourceDataMap(), args);
	}

	public <S> S callSubChainWithRuntimeData(String label, Class<S> resultType, Object... args) throws Exception {
		return container.getManager()
				.callChain(label, resultType, this, this.store.getRuntimeDataMap(), args);
	}

	protected void executeOnChain(final Map<Integer, ILayer> chain, final int lowestId, final int highestId)
			throws ExceptionSurpression {
		Set<Exception> errors = new HashSet<>();
		boolean noErrors = true;

		for (this.iterator.setI(lowestId); this.iterator.getI() <= highestId; this.iterator.next()) {
			ILayer layer = chain.get(this.iterator.getI());
			if (layer != null)
				try {
					if ((noErrors || ChainLayer.ignoreErrors(layer))
							&& (this.active() || ChainLayer.ignoreInActive(layer)))
						layer.call(this);
				} catch (Exception e) {
					errors.add(e);
					noErrors = false;
				}
		}

		if (!noErrors)
			throw new ExceptionSurpression(
					"ChainRuntime[" + this.hashCode() + "] of Chain<" + this.container.getLabel() + "> errored out!")
							.addSuppressed(errors);
	}

	public void jumpToLayer(int layerId) {
		this.iterator.setI(layerId);
	}

	public <D extends Object> D storeData(D entity) {
		return this.storeData(entity.getClass(), entity);
	}

	public <D extends Object> D storeData(Class<?> type, D entity) {
		if (entity instanceof ChainRuntime<?> || entity instanceof Store)
			return entity;

		this.store.putData(type, entity);
		return entity;
	}

	public void setResult(R result) {
		this.result = result;
	}

	@SuppressWarnings("unchecked")
	public boolean setPossibleResult(Object entity) {
		if (this.resultType.isAssignableFrom(entity.getClass())) {
			this.result = (R) entity;
			return true;
		}
		return false;
	}

	public Object[] getParameters(Class<?>[] paramTypes) {
		return this.store.getData(paramTypes);
	}

	public boolean active() {
		if (this.canceled || this.result != null)
			return false;
		return true;
	}

	public boolean isRoot() {
		return this.root == null;
	}

	public boolean hasResult() {
		return this.result != null;
	}

	@SuppressWarnings("unchecked")
	protected R getFinalResult() {
		if (this.canceled)
			return null;

		if (this.resultType == null)
			return (R) store.getLastAdded();
		else if (this.result != null)
			return this.result;
		return store.getData(this.resultType);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	private static class Iterator {
		private int i;

		public void next() {
			this.i = this.i + 1;
		}
	}
}
