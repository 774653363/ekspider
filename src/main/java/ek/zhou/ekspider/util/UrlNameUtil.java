package ek.zhou.ekspider.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlNameUtil {
    public static String getFileNameFromUrl(String str) {
        String suffixes = "avi|mpeg|3gp|mp3|mp4|wav|jpeg|gif|jpg|png|apk|exe|pdf|rar|zip|docx|doc";
        Pattern pat = Pattern.compile("[\\w]+[\\.](" + suffixes + ")");//正则判断
        Matcher mc = pat.matcher(str);//条件匹配
        mc.find();
        String substring = mc.group();//截取文件名后缀名
        return substring;

    }
}
