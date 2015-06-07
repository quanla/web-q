package qj.tool.web.servlet;

import qj.util.FileUtil;
import qj.util.RegexUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

        // Css bundle:
        RegexUtil.each("var (.+?) = new " + type + "\\(\"(.+?)\"\\);", content, (matcher) -> {
            String bundleVarName = matcher.group(1);
            String bundleName = matcher.group(2);

//            System.out.println("bundleName=" + bundleName + ", bundleVarName=" + bundleVarName);

//            Matcher m2 = RegexUtil.matcher(bundleVarName + "\\.Include\\(((?:.|\r?\n)+?)\\);", content);
            Matcher m2 = RegexUtil.matcher(bundleVarName + "\\.Include\\(([^)]+?)\\);", content);
            m2.find();
            String dec = m2.group(1);
            LinkedList<String> list = new LinkedList<>();
            RegexUtil.each("\"(.+?)\"", dec, (m3) -> {
                list.add(m3.group(1).replaceFirst("^~/", ""));
            });

            bundles.put(bundleName, list);
//            System.out.println(list);
        });

        return bundles;
    }
}
