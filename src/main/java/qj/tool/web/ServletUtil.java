package qj.tool.web;

import qj.util.IOUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Created by quan on 2/6/2015.
 */
public class ServletUtil {

    public static void proxy(HttpServletRequest req, HttpServletResponse resp, String host, int port) throws IOException {


        HttpURLConnection conn = (HttpURLConnection) new URL("http://"+ host+ ":" + port + req.getRequestURI() + (req.getQueryString() == null ? "" : "?" + req.getQueryString())).openConnection();
        conn.setRequestMethod(req.getMethod());
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String hName =  names.nextElement();

            if (!hName.equalsIgnoreCase("HOST")) {
                conn.addRequestProperty(hName, req.getHeader(hName));
            } else {
                conn.addRequestProperty("Host", host + ":" + port);
            }
        }
        if (!req.getMethod().equalsIgnoreCase("GET")) {
            conn.setDoOutput(true);
            IOUtil.connect(req.getInputStream(), conn.getOutputStream());
        }

        try {
            writeResponseHeaders(conn, resp);
            IOUtil.connect(conn.getInputStream(), resp.getOutputStream());
        } catch (IOException e) {
            writeResponseHeaders(conn, resp);
            IOUtil.connect(conn.getErrorStream(), resp.getOutputStream());
        }
    }

    private static void writeResponseHeaders(HttpURLConnection conn, HttpServletResponse resp) throws IOException {
        resp.setStatus(conn.getResponseCode());

        for (Map.Entry<String, List<String>> stringListEntry : conn.getHeaderFields().entrySet()) {
            if (stringListEntry.getKey()==null) {
                continue;
            }
            for (String val : stringListEntry.getValue()) {
                resp.addHeader(stringListEntry.getKey(), val);
            }
        }
    }
}
