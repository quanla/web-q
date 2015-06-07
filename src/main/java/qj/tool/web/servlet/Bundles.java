package qj.tool.web.servlet;

import qj.util.Cols;

import java.util.List;

/**
 * Created by quan on 3/30/2015.
 */
public class Bundles {
    public static String serveCss(String content, String name, List<String> resources) {
        return content.replace("@Styles.Render(\"" + name + "\")", Cols.join(Cols.yield(resources, (r) -> "<link rel=\"stylesheet\" href=\"" + r.replaceFirst("^~", "") + "\"/>"), "\n"));
    }

    public static String serveJs(String content, String name, List<String> resources) {
        return content.replace("@Scripts.Render(\"" + name + "\")", Cols.join(Cols.yield(resources, (r) -> "<script type=\"text/javascript\" src=\"" + r.replaceFirst("^~", "") + "\"></script>"), "\n"));
    }
}
