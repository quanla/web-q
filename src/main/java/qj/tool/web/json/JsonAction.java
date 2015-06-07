package qj.tool.web.json;

import qj.util.funct.F1;

public class JsonAction {
        public String url;
        public String method;
        public Class<?>[] requestParams;
        public Class<?> response;
        public F1<Object[],Object> exec;
}
