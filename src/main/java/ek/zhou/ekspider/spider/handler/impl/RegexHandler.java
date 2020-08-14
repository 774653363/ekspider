package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.HandlerUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.RegexSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.Method;
import java.util.Arrays;

public class RegexHandler extends OperateHandler {
    public RegexHandler() {
        this.method = "regex";
        String[] necessary = {"resource","pattern","name"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {

            String resource = operateElement.getResource();
            if (!StringUtils.isEmpty(resource)) {
                Object obj = page.getResultItems().get(resource);
                if (obj instanceof Selectable) {
                    Selectable selectable = (Selectable) obj;
                    Selectable result = selectable.regex(operateElement.getPattern());
                    page.getResultItems().put(operateElement.getName(), result.get());
                }
                if (obj instanceof String) {
                    String str = (String) obj;
                    RegexSelector regexSelector = new RegexSelector(operateElement.getPattern());
                    String result = regexSelector.select(str);
                    page.getResultItems().put(operateElement.getName(), result);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
