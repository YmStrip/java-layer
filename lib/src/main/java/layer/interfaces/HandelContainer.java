package layer.interfaces;

import layer.entity.Container;

@FunctionalInterface
public interface HandelContainer {
	void instanceCall(Container c);
}
