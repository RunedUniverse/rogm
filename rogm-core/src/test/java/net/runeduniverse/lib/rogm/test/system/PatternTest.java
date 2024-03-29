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
package net.runeduniverse.lib.rogm.test.system;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.NoArgsConstructor;
import net.runeduniverse.lib.rogm.pattern.APattern;
import net.runeduniverse.lib.rogm.pattern.FieldPattern;
import net.runeduniverse.lib.rogm.pattern.IBaseQueryPattern;
import net.runeduniverse.lib.rogm.test.AArchiveTest;
import net.runeduniverse.lib.rogm.test.model.Artist;
import net.runeduniverse.lib.rogm.test.model.Game;

@NoArgsConstructor
public class PatternTest extends AArchiveTest {

	@SuppressWarnings("rawtypes")
	public static Class<APattern> UNLOCKED_APATTERN_CLASS = APattern.class;
	public static Field UNLOCKED_APATTERN_ID_FIELD;
	public static final String APATTERN_ID_FIELD_NAME = "idFieldPattern";

	@BeforeAll
	@Tag("system")
	public static void prepare() throws Exception {
		for (Class<?> clazz = UNLOCKED_APATTERN_CLASS; clazz != Object.class; clazz = clazz.getSuperclass()) {
			unlockClass(clazz);
		}
		assertNotNull(UNLOCKED_APATTERN_ID_FIELD,
				"Field of name: \"" + APATTERN_ID_FIELD_NAME + "\" could not be collected from Class<APattern>");
		assertTrue(FieldPattern.class.isAssignableFrom(UNLOCKED_APATTERN_ID_FIELD.getType()),
				"collected Field of name: \"" + APATTERN_ID_FIELD_NAME + "\" is not of Class<FieldPattern>");
	}

	private static void unlockClass(Class<?> clazz) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if (f.getName()
					.equals(APATTERN_ID_FIELD_NAME))
				UNLOCKED_APATTERN_ID_FIELD = f;
		}
		for (Method m : clazz.getDeclaredMethods()) {
			m.setAccessible(true);
		}
	}

	@Test
	@Tag("system")
	public void existenceCheckArtist() throws Exception {
		IBaseQueryPattern<?> pattern = this.archive.getPattern(Artist.class, IBaseQueryPattern.class);
		if (pattern instanceof APattern<?>) {
			assertTrue(((APattern<?>) pattern).isValid(), "Object of Class<" + pattern.getClass()
					.getSimpleName() + "> NEVER got validated!");
			FieldPattern fieldPattern = (FieldPattern) UNLOCKED_APATTERN_ID_FIELD.get(pattern);
			assertNotNull(fieldPattern,
					"Field APattern<?>." + APATTERN_ID_FIELD_NAME + " is NULL in Object of Class<" + pattern.getClass()
							.getSimpleName() + ">");
		}
	}
	
	@Test
	@Tag("system")
	public void existenceCheckGame() throws Exception {
		IBaseQueryPattern<?> pattern = this.archive.getPattern(Game.class, IBaseQueryPattern.class);
		if (pattern instanceof APattern<?>) {
			assertTrue(((APattern<?>) pattern).isValid(), "Object of Class<" + pattern.getClass()
					.getSimpleName() + "> NEVER got validated!");
			FieldPattern fieldPattern = (FieldPattern) UNLOCKED_APATTERN_ID_FIELD.get(pattern);
			assertNotNull(fieldPattern,
					"Field APattern<?>." + APATTERN_ID_FIELD_NAME + " is NULL in Object of Class<" + pattern.getClass()
							.getSimpleName() + ">");
		}
	}
}
