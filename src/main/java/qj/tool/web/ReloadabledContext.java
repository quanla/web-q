package qj.tool.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import qj.util.ReflectUtil;
import qj.util.funct.Fs;
import qj.util.funct.P1;
import qj.util.lang.CompositeClassPathLoader;
import qj.util.lang.DirClassPathClassLoader;

public class ReloadabledContext {

	private String contextClassName;
	private String[] classpaths;
	private Object contextO;
	LinkedList<P1<Object>> afterCreateContext = new LinkedList<>();

	public ReloadabledContext(String contextClass, String... classpaths) {
		this.contextClassName = contextClass;
		this.classpaths = classpaths;
	}

	public void reload() {
		contextO = createContextObj();
		Fs.invokeAll(afterCreateContext, contextO);
	}

	private Object createContextObj() {
		Class<?> contextClass = new CompositeClassPathLoader(classpaths).load(contextClassName);
		return ReflectUtil.newInstance(contextClass);
	}

	public HttpServlet getServlet(final String servletName) {
		return new HttpServlet() {
			protected void service(HttpServletRequest req,
					HttpServletResponse resp) throws ServletException,
					IOException {
				if ("true".equals(req.getParameter("rc")) || contextO == null) {
					reload();
				}
				Field field = ReflectUtil.getField(servletName + "Servlet", contextO.getClass());
				((HttpServlet) ReflectUtil.getFieldValue(field, contextO)).service(req, resp);
			}
		};
	}

	public void invoke(final String methodName, Object... params) {
		afterCreateContext.add(obj -> {
			ReflectUtil.invoke(methodName, obj, params);
		});
	}
	@SuppressWarnings("UnusedDeclaration")
	public void setField(final String fieldName, Object value) {
		afterCreateContext.add(obj -> {
			ReflectUtil.setFieldValue(value, fieldName, obj);
		});
	}
	
}
