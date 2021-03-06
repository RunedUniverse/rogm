package net.runeduniverse.libs.rogm.pattern;

import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.runeduniverse.libs.rogm.annotations.Converter;
import net.runeduniverse.libs.rogm.annotations.IConverter;
import net.runeduniverse.libs.rogm.annotations.Id;

@ToString(callSuper = true)
public class FieldPattern extends net.runeduniverse.libs.scanner.FieldPattern {

	protected final IStorage factory;
	@Getter
	@Setter
	protected IConverter<?> converter = null;

	public FieldPattern(IStorage factory, Field field) throws Exception {
		super(field);
		this.factory = factory;
		Converter converterAnno = this.field.getAnnotation(Converter.class);
		if (converterAnno == null) {
			if (this.field.isAnnotationPresent(Id.class))
				this.converter = IConverter.createConverter(null, field.getType());
		} else
			this.converter = IConverter.createConverter(converterAnno, field.getType());
	}
}
