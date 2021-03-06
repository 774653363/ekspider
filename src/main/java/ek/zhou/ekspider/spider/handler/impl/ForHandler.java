package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.UrlMerge;
import ek.zhou.ekspider.main.CreateSpider;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ForHandler extends OperateHandler {
    public ForHandler() {
        this.method = "for";
        String[] necessary = {"resource","detailOperate","name"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
        String[] expression = {"resource","name"};
        this.expressionColumn.addAll(Arrays.asList(expression));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {

            if (!StringUtils.isEmpty(operateElement.getResource())) {
                List<Object> objs = new ArrayList<>();
                OperateElement detailOperate = operateElement.getDetailOperate();
                List<OperateElement> detailOperates = operateElement.getDetailOperates();

                if(page.getResultItems().get(operateElement.getResource())!=null){
                    if(page.getResultItems().get(operateElement.getResource()) instanceof List){
                        List<String> strs = page.getResultItems().get(operateElement.getResource());
                        if(strs!=null&&strs.size()>0){
                            int i = 0;
                            for (String s : strs
                            ) {

                                if (null != detailOperate) {
                                    page.putField("forIndex", i+"");
                                    page.putField("foreach", s);
                                    detailOperate.setResource("foreach");
                                    CreateSpider.operate(page, detailOperate);
                                    Object obj = page.getResultItems().get(detailOperate.getName());
                                    objs.add(obj);
                                }
                                if(detailOperates!=null&&detailOperates.size()>0){
                                    for (OperateElement opt:detailOperates
                                    ) {
                                        page.putField("forIndex", i+"");
                                        page.putField("foreach", s);
                                        opt.setResource("foreach");
                                        CreateSpider.operate(page, opt);
                                        Object obj = page.getResultItems().get(opt.getName());
                                        objs.add(obj);
                                    }
                                }
                                i++;
                            }

                        }
                    }else if(page.getResultItems().get(operateElement.getResource()) instanceof HtmlNode){
                        HtmlNode htmlNode =  (HtmlNode)page.getResultItems().get(operateElement.getResource());
                        List<Selectable> nodes = htmlNode.nodes();
                        if(nodes!=null&&nodes.size()>0){
                            int i = 0;
                            for (Selectable s : nodes
                            ) {

                                if (null != detailOperate) {
                                    page.putField("forIndex", i+"");
                                    page.putField("foreach", s);
                                    detailOperate.setResource("foreach");
                                    CreateSpider.operate(page, detailOperate);
                                    Object obj = page.getResultItems().get(detailOperate.getName());
                                    objs.add(obj);
                                }
                                if(detailOperates!=null&&detailOperates.size()>0){
                                    for (OperateElement opt:detailOperates
                                    ) {
                                        page.putField("forIndex", i+"");
                                        page.putField("foreach", s);
                                        opt.setResource("foreach");
                                        CreateSpider.operate(page, opt);
                                        Object obj = page.getResultItems().get(opt.getName());
                                        objs.add(obj);
                                    }
                                }
                                i++;
                            }

                        }
                    }

                }


                else{
                    //进行次数迭代,格式1->3从1到3进行迭代
                    String[] split = operateElement.getResource().split("->");
                    Integer start = Integer.parseInt(split[0]);
                    Integer end = Integer.parseInt(split[1]);
                    for(int j = start;j<=end;j++){

                        if (null != detailOperate) {
                            page.putField("forIndex", j+"");
                            page.putField("foreach", j+"");
                            detailOperate.setResource("foreach");
                            CreateSpider.operate(page, detailOperate);
                            Object obj = page.getResultItems().get(detailOperate.getName());
                            objs.add(obj);
                        }
                        if(detailOperates!=null&&detailOperates.size()>0){
                            for (OperateElement opt:detailOperates
                            ) {
                                page.putField("forIndex", j+"");
                                page.putField("foreach", j+"");
                                opt.setResource("foreach");
                                CreateSpider.operate(page, opt);
                                Object obj = page.getResultItems().get(opt.getName());
                                objs.add(obj);
                            }
                        }

                    }
                }
                page.putField(operateElement.getName(), objs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
