package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.UrlMerge;
import ek.zhou.ekspider.main.CreateSpider;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ForHandler extends OperateHandler {
    public ForHandler() {
        this.method = "for";
        String[] necessary = {"resource","detailOperate","name"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {
            if (!StringUtils.isEmpty(operateElement.getResource())) {
                List<String> strs = page.getResultItems().get(operateElement.getResource());
                List<Object> objs = new ArrayList<>();
                for (String s : strs
                ) {
                    OperateElement detailOperate = operateElement.getDetailOperate();
                    if (null != detailOperate) {
                        page.putField("foreach", s);
                        detailOperate.setResource("foreach");
                        CreateSpider.operate(page, detailOperate);
                        Object obj = page.getResultItems().get(detailOperate.getName());
                        objs.add(obj);
                    }
                }
                page.putField(operateElement.getName(), objs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
