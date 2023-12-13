package layer.interfaces;

import layer.entity.LayerContainer;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface HandelClassAnnotation {
	void call(LayerContainer cls, Annotation an);
}
