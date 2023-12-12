package layer.entity;

import layer.layer.Logger;
import org.fusesource.jansi.Ansi;

import java.util.HashMap;

public class Container {
	public Container(Instance group) {
		this.group = group;
	}
	
	public void name(String name) {
		this.name = name;
		if (this.layerName == null) this.layerName = name;
	}
	
	public boolean deployInfo = false;
	
	public void deployInfo() {
		deployInfo = true;
	}
	
	public void description(String description) {
		this.description = description;
	}
	
	String name;
	String description = "no description";
	Layer layer;
	String layerName;
	Instance group;
	HashMap<String, String> require = new HashMap<>();
	HashMap<String, Object> config = new HashMap<>();
	
	public void implement(String name) {
		layerName = name;
		if (this.name == null) this.name = name;
	}
	
	public Container implement(Layer l) {
		layerName = (String) l.meta.get("name");
		if (name == null) name = layerName;
		layer = l;
		return this;
	}
	
	public Container config(String name, Object data) {
		config.put(name, data);
		return this;
	}
	
	public Container config(HashMap<String, Object> hash) {
		config.putAll(hash);
		return this;
	}
	
	public Container require(String name, String data) {
		require.put(name, data);
		return this;
	}
	
	public Container require(HashMap<String, String> hash) {
		require.putAll(hash);
		return this;
	}
	
	public Container require(String name) {
		require(name, name);
		return this;
	}
	
	public void init() throws Exception {
		if (layer == null) {
			if (group.provide.get(layerName) == null) {
				throw new Exception(String.format("[Instance][%s] %s\nimplement %s undefined\nuse provide(... include %s)", name, description, layerName, layerName));
			}
			layer = group.provide.get(layerName);
			if (layer == null) {
				throw new Exception(String.format("[Instance][%s] %s\nlayer is undefined, use implement(\"name\")", name, description));
			}
			layer.reInit();
		}
		
		if (name == null) name = (String) layer.meta.get("name");
		
		if (group.instance.get(name) != null) {
			throw new Exception("Instance exist " + name);
		}
		group.instance.put(name, layer);
	}
	
	public void deploy() throws Exception {
		//deploy config
		var configReq = (HashMap<String, FieldConfig>) layer.meta.get("config");
		configReq.forEach((reqName, fieldConfig) -> {
			try {
				var sd = config.get(reqName);
				if (sd == null) {
					if (fieldConfig.required)
						throw new Exception(String.format("[Instance][%s] %s\nnot config %s, use config(\"%s\",...)", name, description, reqName, reqName));
					return;
				}
				var field = fieldConfig.field;
				var canAccessible = field.canAccess(layer);
				field.setAccessible(true);
				field.set(layer, sd);
				field.setAccessible(canAccessible);
			} catch (Exception e) {
				try {
					throw new Exception(String.format("\n[Instance][%s] %s\n config %s error\n%s ", name, description, reqName, e));
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		var requireReq = (HashMap<String, FieldRequire>) layer.meta.get("require");
		requireReq.forEach((reqName, fieldRequire) -> {
			try {
				var instanceName = require.get(reqName);
				if (instanceName == null) {
					throw new Exception(String.format("\n[Instance][%s] require %s = <?> abstract %s undefined\nuse require(\"implement %s\")", name, fieldRequire.fieldName, fieldRequire.ab, fieldRequire.ab));
				}
				var sdData = group.instance.get(instanceName);
				if (sdData == null) {
					throw new Exception(String.format("\n[Instance][%s] require %s = <%s>implement %s abstract %s, but implement undefined\nuse instance(\"%s\")", name, fieldRequire.fieldName, instanceName, "?", fieldRequire.ab, instanceName));
				}
				var implementName = sdData.getClass().getSimpleName();
				var field = fieldRequire.field;
				var canAccessible = field.canAccess(layer);
				field.setAccessible(true);
				field.set(layer, sdData);
				field.setAccessible(canAccessible);
				Logger.log(Ansi.Color.YELLOW, Ansi.Color.WHITE, "Manager", "Require", String.format("[%s] %s = <%s>implement %s abstract %s", name, fieldRequire.fieldName, instanceName, implementName, fieldRequire.ab));
			} catch (Exception e) {
				try {
					var instanceName = require.get(reqName);
					var sd = require.get(reqName);
					var sdData = group.instance.get(instanceName);
					System.out.println(sdData);
					throw new Exception(String.format("\n[Instance][%s] require %s = implement %s abstract %s error\n%s", name, fieldRequire.fieldName, sd, fieldRequire.ab, e));
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		try {
			layer.setup();
		} catch (Exception e) {
			throw new Exception(String.format("[Instance][%s] %s\n setup error \n%s ", name, description, e));
		}
	}
}
