package qj.tool.web.model;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import qj.tool.web.model.ModelConfig.Action;
import qj.util.Cols;
import qj.util.DateUtil;
import qj.util.HttpUtil;
import qj.util.JsonUtil;
import qj.util.ReflectUtil;
import qj.util.RegexUtil;
import qj.util.funct.F1;
import qj.util.funct.P1;

@SuppressWarnings("rawtypes")
public class ModelServlet extends HttpServlet {
	private Persistence persistence;
	private ModelController config;
	
	protected void init(Persistence persistence,
			ModelController config) {
		this.persistence = persistence;
		this.config = config;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		Matcher matcher = RegexUtil.matcher("/model/([^/]+?)(?:/(.+))?", req.getRequestURI());
		matcher.matches();
		String type = matcher.group(1);
		String action = req.getParameter("action");
		String id = matcher.group(2);
		
		ModelConfig config = this.config.models.get(type);
		
		if ("".equals(action)) {
			;
		} else {
			HashMap actions = config.actions;
			final Action actionObj = (Action) actions.get(action);
			persistence.getAndSave(id, config.modelClass, new P1() {public void e(Object o) {
				try {
					if (actionObj.authenticate(req, o)) {
						actionObj.p1.e(o);
						resp.getWriter().write(JsonUtil.toJsonString(Cols.map("status", "success")));
					} else {
						resp.getWriter().write(JsonUtil.toJsonString(Cols.map("status", "error", "message", "Not authenticated")));
					}
				} catch (IOException e1) {
					throw new RuntimeException(e1.getMessage(), e1);
				}
			}});
		}
	}

	public static <M> Map toMapModel(ModelConfig<M> model) {
		LinkedHashMap<String, Map> ret = new LinkedHashMap<>();
		
		for (Entry<String, Field> entry : ReflectUtil.getAllFields(model.modelClass).entrySet()) {
			String fieldName = entry.getKey();
			ModelField fieldConfig = model.fields.get(fieldName);
			
			ret.put(fieldName, toMapField(entry.getValue(), fieldConfig));
		}
		return ret;
	}

	private static Map toMapField(Field field, ModelField fieldConfig) {
		LinkedHashMap ret = new LinkedHashMap();
		
		String typeName = typeName(field.getType());
		ret.put("type", typeName);
		if ("Map".equals(typeName)) {
			ret.put("valuesType", typeName(fieldConfig.valuesType));
		}
		
		if (fieldConfig != null) {
			if (fieldConfig.values != null) {
				ret.put("values", fieldConfig.values);
			}
			if (fieldConfig.required) {
				ret.put("required", true);
			}
			if (fieldConfig.maxLength != null) {
				ret.put("maxLength", fieldConfig.maxLength);
			}
			if (fieldConfig.trim) {
				ret.put("trim", true);
			}
			if (fieldConfig.displayFormat != null) {
				ret.put("displayFormat", fieldConfig.displayFormat);
			}
		}
		
		return ret;
	}

	private static String typeName(Class<?> type) {
		String typeName;
		if (type.equals(String.class)) {
			typeName = "String";
		} else if (Map.class.isAssignableFrom(type)) {
			typeName = "Map";
		} else if (type.equals(BigDecimal.class)) {
			typeName = "Number";
		} else {
			typeName = null;
		}
		return typeName;
	}

	public static <M> M req(HttpServletRequest req, ModelConfig<M> config) {
		final M newInstance = ReflectUtil.newInstance(config.modelClass);
		
		final Enumeration<String> parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			final Field field = ReflectUtil.getField(parameterName, config.modelClass);
			if (!parameterName.contains("[")) {
				if (field==null) {
					continue;
				}
				final ModelField modelField = config.fields.get(parameterName);
				ReflectUtil.setFieldValue(toValue(req.getParameter(parameterName), field.getType(), modelField), field, newInstance);
				
			} else {
				// contains [
			}
		}
		
		
		return newInstance;
		
	}

	private static <F> F toValue(String parameter, Class<?> type,
			ModelField modelField) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <M> M getData(HttpServletRequest req, ModelConfig<M> model) {
		F1<String, String> paramF = paramF(req);
		M m = ReflectUtil.newInstance(model.modelClass);
		for (Entry<String, ModelField> fieldEntry : model.fields.entrySet()) {
			String fName = fieldEntry.getKey();
			String param = paramF.e(fName);
			if (param == null) {
				continue;
			}
			
			param = param.trim();
			
			Object val = null;
			
			Field field = ReflectUtil.getField(fName, model.modelClass);
			if (field.getType().equals(String.class)) {
				val = param;
			} else if (field.getType().equals(Date.class)) {
				try {
					long time = Long.parseLong(param);
					val = new Date(time);
				} catch (NumberFormatException e1) {
					val = DateUtil.parse(param, new String[] {"yyyy-MM-dd'T'HH:mm:ss.S'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"}, TimeZone.getTimeZone("UTC"));
				}
			} else if (field.getType().equals(BigDecimal.class)) {
				val = new BigDecimal(param);
			}
			
			ReflectUtil.setFieldValue(val, field, m);
		}
		return m;
	}

	private static F1<String, String> paramF(HttpServletRequest req) {
		if (req.getHeader("Content-Type").startsWith("multipart/form-data")) {
			try {
				HashMap<String, byte[]> multipart = HttpUtil.getMultipart(RegexUtil.getString("boundary=(.+)", 1, req.getHeader("Content-Type")), req.getInputStream());
				return new F1<String,String>() {public String e(String name) {
					return new String(multipart.get(name));
				}};
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new F1<String, String>() {public String e(String fName) {
				return req.getParameter(fName);
			}};
		}
	}

	
}