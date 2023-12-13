package layer.interfaces;

import layer.entity.LayerContainer;

@FunctionalInterface
public interface HandelContainer {
	void instanceCall(LayerContainer c);
}
