package ek.zhou.ekspider.controller;

import ek.zhou.ekspider.entity.Spider;
import ek.zhou.ekspider.main.CreateSpider;
import ek.zhou.ekspider.util.JsonUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description: 测试
 * @author: zhouyikun
 * @create: 2020-06-19 14:02
 */
@RestController
public class TestController {
    @GetMapping("/creatSpider")
    public Spider creatSpider(HttpServletResponse response){
        CreateSpider.initOperateList();
        Spider spider = null;
        try {
            spider = JsonUtils.readJsonFromClassPath("spider.json", Spider.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateSpider.create(spider);
        if (spider.getSpiders() != null && spider.getSpiders().size() != 0) {
            for (Spider sp : spider.getSpiders()
            ) {
                CreateSpider.create(sp);
            }
        }
        return new Spider();
    }

}
