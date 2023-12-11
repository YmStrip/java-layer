package layer.util;

import layer.entity.Instance;
import layer.entity.Layer;
import layer.interfaces.HandelStore;

public class Util {
	public static <T extends Layer> T store(T layer, HandelStore handelStore) {
		var instance = new Instance();
		instance.instance("_index_", t -> {
			t.implement(layer);
			handelStore.HandelContainer(t, instance);
		});
		return (T) instance.deploy().instance.get("_index_");
	}
	public static <T extends Layer> T only(T layer) {
		return store(layer,((c, i) -> {}));
	}
}
