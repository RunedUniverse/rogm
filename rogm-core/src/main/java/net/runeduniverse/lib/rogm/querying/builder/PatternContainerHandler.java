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
package net.runeduniverse.lib.rogm.querying.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.runeduniverse.lib.rogm.pattern.IBaseQueryPattern;
import net.runeduniverse.lib.rogm.pattern.IPattern;
import net.runeduniverse.lib.utils.logging.logs.CompoundTree;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatternContainerHandler implements IPattern.IPatternContainer, NoFilterType, ITraceable {

	private IBaseQueryPattern<?> pattern;

	@Override
	public void toRecord(CompoundTree tree) {
		if (this.pattern != null)
			tree.append("PATTERN", this.pattern.getClass()
					.getCanonicalName());
	}
}
