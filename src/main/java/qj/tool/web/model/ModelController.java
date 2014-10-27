package qj.tool.web.model;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import qj.util.funct.F1;
import qj.util.funct.F3;

public class ModelController {

	HashMap<Class, FieldTypeConfig> fieldTypes = new HashMap<>();
	public F3<HttpServletRequest, String, Object, Boolean> authenticate;
	HashMap<String, ModelConfig> models = new HashMap<String,ModelConfig>();

	public <M> ModelConfig<M> newModelConfig(Class<M> modelClass) {
		ModelConfig<M> modelConfig = new ModelConfig<>(modelClass, this);
		this.models.put(modelConfig.modelClass.getSimpleName(), modelConfig);
		return modelConfig;
	}
	
	public <M> ModelConfig<M> getModel(Class<M> type) {
		return models.get(type.getSimpleName());
	}

	public <T> void fieldType(Class<T> clazz, F1<T, String> serializeF,
			F1<String, T> deserializeF) {
		FieldTypeConfig<T> fieldTypeConfig = new FieldTypeConfig<T>();
		fieldTypeConfig.serializeF = serializeF;
		fieldTypeConfig.deserializeF = deserializeF;
		fieldTypes.put(clazz, fieldTypeConfig);
	}
	
	static class FieldTypeConfig<T> {

		public F1<String, T> deserializeF;
		public F1<T, String> serializeF;
		
	}
}