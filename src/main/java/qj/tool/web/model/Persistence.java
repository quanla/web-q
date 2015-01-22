package qj.tool.web.model;

import qj.util.funct.P1;

public interface Persistence {
	<M> void getAndSave(String id, Class<M> clazz, P1<M> p1);
}