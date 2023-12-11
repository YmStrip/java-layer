package layer.entity;

import layer.annotations.LayerClass;
import layer.annotations.LowPriority;
import layer.annotations.Require;
import layer.annotations.Config;

import java.util.HashMap;

public class Layer {
	public Layer() {
		init_preset();
		init_class_annotation();
	}
	@LowPriority
	public void init_preset() {
		meta.clear();
		var config = new HashMap<>();
		var require = new HashMap<>();
		meta.put("require", require);
		meta.put("config", config);
	}
	@LowPriority
	public void init_class_annotation() {
		var cls = this.getClass();
		var annotation = cls.getAnnotations();
		for (var i : annotation) {
			if (i instanceof LayerClass layer) {
				var name = layer.name();
				var implement = layer.implement();
				if (name.isEmpty()) name = cls.getSimpleName();
				meta.put("name", name);
				if (!implement.isEmpty()) meta.put("implement", implement);
			}
		}
	}
	@LowPriority
	public void init_obj() {
		init_preset();
		init_class_annotation();
		var cls = this.getClass();
		var config = (HashMap) meta.get("config");
		var require = (HashMap) meta.get("require");
		meta.put("name", this.getClass().getSimpleName());
		var fields = cls.getDeclaredFields();
		for (var i : fields) {
			var iAccess = i.canAccess(this);
			i.setAccessible(true);
			var fieldAnnotation = i.getAnnotations();
			//字段名称
			var name = i.getName();
			var obj = this;
			for (var j : fieldAnnotation) {
				i.setAccessible(true);
				if (j instanceof Require req) {
					require.put(req.name().isEmpty() ? name : req.name(), name);
				}
				if (j instanceof Config conf) {
					var configName = conf.name().isEmpty() ? name : conf.name();
					config.put(configName, new ConfigField() {{
						fieldName = name;
						try {
							//System.out.println("v:" + configName + " " + i.get(obj));
							if (i.get(obj) != null) {
								required = false;
							}
						} catch (Exception e) {
						}
					}});
				}
			}
			i.setAccessible(iAccess);
		}
		
	}
	
	public HashMap<String, Object> meta = new HashMap<>();
	
	//run after all deploy
	@LowPriority
	public void run() {
	}
	
	//run after inject config (require maybe not deploy)
	@LowPriority
	public void setup() {
	
	}
}
