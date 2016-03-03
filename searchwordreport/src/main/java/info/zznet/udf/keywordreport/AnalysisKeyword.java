package info.zznet.udf.keywordreport;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.zznet.udf.util.URLUtil;

public class AnalysisKeyword {

	public String exec(String url) throws Exception {
		String reg = "([?|#]kw|&kw)=[^&]*{}([?|#]word|&word)=[^&]*{}([?]key[0-9]*|&key[0-9]*)=[^&]*{}([?|#]query|&query)=[^&]*{}([?|#]words|&words)=[^&]*{}([?|#]wd|&wd)=[^&]*{}(&k|[?|#]k)=[^&]*{}(&q[0-9]*|[?|#]q[0-9]*)=[^&]*{}(&w|[?|#]w)=[^&]*{}([?|#]fromtitle|&fromtitle)=[^&]*{}([?|#]fcQuery|&fcQuery)=[^&]*{}([?|#]qw|&qw)=[^&]*{}([?|#]websearchkw|&websearchkw)=[^&]*{}([?|#]b_q|&b_q)=[^&]*{}([?|#]keyword|&keyword)=[^&]*";
		String keyword = null;
		if (url == null)
			return null;
		try {
			// 因为以http://cpro.baidu.com开头的URL中可能会有包含多个可解析的参数，但只有k参数才是需要解析的所以提前处理。
			if (url.matches("^http://cpro\\.baidu\\.com.*")) {
				String keywordTmp = URLUtil.getKeyword(url, "([?|#]k|&k)=[^&]*");
				if (keywordTmp == null || keywordTmp.equals("")) {
					String argReg = "([?|#]refer|&refer)=[^&]*";
					String refer = URLUtil.pickArgsToURL(url, argReg);
					if (refer == null || refer.equals(""))
						return "";
					keyword = URLUtil.getKeywordForRegs(refer, reg);
					if (keyword != null && !keyword.equals("")) {
						return keyword;
					} else {
						return "";
					}
				} else {
					return keywordTmp;
				}
			}
			if (url.matches("^http://googleads\\.g\\.doubleclick\\.net.*")) {
				return "";
			}
			// 因为以^http://.*sogou.*开头的URL
			if (url.matches("^http://[a-z]*.sogou.com.*")) {
				String keywordTmp = URLUtil.getKeyword(url, "([?|#]query|&query)=[^&]*");
				if (keywordTmp == null || keywordTmp.equals("")) {
					String argReg = "([?|#]keyword|&keyword)=[^&]*";
					keyword = URLUtil.getKeywordForRegs(url, argReg);
					if (keyword != null && !keyword.equals("")) {
						return keyword;
					} else {
						return "";
					}
				} else {
					return keywordTmp;
				}
			}
			// 普通方式解析搜索词

			keyword = URLUtil.getKeywordForRegs(url, reg);
			if (keyword != null && !keyword.equals(""))
				return keyword;
			// 解析以cb.baidu.com、pos.baidu.com开头的URL
			if (url.matches("^http://pos\\.baidu\\.com.*") || url.matches("^http://cb\\.baidu\\.com.*")) {
				String ltrReg = "[?|#]ltr|&ltr=[^&]*";
				String ltuReg = "[?|#]ltu|&ltu=[^&]*";
				String ltr = URLUtil.pickArgsToURL(url, ltrReg);
				keyword = URLUtil.getKeywordForRegs(ltr, reg);
				if (keyword != null && !keyword.equals("")) {
					return keyword;
				} else {
					String ltu = URLUtil.pickArgsToURL(url, ltuReg);
					if (ltu == null || ltu.equals("")) {
						return "";
					}
					keyword = URLUtil.getKeywordForRegs(ltu, reg);
					if (keyword != null && !keyword.equals("")) {
						return keyword;
					}
				}
			}
			// 解析m.baidu.com、wap.baidu.com，按获取URL中/w=0_10_搜索词/的内容
			if (url.matches("^http://m[0-9]*\\.baidu\\.com.*") || url.matches("^http://wap\\.baidu\\.com.*")) {
				if (url.indexOf("w=0_10_") != -1) {
					String wReg = "/w=0_10_([^/]*)";
					Pattern pattern = Pattern.compile(wReg);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						String tmp = matcher.group(1);
						try {
							keyword = URLDecoder.decode(tmp, "utf-8");
						} catch (UnsupportedEncodingException e) {
							return tmp;
						}
						if (keyword != null && !keyword.equals("")) {
							return keyword;
						} else {
							return "";
						}
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
			// 处理赶集URL中，http://cs.ganji.com/site/s/_%E5%BC%80%E5%BA%97/,此类型的URL
			if (url.matches("^http://.*\\.ganji\\.com.*") || url.matches("^http://.*\\.58\\.com.*")) {
				String wReg = "/[a-z]*_([^/]*)";
				String urlReg = "([?|#]websearchkw|&websearchkw)=[^&]*";
				keyword = URLUtil.getKeyword(url, urlReg);
				if (keyword == null || keyword.equals("")) {
					Pattern pattern = Pattern.compile(wReg);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						String tmp = matcher.group(1);
						try {
							keyword = URLDecoder.decode(tmp, "utf-8");
						} catch (UnsupportedEncodingException e) {
							return tmp;
						}
						if (keyword != null && !keyword.equals("")) {
							return keyword;
						} else {
							return "";
						}
					}
				} else {
					return keyword;
				}
			}
			// 处理suningURL,解析src中URL的搜索词作为搜索词
			if (url.matches("^http://.*\\.suning\\.com.*")) {
				String argReg = "[?|#]src|&src=[^&]*";
				String src = URLUtil.pickArgsToURL(url, argReg);
				if (src == null || src.equals(""))
					return "";
				keyword = URLUtil.getKeywordForRegs(src, reg);
				if (keyword != null && !keyword.equals("")) {
					return keyword;
				} else {
					return "";
				}
			}
			// 处理baidu.mobi url,解析两个word中其中一个不为空的word作为搜索词
			if (url.matches("^http://.*baidu\\.mobi.*")) {
				String argReg = "word=([^&])*";
				Pattern pattern = Pattern.compile(argReg);
				Matcher matcher = pattern.matcher(url);
				while (matcher.find()) {
					String[] tmp = matcher.group().split("=", 2);
					if (!tmp[1].equals("")) {
						try {
							keyword = URLDecoder.decode(tmp[1], "utf-8");
						} catch (UnsupportedEncodingException e) {
							return tmp[1];
						}
					}
				}
				if (keyword != null && !keyword.equals("")) {
					return keyword;
				} else {
					return "";
				}
			}
			// 处理vvtoolURL,解析url中URL的搜索词作为搜索词
			if (url.matches("^http://.*vvtool.com.*")) {
				String argReg = "[?|#]url|&url=[^&]*";
				String src = URLUtil.pickArgsToURL(url, argReg);
				if (src == null || src.equals(""))
					return "";
				keyword = URLUtil.getKeywordForRegs(src, reg);
				if (keyword != null && !keyword.equals("")) {
					return keyword;
				} else {
					return "";
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return "";
	}

	public static void main(String[] args) throws Exception {
//        System.out.println(exec("http://www.baidu.com/s?cl=3&wd=Invalid+signature+file+digest+for+Manifest+main+attributes&tn=ubuntuu_cb"));
	}

}
