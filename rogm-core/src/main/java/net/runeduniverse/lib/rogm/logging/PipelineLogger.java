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
package net.runeduniverse.lib.rogm.logging;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.runeduniverse.lib.utils.logging.ALogger;

public final class PipelineLogger extends ALogger {

	private static final AtomicLong id = new AtomicLong(0);

	private final String prefix;

	public PipelineLogger(Class<?> clazz, Logger parent) {
		super("ROGM", null, parent);
		prefix = "[" + clazz.getSimpleName() + '|' + id.getAndIncrement() + "] ";
	}

	@Override
	public void log(LogRecord record) {
		record.setMessage(this.prefix + record.getMessage());
		super.log(record);
	}
}
