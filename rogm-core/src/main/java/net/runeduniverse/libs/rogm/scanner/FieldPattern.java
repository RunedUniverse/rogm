package net.runeduniverse.libs.rogm.scanner;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import lombok.Data;

@Data
public class FieldPattern {

	protected final Field field;
	protected final Class<?> type;
	protected final boolean collection;

	public FieldPattern(Field field) {
		this.field = field;
		this.field.setAccessible(true);
		Class<?> clazz = this.field.getType();
		this.collection = Collection.class.isAssignableFrom(clazz);
		if (this.collection)
			clazz = (Class<?>) ((ParameterizedType) this.field.getGenericType()).getActualTypeArguments()[0];
		this.type = clazz;
	}

	public void setValue(Object entity, Object value) throws IllegalArgumentException {
		if (entity == null)
			return;
		try {
			this.field.set(entity, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void putValue(Object entity, Object value) throws IllegalArgumentException {
		if (entity == null)
			return;
		try {
			if (this.collection) {
				((Collection<Object>) this.field.get(entity)).add(value);
				return;
			}
			this.field.set(entity, value);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Object getValue(Object entity) throws IllegalArgumentException {
		if (entity == null)
			return null;
		try {
			return this.field.get(entity);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void removeValues(Object entity, Collection<Object> deletedEntities) {
		if (entity == null || deletedEntities == null)
			return;
		try {
			if (this.collection) {
				((Collection<Object>) this.field.get(entity)).removeAll(deletedEntities);
				return;
			}
			this.field.set(entity, Number.class.isAssignableFrom(this.type) ? 0 : null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void clearValue(Object entity) {
		if (entity == null)
			return;
		try {
			if (this.collection) {
				((Collection<Object>) this.field.get(entity)).clear();
				return;
			}
			this.field.set(entity, Number.class.isAssignableFrom(this.type) ? 0 : null);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
