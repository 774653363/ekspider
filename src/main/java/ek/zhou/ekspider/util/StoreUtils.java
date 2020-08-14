package ek.zhou.ekspider.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StoreUtils {

    /**
     * 统计错误URL
     */
    public static Set<String> errorListUrls = Collections.synchronizedSet(new HashSet<>());

    /**
     * 统计成功URL
     */
    public static Set<String> successListUrls = Collections.synchronizedSet(new HashSet<>());



}
