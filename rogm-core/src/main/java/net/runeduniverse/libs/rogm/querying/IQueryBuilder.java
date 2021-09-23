package net.runeduniverse.libs.rogm.querying;

import java.io.Serializable;
import java.util.Set;

import net.runeduniverse.libs.rogm.pattern.IBaseQueryPattern;

public interface IQueryBuilder<B extends IQueryBuilder<?, ?, R>, P extends IBaseQueryPattern<?>, R extends IFilter> {

	// internal use only
	@Deprecated
	public B setAutoGenerated(boolean state);

	public B where(Class<?> type);

	public B whereParam(String label, Object value);

	public B whereId(Serializable id);

	public B storePattern(P pattern);

	public B storeData(Object data);

	public B setOptional(boolean optional);

	public B setReturned(boolean returned);

	public B setPersist(boolean persist);

	public B setLazy(boolean lazy);

	public B setReadonly(boolean readonly);

	public boolean isAutoGenerated();

	public Set<String> getLabels();

	public P getStoredPattern();

	public Object getStoredData();

	public boolean isOptional();

	public boolean isReturned();

	public boolean persist();

	public boolean isLazy();

	public boolean isReadonly();

	public B asRead();

	public B asWrite();

	public B asUpdate();

	public B asDelete();

	public R getResult();
}
