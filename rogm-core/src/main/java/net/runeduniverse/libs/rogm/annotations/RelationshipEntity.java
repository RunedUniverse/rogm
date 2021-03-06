package net.runeduniverse.libs.rogm.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface RelationshipEntity {
	String label() default "";
	Direction direction() default Direction.OUTGOING;
	InterpretationMode mode() default InterpretationMode.IMPLICIT;
}
