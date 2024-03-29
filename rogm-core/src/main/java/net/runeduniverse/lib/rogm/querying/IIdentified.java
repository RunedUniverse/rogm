/*
 * Copyright © 2022 Pl4yingNight (pl4yingnight@gmail.com)
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

public interface IIdentified<ID extends Serializable> extends IFilter {

	ID getId();

	public default Class<?> getIdType() {
		return getId().getClass();
	}

	public static Class<?> getIdType(IFilter filter) {
		if (!identify(filter))
			return null;
		return ((IIdentified<?>) filter).getIdType();
	}

	public static boolean identify(IFilter filter) {
		if (filter == null || !(filter instanceof IIdentified) || ((IIdentified<?>) filter).getId() == null)
			return false;
		return true;
	}
}
