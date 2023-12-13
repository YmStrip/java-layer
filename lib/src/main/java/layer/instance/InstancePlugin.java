package layer.instance;

import layer.annotations.*;
import layer.entity.FieldConfig;
import layer.entity.FieldRequire;
import layer.entity.LayerContainer;
import layer.entity.Plugin;

import java.util.ArrayList;

public class InstancePlugin extends Plugin {
	public InstancePlugin() {
		this(new InstanceFactory());
	}
	
	public InstanceFactory factory;
	
	public InstancePlugin(InstanceFactory factory) {
		this.factory = factory;
		//class
		classAnnotation.add((l, a) -> {
			if (a instanceof LayerClass layer) {
				l.setName(l.layer.getClass(), layer.name());
			} else if (a instanceof NameSpace n) {
				l.meta.put("nameSpace", n.name());
			}
		});
		//field
		fieldAnnotation.add((l, f, a) -> {
			if (a instanceof Require req) {
				var ch = l.defineRequire(f);
				if (!req.name().isEmpty()) ch.requireName = req.name();
			}
			//
			else if (a instanceof Config conf) {
				var ch = l.defineConfig(f);
				if (!conf.name().isEmpty()) ch.configName = conf.name();
				try {
					if (f.get(l.layer) != null) {
						ch.required = false;
					}
				} catch (Exception e) {
				}
			}
		});
	}
	
	@Override
	public String requireName(LayerContainer l, FieldRequire req) {
		var data = new ArrayList<String>();
		if (req.isImport) {
			if (req.importName.isEmpty()) throw new Error("import name is empty");
			data.add(req.importName);
		}
		if (!req.requireName.isEmpty())
			data.add(req.requireName);
		else
			data.add(req.fieldName);
		return String.join(".", data);
	}
	
	@Override
	public String configName(LayerContainer l, FieldConfig c) {
		var data = new ArrayList<String>();
		if (!c.configName.isEmpty()) {
			data.add(c.configName);
		} else {
			data.add(c.fieldName);
		}
		return String.join(".", data);
	}
	
	@Override
	public String layerName(LayerContainer l) {
		var data = new ArrayList<String>();
		if (l.meta.get("namespace") != null) {
			if (((String) l.meta.get("namespace")).isEmpty()) throw new Error("namespace is empty");
			data.add((String) l.meta.get("namespace"));
		}
		data.add((String) l.meta.get("name"));
		return String.join(".", data);
	}
	//require
	
	@Override
	public Object require(LayerContainer l, String name, FieldRequire req) {
		if (factory.containers.get(name) == null) return null;
		return factory.containers.get(name).layer;
	}
}
