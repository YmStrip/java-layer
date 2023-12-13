package layer.module;

import layer.annotations.Module;
import layer.entity.LayerContainer;
import layer.instance.InstanceFactory;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

@Module
public class LayerModule {
	public ArrayList<Object> controllers = new ArrayList<>();
	public ArrayList<Object> providers = new ArrayList<>();
	public ArrayList<Object> imports = new ArrayList<>();
	
	public LayerModule controller(Object... inc) {
		controllers.addAll(List.of(inc));
		return this;
	}
	
	public LayerModule provider(Object... inc) {
		providers.addAll(List.of(inc));
		return this;
	}
	public LayerModule imports (Object... inc) {
		imports.addAll(List.of(inc));
		return this;
	}
	public void handelContainer (LayerContainer c) {
	
	}
	public void handelInstance (InstanceFactory f) {
	
	}
	
}
