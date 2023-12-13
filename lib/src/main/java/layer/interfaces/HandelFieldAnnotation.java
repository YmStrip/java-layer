package layer.interfaces;

import layer.entity.LayerContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@FunctionalInterface
public interface HandelFieldAnnotation {
	void call(LayerContainer cls, Field field, Annotation an);
}
