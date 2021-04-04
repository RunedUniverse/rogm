package net.runeduniverse.libs.rogm.scanner;

import java.lang.annotation.Annotation;

public class TypeAnnotationScanner<F extends FieldPattern, M extends MethodPattern, T extends TypePattern<F, M>>
		extends TypeScanner<F, M, T> {

	private final Class<? extends Annotation> anno;

	public TypeAnnotationScanner(Class<? extends Annotation> anno, PatternCreator<F, M, T> creator,
			ResultConsumer consumer) {
		super(creator, consumer);
		this.anno = anno;
	}

	@Override
	public void scan(Class<?> type, ClassLoader loader, String pkg) {
		if (type.isAnnotationPresent(this.anno))
			super.scan(type, loader, pkg);
	}

	public static TypeAnnotationScanner<FieldPattern, MethodPattern, TypePattern<FieldPattern, MethodPattern>> DEFAULT(
			Class<? extends Annotation> anno, ResultConsumer consumer) {
		return new TypeAnnotationScanner<>(anno, TypeScanner::createPattern, consumer);
	}
}
