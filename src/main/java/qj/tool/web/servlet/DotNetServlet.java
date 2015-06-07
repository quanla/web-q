package qj.tool.web.servlet;

import qj.util.*;
import qj.util.funct.P1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Quan on 21/03/2015.
 */
public class DotNetServlet extends HttpServlet {
	public String resourceRoot;
    private Map<String,LayoutFile> layoutFiles = new HashMap<>();

	HashMap<String, P1<byte[]>> acceptPosts = new HashMap<>();
	private String urlPath;

	public DotNetServlet() {
		this.urlPath = "/";
	}
	public DotNetServlet(String urlPath) {
		this.urlPath = urlPath;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		String path = requestURI.replaceFirst("^" + urlPath.replaceFirst("/$", ""), "");
		if (layoutFiles.containsKey(path)) {
			layoutFiles.get(path).serve(resp);
		} else if (req.getMethod().equalsIgnoreCase("GET")) {
			serveStaticFile(resp, path);
        } else if (req.getMethod().equalsIgnoreCase("POST")) {
			P1<byte[]> acceptPost = acceptPosts.get(requestURI);
			if (acceptPost != null) {
				acceptPost.e(IOUtil.readEnough(req.getContentLength(), req.getInputStream()));
			}
		}
	}

	public DotNetServlet acceptPost(String url, P1<byte[]> action) {
		acceptPosts.put(url, action);
		return this;
	}

	private void serveStaticFile(HttpServletResponse resp, String path) throws IOException {
		File file = new File(resourceRoot + path);
		if (!file.exists()) {
            resp.sendError(404);
            return;
        }
		String contentType = ((String) Cols.map(
				"js", "application/javascript",
				"css", "text/css",
				"html", "text/html",
				"woff", "application/octet-stream",
				"woff2", "application/octet-stream",
				"ico", "image/vnd.microsoft.icon",
				"png", "image/png",
				"gif", "image/gif",
				"svg", "image/svg+xml",
				"jpg", "image/jpeg"
		).get(RegexUtil.getString(".+\\.(.*)$", 1, path).toLowerCase()));
		if (contentType == null) {
            System.out.println(path + "; " + RegexUtil.getString("(\\..*)$", 1, path) + "; " + contentType);
        }
		resp.setContentType(contentType);
		resp.setCharacterEncoding("UTF-8");
		if ("text/html".equals(contentType)) {

			resp.getWriter().write(serveIncludes(FileUtil.readFileToString(file)));
		} else {
			FileUtil.readFileOut(file, resp.getOutputStream());
		}
	}

	private String serveIncludes(String content) {
		//<!--#include virtual="/ssi/head.html" -->
		content = RegexUtil.replaceAll(content, "<!-- *#include +(?:virtual|file)=\"(.+)\" *-->", (m) -> {
			File fileToRead = new File(resourceRoot + m.group(1));
			if (!fileToRead.exists()) {
				System.err.println("Not found file to include: " + fileToRead);
				return "";
			}
			return serveIncludes(FileUtil.readFileToString(fileToRead));
		});
//		content = RegexUtil.replaceAll(content, "<!-- *#include +virtual=\"(.+)\" *-->", (m) -> FileUtil.readFileToString(resourceRoot + m.group(1)));
		return content;
	}


	public DotNetServlet setResourceRoot(String resourceRoot) {
		this.resourceRoot = resourceRoot;
		return this;
	}

	public static void main(String[] args) {
		System.out.println(RegexUtil.getString("(\\..*)$", 1, "/Areas/Management/app/css/selectboxit.css"));
	}

	public DotNetServlet addLayoutFile(String url, LayoutFile layoutFile) {
		layoutFiles.put(url, layoutFile);
		layoutFile.parent = this;
		return this;
	}
}
