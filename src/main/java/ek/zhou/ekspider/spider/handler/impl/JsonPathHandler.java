package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.HandlerUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.Method;
import java.util.Arrays;

public class JsonPathHandler extends OperateHandler {
    public JsonPathHandler() {
        this.method = "jsonPath";
        String[] necessary = {"resource","pattern","name"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {
            Class<? extends Html> clazz = page.getHtml().getClass();
            Integer putType = HandlerUtils.checkPutType(operateElement.getOpType());
            String resource = operateElement.getResource();
            Method method = null;
            method = clazz.getMethod(this.method, String.class);
            Selectable target = page.getResultItems().get(resource);
            if (null == target) {
                target = page.getHtml();
            }
            Selectable result = (Selectable) method.invoke(target, operateElement.getPattern());
            if (!StringUtils.isEmpty(operateElement.getName())) {
                HandlerUtils.pagePutField(page, operateElement.getName(), result, putType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
