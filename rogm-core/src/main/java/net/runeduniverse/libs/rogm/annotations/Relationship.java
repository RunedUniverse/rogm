package net.runeduniverse.libs.rogm.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Relationship {
	String label() default "";
	Direction direction() default Direction.OUTGOING;
}
