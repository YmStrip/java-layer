package layer.entity;

import layer.annotations.*;

import java.util.HashMap;

public class Layer {
	public String requirePrefix = "";
	public String configPrefix = "";
	public String namePrefix = "";
	
	public String cleanName() {
		var name = (String) meta.get("name");
		return name.replace(namePrefix, "");
	}
	
	public Layer() {
		reInitClass();
	}
	
	public void reInitClass() {
		init_preset();
		init_class_annotation();
	}
	
	public void reInit() {
		init_preset();
		init_class_annotation();
		init_inject();
	}
	
	void init_preset(Layer t) {
		t.meta.clear();
		var config = new HashMap<>();
		var require = new HashMap<>();
		t.meta.put("require", require);
		t.meta.put("config", config);
	}
	
	void init_preset() {
		init_preset(this);
	}
	
	void setMetaName(Layer t, Class<Layer> tc, String name) {
		if (name == null || name.isEmpty()) {
			name = tc.getSimpleName();
		}
		t.meta.put("name", namePrefix + name);
	}
	
	void setMetaName(String name) {
		setMetaName(this, (Class<Layer>) this.getClass(), name);
	}
	
	void init_class_annotation(Layer t, Class<Layer> tc) {
		if (tc.getSuperclass() != null) {
			init_class_annotation(t, (Class<Layer>) tc.getSuperclass());
		}
		var cls = t.getClass();
		var annotation = cls.getAnnotations();
		for (var i : annotation) {
			if (i instanceof LayerClass layer) {
				setMetaName(t, tc, layer.name());
			} else if (i instanceof Controller c) {
				namePrefix = "controller.";
				requirePrefix = "provider.";
				setMetaName(t, tc, c.name());
			} else if (i instanceof Provider p) {
				namePrefix = "provider.";
				setMetaName(t, tc, p.name());
				//requirePrefix = "provider";
			}
		}
	}
	
	void init_class_annotation() {
		init_class_annotation(this, (Class<Layer>) this.getClass());
	}
	
	void init_inject(Layer t, Class<Layer> tc) {
		//System.out.println(t+" "+tc.getSuperclass());
		var sup = tc.getSuperclass();
		if (sup != null) {
			init_inject(t, (Class<Layer>) tc.getSuperclass());
		}
		//init_preset(t);
		init_class_annotation(t, tc);
		var config = (HashMap) t.meta.get("config");
		var require = (HashMap) t.meta.get("require");
		var fields = tc.getDeclaredFields();
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
					var requireName = req.name().isEmpty() ? name : req.name();
					require.put(t.requirePrefix + requireName, new FieldRequire() {{
						field = i;
						fieldName = name;
						ab = i.getType().getSimpleName();
					}});
				}
				if (j instanceof Config conf) {
					var configName = conf.name().isEmpty() ? name : conf.name();
					config.put(t.configPrefix + configName, new FieldConfig() {{
						field = i;
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
	
	void init_inject() {
		init_inject(this, (Class<Layer>) this.getClass());
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
