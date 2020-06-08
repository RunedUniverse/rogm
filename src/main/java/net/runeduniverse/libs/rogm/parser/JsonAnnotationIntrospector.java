package net.runeduniverse.libs.rogm.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude.Value;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

import net.runeduniverse.libs.rogm.annotations.Id;
import net.runeduniverse.libs.rogm.annotations.Property;
import net.runeduniverse.libs.rogm.annotations.Relationship;
import net.runeduniverse.libs.rogm.annotations.RelationshipEntity;
import net.runeduniverse.libs.rogm.annotations.Transient;

public class JsonAnnotationIntrospector extends NopAnnotationIntrospector {
	private static final long serialVersionUID = 1L;

	@Override
	public Value findPropertyInclusion(Annotated a) {
		return _isProperty(a);
	}

	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		return _isTransient(m) || _isId(m) || _isRelationship(m) || _isRelationshipEntity(m);
	}

	private JsonInclude.Value _isProperty(Annotated a) {
		Property anno = _findAnnotation(a, Property.class);
		if (anno == null)
			return JsonInclude.Value.empty();

		return JsonInclude.Value.empty().withValueInclusion(Include.ALWAYS).withContentInclusion(Include.ALWAYS)
				.withValueFilter(Void.class).withContentFilter(Void.class);
	}

	private boolean _isTransient(Annotated a) {
		Transient anno = _findAnnotation(a, Transient.class);
		if (anno == null)
			return false;
		return anno.value();
	}

	private boolean _isId(Annotated a) {
		Id anno = _findAnnotation(a, Id.class);
		if (anno == null)
			return false;
		return true;
	}

	private boolean _isRelationship(Annotated a) {
		Relationship anno = _findAnnotation(a, Relationship.class);
		if (anno == null)
			return false;
		return true;
	}

	private boolean _isRelationshipEntity(Annotated a) {
		RelationshipEntity anno = _findAnnotation(a, RelationshipEntity.class);
		if (anno == null)
			return false;
		return true;
	}

}
