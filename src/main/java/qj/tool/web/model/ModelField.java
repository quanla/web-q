package qj.tool.web.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ModelField {

	String name;
	boolean trim;
	boolean required;
	Class valuesType;
	List<Object> values;
	String displayFormat;
	Integer maxLength;

	public ModelField(String name) {
		this.name = name;
	}

	public ModelField required() {
		required = true;
		return this;
	}

	public ModelField trim() {
		trim = true;
		return this;
	}

	public ModelField valuesType(Class<?> valuesType) {
		this.valuesType = valuesType;
		return this;
	}

	public ModelField values(Object... values) {
		this.values = Arrays.asList(values);
		return this;
	}

	public ModelField displayFormat(String displayFormat) {
		this.displayFormat = displayFormat;
		return this;
	}

	public ModelField maxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}
	
}
