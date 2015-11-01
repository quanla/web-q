package qj.tool.web.servlet;

import qj.util.Cols;
import qj.util.FileUtil;
import qj.util.RegexUtil;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by quan on 4/2/2015.
 */
public class BundleFile {
    private String path;
    Map<String, List<String>> cssBundles;
    Map<String, List<String>> jsBundles;

    public BundleFile(String path) {
        this.path = path;
    }

    public static void main(String[] args) {
        new BundleFile("d:\\Workon\\qj-svn\\commercial-apps\\prototype1\\GIE-PST\\PST.API\\App_Start\\BundleConfig.cs").scan();
    }

    public void scan() {
        String content = FileUtil.readFileToString(path);

        cssBundles = extract(content, "StyleBundle");
        jsBundles = extract(content, "ScriptBundle");
    }

    private Map<String, List<String>> extract(String content, String type) {
        Map<String, List<String>> bundles = new HashMap<>();

        extractDeclarationType1(content, type, bundles);
        extractDeclarationType2(content, type, bundles);

        return bundles;
    }

    private void extractDeclarationType1(String content, String type, Map<String, List<String>> bundles) {
        RegexUtil.each("var (.+?) = new " + type + "\\(\"(.+?)\"\\);", content, (matcher) -> {
            String bundleVarName = matcher.group(1);
            String bundleName = matcher.group(2);

            Matcher m2 = RegexUtil.matcher(bundleVarName + "\\.Include\\(([^)]+?)\\);", content);
            m2.find();
            String dec = m2.group(1);
            LinkedList<String> list = new LinkedList<>();
            RegexUtil.each("(?m)^\\s+\"(.+?)\"", dec, (m3) -> {
                list.add(m3.group(1).replaceFirst("^~/", ""));
            });

            bundles.put(bundleName, list);
//            System.out.println(list);
        });
    }

    private void extractDeclarationType2(String content, String type, Map<String, List<String>> bundles) {
        RegexUtil.each("(?s)bundles\\.Add\\(new " + type + "\\(\"(.+?)\"\\)(.+?)\\);", content, (matcher) -> {
            String bundleName = matcher.group(1);
            String includes = matcher.group(2);
            System.out.println(includes);

            LinkedList<String> list = new LinkedList<>();
            RegexUtil.each("\\.Include\\(\"~/([^\"]+)\"\\)", includes, (m3) -> {
                list.add(m3.group(1));
            });

            bundles.put(bundleName, list);
        });
    }

    public void updateJsBundle(String jsVarName, String rootDir, String jsDir) {
        String content = FileUtil.readFileToString(path);

        String lineBreak = "\r\n                ";
        String lines = lineBreak + "\"" + Cols.join(Cols.yield(JsCollector.collectJsFiles(jsDir, rootDir, null), jsFile -> "~" + jsFile), "\"," + lineBreak + "\"") + "\"" + lineBreak;


        String newContent = content.replaceAll(jsVarName + "\\.Include\\(([^)]+?)\\);", jsVarName + ".Include(" + lines + ");");

        FileUtil.writeToFile(newContent, path);

    }
}
