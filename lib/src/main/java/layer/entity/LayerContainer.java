package layer.entity;

import layer.extend.Layer;
import layer.layer.Logger;
import org.fusesource.jansi.Ansi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class LayerContainer {
	public LayerContainer() {
		reInit();
	}
	
	public LayerContainer(Plugin plugin) {
		this.plugin = plugin;
		reInit();
	}
	
	public LayerContainer(Plugin plugin, Layer l) {
		this.plugin = plugin;
		this.layer = l;
		reInit();
	}
	
	
	HashMap<String, Object> defineRequire = new HashMap<>();
	HashMap<String, Object> defineConfig = new HashMap<>();
	//dump name
	public String outsideName;
	
	public String name() {
		return (String) meta.get("name");
	}
	
	;
	//封装class
	public Layer layer;
	public Plugin plugin;
	
	public boolean deployInfo = false;
	
	public LayerContainer deployInfo() {
		deployInfo = true;
		return this;
	}
	
	//meta
	public HashMap<String, Object> meta = new HashMap<>();
	HashMap<String, FieldRequire> require = new HashMap<>();
	HashMap<String, FieldConfig> config = new HashMap<>();
	HashMap<String, FieldRequire> requireD = new HashMap<>();
	HashMap<String, FieldConfig> configD = new HashMap<>();
	
	//重载信息
	public LayerContainer reInit() {
		init_preset();
		init_class_annotation();
		init_inject();
		return this;
	}
	
	String getOutsideName() {
		if (outsideName != null && !outsideName.isEmpty()) return outsideName;
		if (layer != null) return layer.getClass().getSimpleName();
		return "???";
	}
	
	public LayerContainer implement(Layer l) {
		layer = l;
		return this;
	}
	
	public LayerContainer plugin(Plugin p) {
		plugin = p;
		return this;
	}
	
	
	public void setName(Class<?> ts, String name) {
		if (name == null || name.isEmpty()) {
			name = ts.getSimpleName();
		}
		meta.put("name", name);
	}
	
	public void setName() {
		setName(layer.getClass(), "");
	}
	
	//require
	public FieldRequire defineRequire(Field field) {
		if (require.get(field.getName()) != null) return require.get(field.getName());
		var ch = new FieldRequire();
		ch.field = field;
		ch.ab = field.getType().getSimpleName();
		ch.fieldName = field.getName();
		ch.requireName = "";
		require.put(field.getName(), ch);
		return ch;
	}
	
	public FieldConfig defineConfig(Field field) {
		if (config.get(field.getName()) != null) return config.get(field.getName());
		var ch = new FieldConfig();
		ch.field = field;
		ch.fieldName = field.getName();
		ch.required = true;
		return ch;
	}
	
	public void init_preset() {
		require.clear();
		config.clear();
		requireD.clear();
		configD.clear();
		meta.clear();
		var config = new HashMap<>();
		var require = new HashMap<>();
		meta.put("require", require);
		meta.put("config", config);
	}
	
	//init
	public void init_class_annotation(Class<?> cls) {
		if (cls.getSuperclass() != null) {
			init_class_annotation(cls.getSuperclass());
		}
		var annotation = cls.getAnnotations();
		setName(cls, "");
		for (var i : annotation) {
			for (var j : plugin.classAnnotation) {
				try {
					j.call(this, i);
				} catch (Exception e) {
					var name = layer.getClass().getSimpleName();
					throw new Error(String.format("[Container][%s] init class Error\n%s", name, e));
				}
			}
		}
	}
	
	public void init_class_annotation() {
		if (layer == null) return;
		this.init_class_annotation(layer.getClass());
	}
	
	public void init_inject(Class<?> tc) {
		if (tc.getSuperclass() != null) {
			init_inject(tc.getSuperclass());
		}
		var fields = tc.getDeclaredFields();
		for (var i : fields) {
			var iAccess = i.canAccess(layer);
			i.setAccessible(true);
			var fieldAnnotation = i.getAnnotations();
			//字段名称
			var obj = this;
			for (var j : fieldAnnotation) {
				for (var k : plugin.fieldAnnotation) {
					try {
						k.call(this, i, j);
					} catch (Exception e) {
						var name = layer.getClass().getSimpleName();
						throw new Error(String.format("[Container][%s] init field(%s) Error\n%e", name, i.getName(), e));
					}
				}
			}
			i.setAccessible(iAccess);
		}
	}
	
	public void init_inject() {
		if (layer == null) return;
		init_inject(layer.getClass());
	}
	
	Object get_require(String name, FieldRequire r, HashMap<String, String> rec) {
		//循环引用没有实际定义的名称
		if (rec.get(name) != null) {
			var t = new ArrayList<String>();
			rec.forEach((a, b) -> t.add(a + " -> " + b));
			throw new Error(String.format("[Container][%s] require(\"%s\") circular reference error", getOutsideName(), String.join(",", t)));
		}
		if (defineRequire.get(name) == null) {
			return plugin.require(this, name, r);
		}
		var g = defineRequire.get(name);
		//name <-> 指向
		if (g instanceof String) {
			rec.put(name, (String) g);
			return get_require((String) g, r, rec);
		}
		return g;
	}
	
	Object get_config(String name, FieldConfig c) {
		Object d;
		if (defineConfig.get(name) != null) {
			d = defineConfig.get(name);
		} else {
			d = plugin.config(this, name, c);
		}
		if (c.required && d == null) {
			throw new Error(String.format("[Container][%s] config %s <- %s is null but it required", getOutsideName(), c.fieldName, name));
		}
		return c;
	}
	
	public void deploy_init() {
		if (layer == null) throw new Error("[Container] not include layer");
		try {
			outsideName = plugin.layerName(this);
			require.forEach((name, data) -> {
				//define require
				var dumpName = plugin.requireName(this, data);
				requireD.put(dumpName, data);
			});
			config.forEach((name, data) -> {
				var dumpName = plugin.configName(this, data);
				configD.put(dumpName, data);
			});
			//export
			plugin.export(this);
		} catch (Exception e) {
			throw new Error(String.format("[Container][%s] deploy_init error\n", getOutsideName(), e));
		}
	}
	
	public void deploy_inject() {
		configD.forEach((name, data) -> {
			try {
				var canAccess = data.field.canAccess(layer);
				data.field.setAccessible(true);
				data.field.set(layer, get_config(name, data));
				data.field.setAccessible(canAccess);
			} catch (Exception e) {
				throw new Error(String.format("[Container][%s] config(%s) error\n%s", getOutsideName(), name + " -> " + data.field.getName(), e));
			}
		});
		requireD.forEach((name, data) -> {
			try {
				var canAccess = data.field.canAccess(layer);
				data.field.setAccessible(true);
				var d = get_require(name, data, new HashMap<>());
				if (d == null)
					throw new Error(String.format("[Container][%s] require(\"%s\") undefined make sure provider it", name(), name));
				data.field.set(layer, d);
				data.field.setAccessible(canAccess);
				if (deployInfo) {
					Logger.log(Ansi.Color.YELLOW, Ansi.Color.WHITE, "Container", "Require", String.format("[%s] require %s (implement %s <- abstract %s)", getOutsideName(), data.fieldName, name, data.ab));
				}
			} catch (Exception e) {
				throw new Error(String.format("[Container][%s] require %s (implement %s <- abstract %s) error\n%s", getOutsideName(), data.fieldName, name, data.ab, e));
			}
		});
		try {
			layer.setup();
		} catch (Exception e) {
			throw new Error(String.format("[Container][%s] setup error\n%s", getOutsideName(), e));
		}
		if (deployInfo) Logger.log("Container", "INFO", String.format("[%s] implement %s", outsideName, name()));
	}
	
	public void deploy_run() {
		try {
			layer.run();
		} catch (Exception e) {
			throw new Error(String.format("[Container][%s] run error\n%s", getOutsideName(), e));
		}
	}
	
	public void deploy() {
		deploy_init();
		deploy_inject();
		deploy_run();
	}
	
	
	//req
	public LayerContainer require(HashMap<String, Object> data) {
		data.forEach(this::require);
		return this;
	}
	
	public LayerContainer require(String name, Object data) {
		defineRequire.put(name, data);
		return this;
	}
	
	public LayerContainer require(String name) {
		defineRequire.put(name, name);
		return this;
	}
	
	//config
	public LayerContainer config(String name, Object data) {
		defineConfig.put(name, data);
		return this;
	}
	
	public LayerContainer config(HashMap<String, Object> data) {
		data.forEach(this::config);
		return this;
	}
	
}
