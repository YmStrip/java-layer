package layer.entity;

import layer.layer.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//更加离谱的模块,支持循环模块和循环依赖
public class MInstance {
	public boolean deployInfo = false;
	
	public MInstance deployInfo() {
		deployInfo = true;
		return this;
	}
	
	public HashMap<String, Instance> instanceList = new HashMap();
	public HashMap<String, LayerModule> module = new HashMap<>();
	public ArrayList<LayerModule> includes = new ArrayList<>();
	
	LayerModule getModule(Object data) {
		if (data instanceof String) {
			return module.get(data);
		} else if (data instanceof LayerModule l) {
			return l;
		}
		throw new Error(String.format("[Module][%s] not a such module", data));
	}
	
	MInstance deploy_init() {
		includes.forEach((m) -> {
			var name = m.getName();
			if (instanceList.get(name) != null) {
				throw new Error(String.format("[Module][%s] exist!", name));
			}
			var ins = new Instance();
			m.init();
			ins.module = m;
			m.layers.forEach(ins::provide);
			m.layers.forEach(e -> {
				//System.out.println(e.meta);
				//System.out.println(e.meta.get("name"));
				ins.instance((String) e.meta.get("name"), e);
			});
			instanceList.put(name, ins);
		});
		//instanceList.forEach((name, d) -> {
		//	d.init();
		//});
		return this;
	}
	
	MInstance deploy_implement() {
		//implement-child
		instanceList.forEach((name, data) -> {
			data.module.handelInstance(data);

			for (var childModule_ : data.module.imports) {
				var childModule = getModule(childModule_);
				childModule.includes.forEach(d -> {
					if (!(d instanceof Layer layer)) return;
					//模块包含的对象名称
					var expName = layer.meta.get("name");
					//注册到父实例
					data.instance.put(childModule.getName() + "." + expName, layer);
				});
			}
		});
		return this;
	}
	
	MInstance deploy_require() {
		//循环依赖
		instanceList.forEach((name, data) -> {
			data.containers.forEach(d -> {
				data.module.handelContainer(d.name,d);
				var reqList = (HashMap<String, FieldRequire>) d.layer.meta.get("require");
				reqList.forEach((reqName, reqField) -> {
					if (reqField.isImport) {
						d.require(reqName, reqField.importName + "." + reqName);
					} else {
						d.require(reqName, reqName);
					}
				});
			});
		});
		return this;
	}
	
	MInstance deploy_finish() {
		//dump
		instanceList.forEach((name, data) -> {
			if (deployInfo) data.deployInfo();
			data.deploy();
		});
		return this;
	}
	
	public MInstance module(LayerModule... data) {
		for (var i : data) {
			if (!i.imports.isEmpty()) {
				i.imports.forEach(d -> {
					if (d instanceof LayerModule lm) {
						includes.add(lm);
					}
				});
				includes.add(i);
			}
		}
		return this;
	}
	
	public MInstance deploy() {
		var startTime = System.currentTimeMillis();
		deploy_init();
		deploy_implement();
		deploy_require();
		deploy_finish();
		if (deployInfo) {
			Logger.log("MI", "INFO", String.format("deploy success use Σ = %sms", System.currentTimeMillis() - startTime));
		}
		return this;
	}
}
