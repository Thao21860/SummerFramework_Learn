package org.example.mvc.utils;

import javax.servlet.ServletException;
import java.util.regex.Pattern;

public class PathUtils {
    // 正则表达式处理url
    // 替换字符串中的"{name}“或者”{age}“等子串替换为”(?name[/]*)“或者”(?age[/]*)"等
    public static Pattern compile(String path) throws ServletException {
        String regPath = path.replaceAll("\\{([a-zA-Z][a-zA-Z0-9]*)\\}", "(?<$1>[^/]*)");
        if (regPath.indexOf('{') >= 0 || regPath.indexOf('}') >= 0) {
            throw new ServletException("invalid path" + path);
        }

        return Pattern.compile("^" + regPath + "$");
    }
}
