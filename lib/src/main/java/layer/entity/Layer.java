package layer.entity;

import layer.annotations.*;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Layer {
	//public String requirePrefix = "";
	//public String configPrefix = "";
	//public String namePrefix = "";
	
	//public String cleanName() {
	//	var name = (String) meta.get("name");
	//	return name.replace(namePrefix, "");
	//}
	
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
		//System.out.println(this.meta.get("name"));
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
		t.meta.put("name", name);
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
		setMetaName(t, tc, "");
		for (var i : annotation) {
			if (i instanceof LayerClass layer) {
				setMetaName(t, tc, layer.name());
			} else if (i instanceof Controller c) {
				meta.put("isController", true);
				//namePrefix = "controller.";
				//requirePrefix = "provider.";
				//setMetaName(t, tc, c.name());
			} else if (i instanceof Provider p) {
				meta.put("isProvider", true);
				//namePrefix = "provider.";
				//setMetaName(t, tc, p.name());
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
					getOrDefaultFieldRequire(i, req.name(), require);
				}
				//
				else if (j instanceof Config conf) {
					var configName = conf.name().isEmpty() ? name : conf.name();
					config.put(configName, new FieldConfig() {{
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
				//
				else if (j instanceof Import imp) {
					var a = getOrDefaultFieldRequire(i, "", require);
					a.importName = imp.name();
					a.isImport = true;
				}
			}
			i.setAccessible(iAccess);
		}
	}
	
	void init_inject() {
		init_inject(this, (Class<Layer>) this.getClass());
	}
	
	HashMap<String, FieldRequire> filedRequireMap = new HashMap<>();
	
	FieldRequire getOrDefaultFieldRequire(Field field, String setName, HashMap require) {
		if (filedRequireMap.get(field.getName()) != null) {
			return filedRequireMap.get(field.getName());
		}
		var reqName = getFieldName(field, setName);
		if (require.get(reqName) != null) {
			return (FieldRequire) require.get(reqName);
		}
		var a = createFieldRequire(field, setName);
		require.put(a.requireName, a);
		return a;
	}
	
	String getFieldName(Field field, String setName) {
		var reqName = field.getName();
		if (setName != null && !setName.isEmpty()) {
			reqName = setName;
		}
		return reqName;
	}
	
	FieldRequire createFieldRequire(Field field, String setName) {
		var reqName = getFieldName(field, setName);
		var f = new FieldRequire();
		f.fieldName = field.getName();
		f.ab = field.getType().getSimpleName();
		f.field = field;
		f.requireName = reqName;
		filedRequireMap.put(field.getName(), f);
		
		return f;
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
