package ek.zhou.ekspider.entity;

import lombok.Data;
import us.codecraft.webmagic.Site;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Data
public class Spider {
    //是否跳过已爬过的内容
    private Boolean skipExists = false;
    //爬虫名字
    private String name = "default";
    //开始url
    private List<String> startUrl;
    //操作元素
    private List<OperateElement> operateElements;
    //爬虫
    private List<Spider> spiders;
    //需要保存到redis的字段,第一个为key
    private List<String> pipeline;
    //爬虫配置
    private Site site;
    //爬虫线程
    private Integer thread = 1;

}
