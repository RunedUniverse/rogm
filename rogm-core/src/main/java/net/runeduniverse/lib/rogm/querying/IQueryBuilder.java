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
package net.runeduniverse.lib.rogm.querying;

import java.io.Serializable;
import java.util.Set;

import net.runeduniverse.lib.rogm.pattern.IBaseQueryPattern;

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