package layer.module;

import layer.annotations.Module;
import layer.annotations.NameSpace;
import layer.entity.LayerContainer;
import layer.entity.Plugin;
import layer.extend.Layer;
import layer.instance.InstanceFactory;
import layer.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModuleFactory {
	public Plugin plugin = new ModulePlugin(new InstanceFactory());
	public ArrayList<LayerModule> includes = new ArrayList<>();
	public HashMap<String, ModuleContainer> modules = new HashMap<>();
	
	public ModuleFactory include(LayerModule... data) {
		includes.addAll(List.of(data));
		return this;
	}
	
	public boolean deployInfo = false;
	
	public ModuleFactory deployInfo() {
		deployInfo = true;
		return this;
	}
	
	String get_module_name(LayerModule m) {
		var an = m.getClass().getAnnotations();
		var data = new ArrayList<String>();
		var name = m.getClass().getSimpleName();
		var ns = "";
		for (var i : an) {
			if (i instanceof NameSpace ns_) {
				ns = ns_.name();
			}
			if (i instanceof Module mo) {
				name = mo.name();
			}
		}
		if (!ns.isEmpty()) {
			data.add(ns);
		}
		data.add(name);
		return String.join(",", data);
	}
	
	//计算所有包含的模块[包括递归的模块]
	
	
	void rec_init_list(LayerModule m) {
		//获取模块名称
		var name = get_module_name(m);
		if (modules.get(name) != null) return;
		//创建模块容器
		var container = new ModuleContainer() {{
			//设置模块容器的模块为当前
			module = m;
			//设置新的模块实例
			instance = new InstanceFactory();
		}};
		//重新配置插件
		container.instance.plugin = new ModulePlugin(container.instance);
		//设置
		if (deployInfo) container.instance.deployInfo();
		//模块事件
		m.handelInstance(container.instance);
		//添加
		modules.put(name, container);
		//获取子模块，为了遍历添加到模块列表
		for (var ch : m.imports) {
			//获取子模块名称
			if (ch instanceof String str) {
				container.child.add(str);
			}
			//
			else if (ch instanceof Class<?> cls) {
				var cm = Util.newInstance(cls);
				var cnm = get_module_name((LayerModule) cm);
				container.child.add(cnm);
				rec_init_list((LayerModule) cm);
			}
			//
			else if (ch instanceof LayerModule cm) {
				var cnm = get_module_name(cm);
				container.child.add(cnm);
				rec_init_list(cm);
			}
			//
			else throw new Error(String.format("[Module] unknown object\n%s", ch));
		}
	}
	
	public void init_list() {
		//清空模块列表
		modules.clear();
		//遍历添加includes
		for (var i : includes) {
			rec_init_list(i);
		}
	}
	
	public void init_implement() {
		//遍历模块列表，为了遍历添加实例
		modules.forEach((name, mc) -> {
			var m = mc.module;
			var h = new ArrayList<>();
			h.addAll(m.providers);
			h.addAll(m.controllers);
			h.forEach(d -> {
				if (d instanceof Layer l) {
					mc.instance.instance(l, m::handelContainer);
				}
				//
				else if (d instanceof Class<?> cls) {
					mc.instance.instance(cls, m::handelContainer);
				}
				//
				else throw new Error(String.format("[Module][%s] unknown include\n%s", get_module_name(m), d));
			});
		});
	}
	
	public void init_require() {
		//遍历模块列表，为了将当前模块的子模块的所有provider实例，添加到当前模块实例列表中作为child
		modules.forEach((moduleName, moduleContainer) -> {
			//储存应该添加的列表
			var addContainer = new HashMap<>();
			//遍历子模块字符串，为了进一步完成上一步的目的
			moduleContainer.child.forEach(childName -> {
				//获取对应的模块容器
				var childModuleContainer = modules.get(childName);
				//如果不存在，这种情况一般是因为 引入了不存在的字符串 导致的
				if (childModuleContainer == null) throw new Error(String.format("cannot find child module %s", childName));
				//成功获取，遍历子模块容器的所有layer容器
				//添加断点
				childModuleContainer.instance.containers.forEach((impName, impData) -> {
					//验证为提供，将当前layer容器，注册到addContainer
					if (impData.meta.get("isProvider") == null || !((Boolean) impData.meta.get("isProvider"))) return;
					addContainer.put(childName + "." + impName, impData);
				});
			});
			//现在执行最后的步骤，将任务列表put，并且添加一个tag，表示为require
			addContainer.forEach((name, data) -> {
				moduleContainer.require.put((String) name,true);
				moduleContainer.instance.containers.put((String) name, (LayerContainer) data);
			});
		});
	}
	
	public void init_deploy() {
		modules.forEach((moduleName, moduleContainer) -> {
			moduleContainer.instance.containers.forEach((name, data) -> {
				//统一fitter，将公共池不包含的layer容器排除
				if (moduleContainer.require.get(name)!=null) return;
				data.deploy_init();
			});
			moduleContainer.instance.containers.forEach((name, data) -> {
				//System.out.println(name+" "+moduleContainer.require.get(name));
				//统一fitter，将公共池不包含的layer容器排除
				if (moduleContainer.require.get(name)!=null) return;
				if(deployInfo)data.deployInfo();
				data.deploy_inject();
			});
		});
		modules.forEach((moduleName,moduleContainer)->{
			moduleContainer.instance.containers.forEach((name, data) -> {
				//统一fitter，将公共池不包含的layer容器排除
				if (moduleContainer.require.get(name)!=null) return;
				data.deploy_run();
			});
		});
	}
	
	public ModuleFactory deploy() {
		init_list();
		init_implement();
		init_require();
		init_deploy();
		return this;
	}
}
