package qj.tool.web.servlet;

import qj.util.Cols;
import qj.util.FileUtil;
import qj.util.StringUtil;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by quan on 3/30/2015.
 */
public class JsCollector {
    private String spaDir;
    private String layoutFile;
    private String urlPath;

    public JsCollector(String spaDir, String layoutFile, String urlPath) {
        this.spaDir = spaDir;
        this.layoutFile = layoutFile;
        this.urlPath = urlPath;
    }

    public String ensureJs(String content, String placeholder, String resourceRoot) {
        if (placeholder==null) {
            placeholder = "spa-js";
        }
        String startStr = "<!-- " + placeholder + " start -->";
        String endStr = "<!-- " + placeholder + " end -->";

        int start = content.indexOf(startStr) + startStr.length();
        if (start == startStr.length()-1) {
            return content;
        }
        int end = content.indexOf(endStr);

        String jsDeclarations = content.substring(start, end);

        LinkedList<String> jss = collectJsFiles(spaDir, resourceRoot, urlPath);
        String dec = "\r\n    " + Cols.join(Cols.yield(jss, (f) -> "<script src=\"" + f + "\"></script>"), "\r\n    ") + "\r\n    ";
        String newContent = content.substring(0, start) + dec + content.substring(end);

        if (!jsDeclarations.equals(dec)) {
            FileUtil.writeToFile(newContent, new File(layoutFile), "UTF-8");
        }
        return newContent;
    }

    public static LinkedList<String> collectJsFiles(String spaDir, String resourceRoot, String urlPath) {
        LinkedList<String> jss = new LinkedList<>();
        FileUtil.eachFile(new File(resourceRoot + "/" + spaDir), (file, path) -> {
            if (file.getName().endsWith(".js")) {
                jss.add((urlPath != null ? urlPath : "/") + spaDir + "/" + (StringUtil.isNotEmpty(path) ? path.replaceAll("\\\\", "/") + "/" : "") + file.getName());
            }
        });
        return jss;
    }
}
