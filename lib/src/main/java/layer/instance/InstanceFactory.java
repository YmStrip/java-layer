package layer.instance;

import layer.entity.LayerContainer;
import layer.entity.Plugin;
import layer.extend.Layer;
import layer.interfaces.HandelContainer;
import layer.layer.Logger;
import layer.util.Util;

import java.util.HashMap;
import java.util.Objects;

public class InstanceFactory {
	public Plugin plugin = new InstancePlugin(this);
	public String name = "instance";
	//include
	public HashMap<String, LayerContainer> containers = new HashMap<>();
	//提供的模板
	public HashMap<String, Object> providers = new HashMap<>();
	public boolean deployInfo = false;
	
	public InstanceFactory deployInfo() {
		deployInfo = true;
		return this;
	}
	
	void include(String name, Object data) {
		if (providers.get(name) != null) {
			throw new Error(String.format("[Instance][%s] includes exist", name));
		}
		providers.put(name, data);
	}
	
	Layer getInclude(String name) {
		if (providers.get(name) == null)
			throw new Error(String.format("[Instance] %s undefined use include(\"%s\")", name, name));
		var obj = providers.get(name);
		//
		if (obj instanceof Class<?> cls) {
			try {
				var l = cls.getDeclaredConstructor().newInstance();
				return (Layer) l;
			} catch (Exception e) {
				throw new Error(String.format("[Instance][%s] constructor error\n%s", cls.getSimpleName()), e);
			}
		}
		//
		else if (obj instanceof Layer l) {
			return l;
		} else {
			throw new Error(String.format("[Instance][%s] unknown object", obj.getClass().getSimpleName()));
		}
	}
	
	public InstanceFactory include(Object... data) {
		for (var is : data) {
			//
			if (is instanceof Class<?> i) {
				try {
					//使用container 计算提供者名称
					var l = (Layer) Util.newInstance(i);
					var c = new LayerContainer(plugin, l);
					include(c.name(), l);
				} catch (Exception e) {
					throw new Error(String.format("[Instance][%s] constructor error\n%s", i.getSimpleName(), e));
				}
			}
			//
			else if (is instanceof Layer i) {
				var m = new LayerContainer(plugin, i).name();
				include(m, i);
			}
		}
		return this;
	}
	
	public InstanceFactory instance(String name) {
		return instance(name, c -> {
			var ins = getInclude(name);
			c.implement(ins);
		});
	}
	public InstanceFactory instance(String name,Layer l) {
		return instance(name,t->{
			t.implement(l);
		});
	}
	
	public InstanceFactory instance(Class<?> cls) {
		return instance((Layer) Util.newInstance(cls));
	}
	
	public InstanceFactory instance(Class<?> cls, HandelContainer h) {
		return instance((Layer) Util.newInstance(cls), h);
	}
	
	public InstanceFactory instance(Layer layer) {
		return instance(layer, (e) -> {
		});
	}
	
	public InstanceFactory instance(Layer layer, HandelContainer c) {
		var name = new LayerContainer(plugin, layer).name();
		return instance(name, e->{
			e.implement(layer);
			c.instanceCall(e);
		});
	}
	
	public InstanceFactory instance(String name, String prov) {
		return instance(name, c -> {
			var ins = getInclude(prov);
			c.implement(ins);
		});
	}
	
	public InstanceFactory instance(String name, HandelContainer c) {
		var def = providers.get(name);
		var container = new LayerContainer(plugin);
		if (def!=null) container.implement(getInclude(name));
		c.instanceCall(container);
		if (container.layer==null) {
			System.out.println(container);
			throw new Error(String.format("[Instance] not a such layer %s",name));
		}
		container.reInit();
		containers.put(name, container);
		return this;
	}
	
	public InstanceFactory deploy() {
		var time = System.currentTimeMillis();
		containers.forEach((name, data) -> {
			if (deployInfo) data.deployInfo();
			data.deploy_init();
		});
		containers.forEach((name, data) -> {
			data.deploy_inject();
		});
		containers.forEach((name, data) -> {
			data.deploy_run();
		});
		if (deployInfo) Logger.log("Instance", "DEPLOY", String.format("use Σ = %sms", System.currentTimeMillis() - time));
		return this;
	}
}
