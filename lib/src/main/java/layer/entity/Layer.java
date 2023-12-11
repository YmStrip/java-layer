package layer.entity;

import layer.Interface.layer;
import layer.Interface.require;
import layer.Interface.config;

import java.util.HashMap;

public class Layer {
	public Layer() {
		var cls = this.getClass();
		var config = new HashMap<>();
		var require = new HashMap<>();
		var annotation = cls.getAnnotations();
		meta.put("name",this.getClass().getSimpleName());
		for (var i : annotation) {
			if (i instanceof layer layer) {
				var name = layer.name();
				var implement = layer.implement();
				if (name.isEmpty()) name = cls.getSimpleName();
				meta.put("name", name);
				if (!implement.isEmpty()) meta.put("implement", implement);
			}
		}
		var fields = cls.getDeclaredFields();
		for (var i : fields) {
			var fieldAnnotation = i.getAnnotations();
			//字段名称
			var name = i.getName();
			for (var j : fieldAnnotation) {
				if (j instanceof require req) {
					require.put(req.name().isEmpty() ? name : req.name(), name);
				}
				if (j instanceof config conf) {
					config.put(conf.name().isEmpty() ? name : conf.name(), name);
				}
			}
		}
		meta.put("require", require);
		meta.put("config", config);
	}
	
	public HashMap<String, Object> meta = new HashMap<>();
	
	public void run() {
	}
}
