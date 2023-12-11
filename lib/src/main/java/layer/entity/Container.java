package layer.entity;

import java.util.HashMap;

public class Container {
	public Container(Instance group) {
		this.group = group;
	}
	
	public void name(String name) {
		this.name = name;
		if (this.layerName == null) this.layerName = name;
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
			layer.init_obj();
		}
		
		if (name == null) name = (String) layer.meta.get("name");
		
		if (group.instance.get(name) != null) {
			throw new Exception("Instance exist " + name);
		}
		group.instance.put(name, layer);
	}
	
	public void deploy() throws Exception {
		//deploy config
		var configReq = (HashMap<String, ConfigField>) layer.meta.get("config");
		configReq.forEach((reqName, configField) -> {
			try {
				var sd = config.get(reqName);
				if (sd == null) {
					if (configField.required)
						throw new Exception(String.format("[Instance][%s] %s\nnot config %s, use config(\"%s\",...)", name, description, reqName, reqName));
					return;
				}
				var field = layer.getClass().getDeclaredField(configField.fieldName);
				field.setAccessible(true);
				field.set(layer, sd);
			} catch (Exception e) {
				try {
					throw new Exception(String.format("[Instance][%s] %s\n config %s error\n%s ", name, description, reqName, e));
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		var requireReq = (HashMap<String, Object>) layer.meta.get("require");
		requireReq.forEach((reqName, fieldName) -> {
			try {
				var sd = require.get(reqName);
				if (sd == null) {
					throw new Exception(String.format("[Instance][%s] %s\nnot require %s, use require(\"%s\",string)", name, description, reqName, reqName));
				}
				var sdData = group.instance.get(sd);
				if (sdData == null) {
					throw new Exception(String.format("[Instance][%s] %s\nrequire (%s,%s) undefined", name, description, reqName, sd));
				}
				var field = layer.getClass().getDeclaredField((String) fieldName);
				field.setAccessible(true);
				field.set(layer, sdData);
			} catch (Exception e) {
				try {
					throw new Exception(String.format("[Instance][%s] %s\n require %s error\n%s ", name, description, reqName, e));
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
