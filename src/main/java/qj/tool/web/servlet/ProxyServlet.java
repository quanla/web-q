package qj.tool.web.servlet;

import qj.tool.web.ServletUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by quan on 8/13/2015.
 */
public class ProxyServlet extends HttpServlet {
    private final String apiHost;
    private final int port;

    public ProxyServlet(String apiHost, int port) {
        this.apiHost = apiHost;
        this.port = port;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletUtil.proxy(req, resp, apiHost, port);
    }
}
