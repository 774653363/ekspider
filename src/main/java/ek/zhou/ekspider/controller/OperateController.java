package ek.zhou.ekspider.controller;

import ek.zhou.ekspider.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description:
 * @author: zhouyikun
 * @create: 2020-06-19 17:34
 */
@RestController
@RequestMapping("/operate")
public class OperateController {
    @Autowired
    RedisUtil redisUtil;

    private static final String keyPrifix= "ekspider:initData:";
    @GetMapping("/all")
    public String[] all(){
        String str = (String)redisUtil.get(keyPrifix+"operate:allOperate");
        return str.split(",");
    }
    @GetMapping("/get/{method}")
    public String[] get(@PathVariable String method){
        String str = (String)redisUtil.get(keyPrifix+"operate:"+method);
        return str.split(",");
    }
}
