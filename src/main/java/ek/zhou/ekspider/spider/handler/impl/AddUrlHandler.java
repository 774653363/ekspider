package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.UrlMerge;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.HandlerUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class AddUrlHandler extends OperateHandler {

    public AddUrlHandler() {
        this.method = "addUrl";
        String[] necessary = {"urlInfo"};
       this.necessaryColumn.addAll(Arrays.asList(necessary));
        String[] expression = {"resource","name","urlInfo.prefix","urlInfo.suffix","urlInfo.value"};
        this.expressionColumn.addAll(Arrays.asList(expression));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {
            UrlMerge urlMerge = operateElement.getUrlInfo();
            if (urlMerge != null) {
                StringBuffer url = new StringBuffer();
                if (urlMerge.getPrefix() != null) {
                    url.append(urlMerge.getPrefix());
                }
                String value = (String) page.getResultItems().get(urlMerge.getValue());
                if (value != null) {
                    if(value.startsWith("./")){
                        url.append(value.substring(2));
                    }else{
                        url.append(value);
                    }
                }
                if (urlMerge.getSuffix() != null) {
                    url.append(urlMerge.getSuffix());
                }
                Request request = new Request();
                request.setExtras(new HashMap<>());
                request.getExtras().put("spiderName",page.getRequest().getExtras().get("spiderName"));
                request.getExtras().put("skipExists",page.getRequest().getExtras().get("skipExists"));
                page.addTargetRequest(request);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
