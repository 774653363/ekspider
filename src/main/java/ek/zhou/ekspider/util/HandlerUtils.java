package ek.zhou.ekspider.util;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Selectable;

public class HandlerUtils {
    //按照类型存到page里
    public static void pagePutField(Page page, String name, Selectable result, Integer putType) {
        Object e = null;
        if (putType == -1) {
            e = result.get();
        }
        if (putType == 0) {
            e = result.all();
        }
        if (putType == 1) {
            e = result;
        }
        page.putField(name, e);
    }

    //检查存入时的类型
    public static Integer checkPutType(String getType) {
        String[] strs = getType.split("-");
        if (strs.length == 1) {
            return -1;//get
        }
        if ("g".equals(strs[1])) {
            return -1;//get
        }
        if ("a".equals(strs[1])) {
            return 0;//get
        }
        if ("s".equals(strs[1])) {
            return 1;//get
        }
        return -1;
    }
}
