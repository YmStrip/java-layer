package layer.interfaces;

import layer.entity.Container;
import layer.entity.Instance;

@FunctionalInterface
public interface HandelStore {
	void HandelContainer (Container c, Instance i);
}
