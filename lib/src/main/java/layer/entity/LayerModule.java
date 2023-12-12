package layer.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class LayerModule {
	public LayerModule() {
		reInit();
	}
	
	public boolean deployInfo = false;
	
	public void deployInfo() {
		deployInfo = true;
	}
	
	public void reInit() {
		this.name = moduleName(this);
	}
	
	public String name;
	public Object[] controllers = new Object[]{};
	public Object[] providers = new Object[]{};
	public LayerModule[] imports = new LayerModule[]{};
	
	public void handelInstance(Instance ins) {
	}
	
	public void handelContainer(String name, Container container) {
	}
	
	static void initConfig(Object[] data, ArrayList<Layer> layer, HashMap<String, Object> map) {
		String LastName = null;
		for (var i : data) {
			if (i instanceof Layer l) {
				var name = l.meta.get("name");
				LastName = (String) name;
				layer.add(l);
			} else if (i instanceof HashMap<?, ?> h) {
				if (LastName == null) continue;
				map.put(LastName, h);
			}
		}
	}
	
	static String moduleName(LayerModule module) {
		String moduleName = null;
		var annotations = module.getClass().getAnnotations();
		for (var i : annotations) {
			if (i instanceof layer.annotations.Module m) {
				moduleName = m.name();
			}
		}
		if (moduleName == null || moduleName.isEmpty()) {
			moduleName = module.getClass().getSimpleName();
		}
		return moduleName;
	}
	
	public static Instance deploy(LayerModule module) {
		var instance = new Instance();
		var configs = new HashMap<String, Object>();
		var layers = new ArrayList<Layer>();
		var chRequire = new HashMap<String, String>();
		for (var i : module.imports) {
			var name = moduleName(i);
			var ins = deploy(i);
			//put - all - instance
			ins.instance.forEach((insName, insData) -> {
				if (!insName.contains("controller")) {
					chRequire.put(name + "." + insData.cleanName(), name + "." + insName);
				}
				instance.instance.put(name + "." + insName, insData);
			});
		}
		initConfig(module.providers, layers, configs);
		initConfig(module.controllers, layers, configs);
		var l = layers.toArray(new Layer[layers.size()]);
		for (var i : l) {
			instance.instance(t -> {
				//i.reInitClass();
				var name = (String) i.meta.get("name");
				i.reInit();
				t.implement(i);
				for (var j : l) {
					var jName = (String) j.meta.get("name");
					if (jName.contains("controller")) continue;
					t.require("provider." + j.cleanName());
				}
				t.require(chRequire);
				if (configs.get(name) != null) t.config((HashMap<String, Object>) configs.get(name));
				module.handelContainer(name, t);
			});
		}
		module.handelInstance(instance);
		if (module.deployInfo) instance.deployInfo();
		instance.deploy();
		return instance;
	}
}
