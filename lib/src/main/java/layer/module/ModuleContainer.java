package layer.module;

import layer.instance.InstanceFactory;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

public class ModuleContainer {
	public HashMap<String,Boolean> require = new HashMap();
	public ArrayList<String> child = new ArrayList<>();
	public LayerModule module;
	public InstanceFactory instance;
}
