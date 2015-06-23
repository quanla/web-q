package qj.tool.web.json;


import com.google.gson.*;
import qj.ui.DesktopUI4;
import qj.util.*;
import qj.util.funct.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonServlet extends HttpServlet {
    LinkedList<JsonAction> actions = new LinkedList<>();
    HashMap<Class<?>,F1<HttpServletRequest,Douce<?,P0>>> resources = new HashMap<>();

    {
        prepareResource(HttpServletRequest.class, (req) -> new Douce<>(req, null));
        prepareResource(HttpSession.class, (req) -> new Douce<>(req.getSession(false), null));
    }


    static Gson gson;
    static GsonBuilder gsonBuilder = new GsonBuilder();
    static {
        registerTypeSerializer(Date.class, new F1<Date,String>() {public String e(Date date) {
            return DateUtil.format(date, "yyyy-MM-dd'T'HH:mm:ss.S'Z'", TimeZone.getTimeZone("UTC"));
        }}, new F1<String,Date>() {public Date e(String obj) {
            return DateUtil.parse(obj, "yyyy-MM-dd'T'HH:mm:ss.S'Z'", TimeZone.getTimeZone("UTC"));
        }});

        gson = gsonBuilder.create();
    }
    public static <T> void registerTypeSerializer(Class<T> clazz, final F1<T,String> serializeF, final F1<String,T> deserializeF) {
        gsonBuilder.registerTypeAdapter(clazz, new JsonSerializer<T>() {public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(serializeF.e(src));
        }});
        gsonBuilder.registerTypeAdapter(clazz, new JsonDeserializer<T>() {public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return deserializeF.e(json.getAsJsonPrimitive().getAsString());
        }});
    }


    public void addAction(JsonAction action) {
        actions.add(action);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");

        if (Objects.equals(req.getMethod(), "OPTIONS")) {
            resp.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authen-type, Authen-Id, Authen-Username, Authen-Email");
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            resp.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE");
            return;
        }

        String requestURI = req.getRequestURI();
        String method = req.getMethod();
        for (JsonAction action : actions) {
            Map<String,String> matchedUrlParams;
            if (Objects.equals(action.method, method) && (matchedUrlParams = action.url.matchUrlParams(requestURI)) != null) {
                LinkedList<P0> afterExec = new LinkedList<>();
                ArrayList<Object> params = new ArrayList<>();
                for (JsonAction.RequestParam requestParam : action.requestParams) {
                    Douce<?,P0> param = requestParam.urlParamName == null ? createParam(req, gson, requestParam.clazz) :
                            new Douce<>(toType(requestParam.clazz, matchedUrlParams.get(requestParam.urlParamName)), null);
                    if (param.get2()!=null) {
                        afterExec.add(param.get2());
                    }
                    params.add(param.get1());
                }

                try {
                    Object ret = action.exec.e(params.toArray());

                    if (ret != null) {
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        PrintWriter writer = resp.getWriter();
                        gson.toJson(ret, writer);
                        writer.flush();
                    }
                } finally {
                    Fs.invokeAll(afterExec);
                }
                return;
            }
        }
        resp.sendError(404);
    }

    private <A> A toType(Class<A> clazz, String value) {
        if (clazz == String.class) {
            return (A) value;
        } else {
            return (A) Long.valueOf(value);
        }
    }

    private Douce<?,P0> createParam(HttpServletRequest req, Gson gson, Class<?> requestParam) throws IOException {
        F1<HttpServletRequest, Douce<?, P0>> resource = resources.get(requestParam);
        if (resource != null) {
            return resource.e(req);
        }
        return new Douce<>(gson.fromJson(new InputStreamReader(req.getInputStream()), requestParam), null);
    }

    @SuppressWarnings("unchecked")
    public <R> void prepareResource(Class<R> clazz, F1<HttpServletRequest, Douce<R,P0>> createResource) {
        F1 createResource1 = createResource;
        resources.put(clazz, createResource1);
    }

    public static void resolveActions(Class clazz, P1<JsonAction> p) {
        Url urlAnno = (Url) clazz.getAnnotation(Url.class);
        String url = urlAnno == null ? null : urlAnno.value();
        String method = clazz.getAnnotation(Post.class) != null ? "POST" :
                clazz.getAnnotation(Get.class) != null ? "GET" :
                clazz.getAnnotation(Put.class) != null ? "PUT" :
                clazz.getAnnotation(Delete.class) != null ? "DELETE" :
                        null;

        if (method != null) {
            p.e(createAction(ReflectUtil.getMethod("exec", clazz), clazz, url, method));
        } else {
            ReflectUtil.eachMethod(clazz, (m) -> {
                String httpMethod = m.getAnnotation(Post.class) != null ? "POST" :
                        m.getAnnotation(Get.class) != null ? "GET" :
                        m.getAnnotation(Put.class) != null ? "PUT" :
                        m.getAnnotation(Delete.class) != null ? "DELETE" :
                                null;

                if (httpMethod != null) {
                    p.e(createAction(m, clazz, url, httpMethod));
                }
                return false;
            });
        }
    }

    private static JsonAction createAction(Method m, Class clazz, String classUrl, String method) {
        JsonAction jsonAction = new JsonAction();
        Url urlAnno = (Url) m.getAnnotation(Url.class);
        jsonAction.url = parseUrlPattern(urlAnno == null ? classUrl : urlAnno.value());
        jsonAction.method = method;


        ArrayList<JsonAction.RequestParam> requestParams = new ArrayList<>();
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            Class<?> paramType = m.getParameterTypes()[i];
            Annotation[] annos = m.getParameterAnnotations()[i];
            UrlParam urlParam = (UrlParam) Cols.find(annos, (a) -> a.annotationType() == UrlParam.class);
            requestParams.add(new JsonAction.RequestParam(paramType, urlParam == null ? null : urlParam.value()));
        }
        jsonAction.requestParams = requestParams;

        jsonAction.response = m.getReturnType();
        jsonAction.exec = (params) -> {
            Object action = ReflectUtil.newInstance4(clazz);
            return ReflectUtil.invoke(m, action, params);
        };

        return jsonAction;
    }

    static class UrlPattern {

        public Pattern ptn;
        public LinkedList<String> names;


        private Map<String, String> matchUrlParams(String reqUrl) {
            Matcher matcher = ptn.matcher(reqUrl);
            if (!matcher.matches()) {
                return null;
            }

            HashMap<String, String> ret = new HashMap<>();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                ret.put(name, matcher.group(i+1));
            }
            return ret;
        }

    }

    private static UrlPattern parseUrlPattern(String url) {
        UrlPattern urlPattern = new UrlPattern();
        LinkedList<String> names = new LinkedList<>();
        urlPattern.ptn = Pattern.compile(RegexUtil.replaceAll(url, ":(\\w+)", (m) -> {
            names.add(m.group(1));
            return "([^/]+)";
        }));
        urlPattern.names = names;
        return urlPattern;
    }

}
