package ek.zhou.ekspider.spider.handler;

import ek.zhou.ekspider.entity.OperateElement;
import us.codecraft.webmagic.Page;

import java.util.LinkedList;
import java.util.List;

/**
 * 操作处理抽象类
 */
public abstract class OperateHandler {

    public String method;
    public List<String> necessaryColumn ;
    public List<String> expressionColumn ;
    public OperateHandler() {
        necessaryColumn = new LinkedList<>();
        necessaryColumn.add("opType");
        expressionColumn = new LinkedList<>();
        expressionColumn.add("opType");
    }

    public abstract void handle(Page page, OperateElement operateElement);
}
