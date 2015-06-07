package qj.tool.web.json;


import com.google.gson.Gson;
import qj.util.LangUtil;
import qj.util.ReflectUtil;
import qj.util.funct.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class JsonServlet extends HttpServlet {
    LinkedList<JsonAction> actions = new LinkedList<>();
    HashMap<Class<?>,F0<Douce<?,P0>>> resources = new HashMap<>();

    public void addAction(JsonAction action) {
        actions.add(action);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");

        if (Objects.equals(req.getMethod(), "OPTIONS")) {
            resp.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept");
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            return;
        }

        String requestURI = req.getRequestURI();
        String method = req.getMethod();
        for (JsonAction action : actions) {
            if (Objects.equals(action.url, requestURI) && Objects.equals(action.method, method)) {
                Gson gson = new Gson();

                LinkedList<P0> afterExec = new LinkedList<>();
                ArrayList<Object> params = new ArrayList<>();
                for (Class<?> requestParam : action.requestParams) {
                    Douce<?,P0> param = createParam(req, gson, requestParam);
                    if (param.get2()!=null) {
                        afterExec.add(param.get2());
                    }
                    params.add(param.get1());
                }
                Object ret = action.exec.e(params.toArray());

                Fs.invokeAll(afterExec);

                if (ret != null) {
                    resp.addHeader("Content-Type", "application/json");
                    PrintWriter writer = resp.getWriter();
                    gson.toJson(ret, writer);
                    writer.flush();
                }
                return;
            }
        }
        resp.sendError(404);
    }

    private Douce<?,P0> createParam(HttpServletRequest req, Gson gson, Class<?> requestParam) throws IOException {
        F0<Douce<?, P0>> resource = resources.get(requestParam);
        if (resource != null) {
            return resource.e();
        }
        return new Douce<>(gson.fromJson(new InputStreamReader(req.getInputStream()), requestParam), null);
    }

    @SuppressWarnings("unchecked")
    public <R> void prepareResource(Class<R> clazz, F0<Douce<R,P0>> createResource) {
        F0 createResource1 = createResource;
        resources.put(clazz, createResource1);
    }

    public static void resolveActions(String pkg, ClassLoader cl, P1<JsonAction> p) {
        LangUtil.eachClass(pkg, cl, (clazz) -> {
            String url = ((Url) clazz.getAnnotation(Url.class)).value();
            String method = clazz.getAnnotation(Post.class) != null ? "POST" :
                    clazz.getAnnotation(Get.class) != null ? "GET" :
                            null;


            if (method != null) {
                p.e(createAction(ReflectUtil.getMethod("exec", clazz), clazz, url, method));
            } else {
                ReflectUtil.eachMethod(clazz, (m) -> {
                    String httpMethod = m.getAnnotation(Post.class) != null ? "POST" :
                            m.getAnnotation(Get.class) != null ? "GET" :
                                    null;

                    if (httpMethod != null) {
                        p.e(createAction(m, clazz, url, httpMethod));
                    }
                    return false;
                });
            }
        });
    }

    private static JsonAction createAction(Method m, Class clazz, String url, String method) {
        JsonAction jsonAction = new JsonAction();
        jsonAction.url = url;
        jsonAction.method = method;



        jsonAction.requestParams = m.getParameterTypes();
        jsonAction.response = m.getReturnType();
        jsonAction.exec = (params) -> {
            Object action = ReflectUtil.newInstance4(clazz);
            return ReflectUtil.invoke(m, action, params);
        };

        return jsonAction;
    }

}
