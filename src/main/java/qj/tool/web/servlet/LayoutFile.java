package qj.tool.web.servlet;

import qj.util.Cols;
import qj.util.FileUtil;
import qj.util.funct.F1;
import qj.util.funct.P0;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by quan on 4/2/2015.
 */
public class LayoutFile {

    public String layoutFile;
    private String renderBodyFile;
    LinkedList<F1<String, String>> layoutChangers = new LinkedList<>();
    {
        layoutChangers.add((content) -> content.replace("@RenderBody()", renderBodyFile == null ? "" : FileUtil.readFileToString(renderBodyFile)));
    }

    public DotNetServlet parent;
    private String spaDir;
    private P0 beforeServe;

    public LayoutFile(String layoutFile) {
        this.layoutFile = layoutFile;
    }

    /**
     * Verify bundle + ensure <!-- spa-js start -->
     * @param spaDir
     * @return
     */
    public LayoutFile setSpaDir(String spaDir) {
        this.spaDir = spaDir;

        addSpaDir(spaDir, null);

        return this;
    }
    public LayoutFile addSpaDir(String spaDir, String placeholder) {
        return addSpaDir(spaDir, null, placeholder);
    }
    public LayoutFile addSpaDir(String spaDir, String urlPath, String placeholder) {
        JsCollector jsCollector = new JsCollector(spaDir, layoutFile, urlPath);
        layoutChangers.add(0, (content) -> jsCollector.ensureJs(content, placeholder, parent.resourceRoot));
        return this;
    }

    public void serve(HttpServletResponse resp) throws IOException {
        if (beforeServe != null) beforeServe.e();

        String content = FileUtil.readFileToString(new File(layoutFile), "UTF-8");

        for (F1<String, String> layoutChanger : layoutChangers) {
            content = layoutChanger.e(content);
        }

        content = content.replaceAll("@\\*(?:.|\r?\n)+?\\*@", "");

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(content);
    }

    public LayoutFile setRenderBody(String renderBodyFile) {
        this.renderBodyFile = renderBodyFile;
        return this;
    }

    public LayoutFile addHtmlPartial(String name, String file) {
        layoutChangers.add((content) -> content.replace("@Html.Partial(\"" + name + "\")", FileUtil.readFileToString(file)));
        return this;
    }

    public LayoutFile setBundleFile(String path) {

        layoutChangers.add((content) -> {
            BundleFile bundleFile = new BundleFile(path);
            bundleFile.scan();
            if (this.spaDir != null) {
                verifyJsBundle(this.spaDir, bundleFile.jsBundles, path);
            }
            for (Map.Entry<String, List<String>> css : bundleFile.cssBundles.entrySet()) {
                content = Bundles.serveCss(content, css.getKey(), css.getValue());
            }
            for (Map.Entry<String, List<String>> css : bundleFile.jsBundles.entrySet()) {
                content = Bundles.serveJs(content, css.getKey(), css.getValue());
            }
            return content;
        });

        return this;
    }

    private void verifyJsBundle(String spaDir, Map<String, List<String>> jsBundles, String bundlesFile) {
        HashSet<String> set = new HashSet<>();
        for (List<String> strings : jsBundles.values()) {
            set.addAll(strings);
        }

        LinkedList<String> missings = new LinkedList<>();
        JsCollector.collectJsFiles(spaDir, parent.resourceRoot, null).stream().filter(jsFile -> !set.contains(jsFile.replaceFirst("^/", ""))).forEach(missings::add);

        if (Cols.isNotEmpty(missings)) {
            System.err.println("Missing files in spa dir:\n\n" +
                            "                " + Cols.join(Cols.yield(missings, m -> "\"~" + m + "\""), ",\n                ")
            );
            System.err.println("Please add it to BundlesConfig file: " + bundlesFile);
            //  + " (copied to clipboard)"
//            Clipboard.copy(bundlesFile);
        }
    }

    public LayoutFile replace(String from, String to) {
        layoutChangers.add((content) -> content.replace(from, to));
        return this;
    }

    public LayoutFile beforeServe(P0 beforeServe) {
        this.beforeServe = beforeServe;
        return this;
    }
}
