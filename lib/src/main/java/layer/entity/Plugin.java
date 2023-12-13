package layer.entity;

import layer.interfaces.*;

import java.util.ArrayList;

public class Plugin {
	public ArrayList<HandelClassAnnotation> classAnnotation = new ArrayList<>();
	public ArrayList<HandelFieldAnnotation> fieldAnnotation = new ArrayList<>();
	
	//namespace.import.[name||field]
	public String requireName(LayerContainer l, FieldRequire r) {
		return null;
	}
	
	public String configName(LayerContainer l, FieldConfig c) {
		return null;
	}
	
	public String layerName(LayerContainer l) {
		return null;
	}
	
	//require-provider
	public Object require(LayerContainer l, String name, FieldRequire req) {
		return null;
	}
	
	public Object config(LayerContainer l, String name, FieldConfig f) {
		return null;
	}
	
	public void export(LayerContainer l) {
	}
}
