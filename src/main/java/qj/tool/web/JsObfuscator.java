package qj.tool.web;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

import qj.tool.http.WebSession;
import qj.ui.DesktopUI4;
import qj.util.Cols;
import qj.util.FileUtil;
import qj.util.HttpUtil;
import qj.util.RegexUtil;
import qj.util.funct.F1;
import qj.util.funct.Fs;


public class JsObfuscator {
	public static void main(String[] args) {
		String content = "var Bill = Bill || {};Bill.love=\"1\";";
//		File cacheDir = new File("target/JsObfuscateCache");
//		cacheDir.mkdirs();
//		F1<String, String> obf = Fs.cacheDigest(obfuscate, FileUtil.fileStringGetter(cacheDir), FileUtil.fileStringSetter(cacheDir));
		System.out.println(obfuscate.e(content));
	}

	public static F1<String, String> obfuscate = new F1<String, String>() {public String e(String content) {
		WebSession ws = new WebSession();
		ws.cookies.put("__cfduid", "d00dedca0ccf0f7fd3e9cf945a755310e1396203877258");
		Map<Object, Object> form = Cols.map(
				"__VIEWSTATE", "/wEPDwUKLTI0MDAwODAzNmQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgYFCGNiTGluZUJSBQhjYkluZGVudAULY2JFbmNvZGVTdHIFDmNiRW5jb2RlTnVtYmVyBQljYk1vdmVTdHIFDmNiUmVwbGFjZU5hbWVzkhu7ipx09t7ORqMSiqXAZjxixpvev5qGduB5U7lxKAA=",
				"__EVENTVALIDATION", "/wEdAAtVzcjdONxmP98Zn2VeKgWWESCFkFW/RuhzY1oLb/NUVB2nXP6dhZn6mKtmTGNHd3PN+DvxnwFeFeJ9MIBWR693/0+kJGcigziRf+JnyYP3ngWOnPKUhxuCfOKb0tlvVuly5juiFHJSf6q9cXRA/+LsCzkidEk0Y8qCyJLcOKXNoEywswNt0lfddYqrIj/HYv1fNaBSlQ4gCFEJtbofwBY37hv76BH8vu7iM4tkb8en1RGDlH5soHS6hWUl4JVZYtSZ51XOVy0Wuo6R2616LTDx",
				"TextBox1", content,
				"TextBox2", "",
				"Button1", "Obfuscate",
				"cbReplaceNames", "on",
				"cbLineBR", "on",
				"TextBox3", "^_get_\n^_set_\n^_mtd_\n"
				);
//		DesktopUI4.alert(HttpUtil.formatPostForm(form));
		String result = ws.post("http://javascriptobfuscator.com/", HttpUtil.formatPostForm(form));
		
		String r = parseResult(result)
				.replaceAll("&quot;", "\"")
				.replaceAll("&gt;", ">")
				.replaceAll("&lt;", "<")
				.replaceAll("&amp;", "&");
		r = RegexUtil.replaceAll(r, "&#(\\d+);", new F1<Matcher,String>() {public String e(Matcher m) {
			return "" + (char)Integer.parseInt(m.group(1));
		}});
		return r;
		
	}};

	public static String parseResult(String result) {
		int i1 = result.indexOf("id=\"TextBox2\"");
		int i2 = result.indexOf(">", i1) + 3;
		int i3 = result.indexOf("</textarea>", i2);
		
		return result.substring(i2, i3);
	}
}
