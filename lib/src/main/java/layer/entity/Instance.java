package layer.entity;

import layer.interfaces.HandelContainer;
import layer.layer.Logger;
import layer.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class Instance {
	public HashMap<String, Object> config = new HashMap<>();
	public HashMap<String, Layer> provide = new HashMap<>();
	public HashMap<String, Layer> instance = new HashMap<>();
	public ArrayList<Container> containers = new ArrayList<>();
	
	public Instance provide(Layer[] list) {
		for (var i : list) {
			var name = i.meta.get("name") == null ? i.getClass().getName() : i.meta.get("name") + "";
			provide.put(name, i);
		}
		return this;
	}
	
	public Instance instance(HandelContainer data) {
		var c = new Container(this);
		data.instanceCall(c);
		containers.add(c);
		return this;
	}
	
	public Instance instance(String name, HandelContainer data) {
		return instance(t -> {
			t.name(name);
			data.instanceCall(t);
		});
	}
	
	public Instance instance(String name, Layer data) {
		return instance(t -> {
			t.name(name);
			t.implement(data);
		});
	}
	
	public Instance instance(String name) {
		return instance(t -> t.name(name));
	}
	
	public Instance deploy() {
		var startTime = System.currentTimeMillis();
		containers.forEach(d -> {
			try {
				d.init();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		containers.forEach(d -> {
			try {
				d.deploy();
				if (deployInfo) Logger.log("Manager", "INFO", String.format("instance %s(abstract %s) %s deploy", d.name, d.layerName,d.description));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		if (deployInfo) Logger.log("Manager","INFO",String.format("deploy all use Σ = %sms",System.currentTimeMillis()-startTime));
		instance.forEach((name, data) -> {
			data.run();
		});

		return this;
	}
	
	public boolean deployInfo = false;
	
	public Instance deployInfo() {
		deployInfo = true;
		return this;
	}
}
