package layer.entity;

import layer.annotations.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LayerModule {
	public String getName() {
		var name = getClass().getSimpleName();
		var annotations = getClass().getAnnotations();
		for (var i : annotations) {
			if (i instanceof Module m) {
				if (m.name().isEmpty()) continue;
				name = m.name();
			}
		}
		return name;
	}
	
	public String name;
	public ArrayList<Object> includes = new ArrayList<>();
	public ArrayList<Layer> layers = new ArrayList<>();
	public ArrayList<Object> imports = new ArrayList<>();
	public HashMap<String, Object> config = new HashMap<>();
	
	public LayerModule controller(Object... l) {
		includes.addAll(Arrays.asList(l));
		return this;
	}
	
	public LayerModule provider(Object... l) {
		includes.addAll(Arrays.asList(l));
		return this;
	}
	
	public LayerModule imports(Object... l) {
		imports.addAll(Arrays.asList(l));
		return this;
	}
	
	public void handelInstance(Instance ins) {
	}
	
	public void handelContainer(String name, Container container) {
	}
	
	public void init() {
		layers.clear();
		config.clear();
		String LastName = null;
		for (var i : includes) {
			if (i instanceof Layer l) {
				l.reInit();
				var name = l.meta.get("name");
				LastName = (String) name;
				layers.add(l);
			} else if (i instanceof HashMap<?, ?> h) {
				if (LastName == null) continue;
				config.put(LastName, h);
			}
		}
	}
	
	public boolean deployInfo = false;
	
	public void deployInfo() {
		deployInfo = true;
	}
	
	/*
	//支持require循环,不支持module循环
	public static Instance deploy(LayerModule module) {
		var instance = new Instance();
		var configs = new HashMap<String, Object>();
		var layers = new ArrayList<Layer>();
		var chRequire = new HashMap<String, String>();
		for (var is : module.imports) {
			if (! (is instanceof LayerModule i)) continue;
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
	
	//完全循环的module
	public static Instance extDeploy(LayerModule data) {
		//模块树展开
		var extList = new HashMap();
		return new Instance();
	}*/
}
