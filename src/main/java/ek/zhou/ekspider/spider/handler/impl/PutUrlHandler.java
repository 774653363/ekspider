package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.UrlMerge;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.Arrays;
import java.util.HashMap;

public class PutUrlHandler extends OperateHandler {
    public PutUrlHandler() {
        this.method = "putUrl";
        String[] necessary = {"urlInfo","name"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
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
                    url.append(value);
                }
                if (urlMerge.getSuffix() != null) {
                    url.append(urlMerge.getSuffix());
                }
                page.putField(operateElement.getName(), url.toString());


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
