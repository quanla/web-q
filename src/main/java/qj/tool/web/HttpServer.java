package qj.tool.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import qj.util.funct.P0;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Quan on 20/03/2015.
 */
public class HttpServer {

	BatchServlet batchServlet = new BatchServlet();

	public P0 start(int port) throws Exception {
		final Server server = new Server(port);

		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContext.setContextPath("/");
		server.setHandler(servletContext);

		servletContext.addServlet(new ServletHolder(batchServlet), "/");

		server.start();

		return () -> {
            try {
                server.stop();
            } catch (Exception e) {
                ;
            }
        };
	}

	public HttpServer addServlet(String url, HttpServlet servlet) {
		batchServlet.addServlet(url, servlet);
		return this;
	}

	public static class BatchServlet extends HttpServlet {
		ArrayList<H> servlets = new ArrayList<>();
		void addServlet(String url, HttpServlet servlet) {
			servlets.add(new H(url, servlet));
			Collections.sort(servlets, new Comparator<H>() {
				public int compare(H o1, H o2) {
					return -1 * (o1.url.length() - o2.url.length());
				}
			});
			System.out.println(servlets);
		}

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String requestURI = req.getRequestURI();
			for (H h : servlets) {
				if (requestURI.startsWith(h.url)) {
					h.servlet.service(req, resp);
					return;
				}
			}
		}

		static class H {
			HttpServlet servlet;
			String url;

			public H(String url, HttpServlet servlet) {
				this.url = url;
				this.servlet = servlet;
			}
		}
	}
}
