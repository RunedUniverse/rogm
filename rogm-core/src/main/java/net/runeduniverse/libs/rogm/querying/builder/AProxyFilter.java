package net.runeduniverse.libs.rogm.querying.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.runeduniverse.libs.rogm.querying.FilterType;
import net.runeduniverse.libs.rogm.querying.IFilter;
import net.runeduniverse.libs.rogm.querying.ILabeled;

public abstract class AProxyFilter<FILTER> implements IFilter, ILabeled, InvocationHandler {
	protected FILTER instance;

	@Getter
	protected final Map<Class<?>, Object> handler = new HashMap<>();
	protected final Set<Method> localMethods = new HashSet<>();
	@Getter
	@Setter
	protected FilterType filterType = FilterType.MATCH;
	@Getter
	protected Set<String> labels = new HashSet<>();

	public FILTER addLabel(String label) {
		this.labels.add(label);
		return this.instance;
	}

	public FILTER addLabels(Collection<String> labels) {
		this.labels.addAll(labels);
		return this.instance;
	}

	public Class<?>[] buildInvocationHandler() {
		Set<Class<?>> l = new HashSet<>();
		l.add(IFilter.class);
		for (Class<?> i : this.instance.getClass()
				.getInterfaces())
			l.add(i);

		this.handler.forEach((c, i) -> {
			if (i != null)
				l.add(c);
		});

		_collectMethods(this.instance.getClass());
		for (Class<?> calzz : l)
			_collectMethods(calzz);
		return l.toArray(new Class<?>[l.size()]);
	}

	private void _collectMethods(Class<?> clazz) {
		Method[] mArr = clazz.getMethods();
		for (int i = 0; i < mArr.length; i++)
			this.localMethods.add(mArr[i]);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (this.localMethods.contains(method))
			return method.invoke(this.instance, args);

		for (Class<?> clazz : this.handler.keySet()) {
			if (has(clazz, method))
				return method.invoke(this.handler.get(clazz), args);
		}
		if (has(Object.class, method))
			return method.invoke(this.instance, args);
		throw new Exception("Interface for Method<" + method + "> not found! > ");
	}

	protected static boolean has(Class<?> c, Method method) {
		return Arrays.asList(c.getMethods())
				.contains(method);
	}
}
