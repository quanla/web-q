package qj.tool.web.json;

import qj.util.funct.F1;

import java.util.List;

public class JsonAction {
    public JsonServlet.UrlPattern url;
    public String method;
    public List<RequestParam> requestParams;
    public Class<?> response;
    public F1<Object[], Object> exec;

    public static class RequestParam {
        Class<?> clazz;
        String urlParamName;

        public RequestParam(Class<?> clazz, String urlParamName) {
            this.clazz = clazz;
            this.urlParamName = urlParamName;
        }
    }
}
