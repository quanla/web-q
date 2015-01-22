package qj.tool.web.model;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import qj.util.funct.F2;
import qj.util.funct.P1;

public class ModelConfig<M> {
	public Class<M> modelClass;
	HashMap<String, Action> actions = new HashMap<>();
	private ModelController mom;
	private String writeRequiredRight;
	Map<String, ModelField> fields = new HashMap<>();
	private P1<M> onCreate;

	public ModelConfig(Class<M> modelClass, ModelController mom) {
		this.modelClass = modelClass;
		this.mom = mom;
	}

	public void simpleEdit(String actionName, P1<M> edit) {
		actions.put(actionName, new Action(edit, new F2<HttpServletRequest, M,Boolean>() {public Boolean e(HttpServletRequest req, M model) {
			return mom.authenticate.e(req, writeRequiredRight, model);
		}}));
	}

	public void writeRequire(String requiredRight) {
		this.writeRequiredRight = requiredRight;
	}
	
	class Action {
		P1<M> p1;
		F2<HttpServletRequest, M,Boolean> authenticate;

		public Action(P1<M> p1, F2<HttpServletRequest, M,Boolean> authenticate) {
			this.p1 = p1;
			this.authenticate = authenticate;
		}

		public boolean authenticate(HttpServletRequest req, M target) {
			return authenticate==null || authenticate.e(req, target);
		}
	}

	public ModelField field(String name) {
		ModelField field = new ModelField(name);
		fields.put(name, field);
		return field;
	}

	public void onCreate(P1<M> onCreate) {
		this.onCreate = onCreate;
	}
}