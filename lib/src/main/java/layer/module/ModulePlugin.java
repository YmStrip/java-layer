package layer.module;

import layer.annotations.Controller;
import layer.annotations.Import;
import layer.annotations.Provider;
import layer.entity.LayerContainer;
import layer.instance.InstanceFactory;
import layer.instance.InstancePlugin;

import java.util.ArrayList;

public class ModulePlugin extends InstancePlugin {
	public ModulePlugin() {
		this(new InstanceFactory());
	}
	
	public ModulePlugin(InstanceFactory fc) {
		super();
		super.factory = fc;
		classAnnotation.add((l, a) -> {
			//
			if (a instanceof Controller c) {
				l.meta.put("isController", true);
			}
			//
			else if (a instanceof Provider p) {
				l.meta.put("isProvider", true);
			}
			//
		});
		fieldAnnotation.add((l, f, a) -> {
			if (a instanceof Import imp) {
				var ch = l.defineRequire(f);
				ch.isImport = true;
				ch.importName = imp.name();
			}
		});
		//System.out.println(classAnnotation.size()+" "+fieldAnnotation.size());
	}
	
	@Override
	public String layerName(LayerContainer l) {
		var data = new ArrayList<String>();
		if (l.meta.get("module") != null) {
			if (((String) l.meta.get("module")).isEmpty()) {
				throw new Error("module is empty");
			}
			data.add((String) l.meta.get("module"));
		}
		data.add(super.layerName(l));
		return String.join(".", data);
	}
}
