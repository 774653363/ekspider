package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.Spider;
import ek.zhou.ekspider.main.CreateSpider;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.HandlerUtils;
import ek.zhou.ekspider.util.JsonUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CreateSpiderHandler extends OperateHandler {
    public CreateSpiderHandler() {
        this.method = "createSpider";
        String[] necessary = {"resource","createSpiderUrl"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
        String[] expression = {"resource","createSpiderUrl"};
        this.expressionColumn.addAll(Arrays.asList(expression));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {

            String resource = operateElement.getResource();
            //如果配置了createSpiderUrl则从这个文件中读取爬虫信息
            String createSpiderUrl = operateElement.getCreateSpiderUrl();
            Spider spider = null;
            if (!StringUtils.isEmpty(createSpiderUrl)) {
                spider = JsonUtils.readJsonFromClassPath(createSpiderUrl, Spider.class);
            }
            //如果没有配置createSpiderUrl就从createSpider中读取爬虫信息
            if (null != operateElement.getCreateSpider()) {
                spider = operateElement.getCreateSpider();
            }
            //两个属性都没就跳过
            if (spider != null) {
                //如果StartUrl没有配置就从resource中读取,如果有就使用StartUrl
                if (!StringUtils.isEmpty(resource)) {
                    Object obj = page.getResultItems().get(resource);
                    if(obj instanceof List){
                        List<String> urls = page.getResultItems().get(resource);
                        if (null != urls) {
                            if((spider.getStartUrl() != null && spider.getStartUrl().size()> 0)){
                                urls.addAll(spider.getStartUrl());
                            }
                            spider.setStartUrl(urls);
                        }
                    }else if(obj instanceof String){
                        List<String> urls = new LinkedList<>();
                        String url = page.getResultItems().get(resource);
                        if (null != url ) {
                            urls.add(url);
                            if((spider.getStartUrl() != null && spider.getStartUrl().size()> 0)){
                                urls.addAll(spider.getStartUrl());
                            }
                            spider.setStartUrl(urls);
                        }
                    }

                }
                CreateSpider.create(spider);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
