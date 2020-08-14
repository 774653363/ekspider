package ek.zhou.ekspider.entity;

import lombok.Data;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.*;

/**
 * @description:
 * @author: zhouyikun
 * @create: 2020-06-19 17:54
 */
@Data
public class MySite extends Site{
    private String name;
}
