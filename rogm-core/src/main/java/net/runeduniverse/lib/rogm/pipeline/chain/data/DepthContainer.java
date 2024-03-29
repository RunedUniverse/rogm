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
package net.runeduniverse.lib.rogm.pipeline.chain.data;

import lombok.ToString;

@ToString
public class DepthContainer {
	private int depth;

	public DepthContainer(int depth) {
		this.set(depth);
	}

	public int getValue() {
		return this.depth;
	}

	public void set(int value) {
		if (depth < 0)
			this.depth = 0;
		else
			this.depth = value;
	}

	public void subtractOne() {
		this.depth = this.depth - 1;
	}
}
