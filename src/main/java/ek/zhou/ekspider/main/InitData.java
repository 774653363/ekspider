package ek.zhou.ekspider.main;

import ek.zhou.ekspider.config.RedisConfig;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.RedisUtil;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 初始化数据
 * @description:
 * @author: zhouyikun
 * @create: 2020-06-19 16:10
 */
@Component
public class InitData implements ApplicationRunner {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Autowired
    RedisUtil redisUtil;
    private static final String keyPrifix= "ekspider:initData:";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //初始化操作元素必须的参数
        List<String> allOperate = new LinkedList<>();
        Reflections reflections = new Reflections("ek.zhou.ekspider.*"); //比如可以获取有Pay注解的class
        Set<Class<? extends OperateHandler>> subTypesOf = reflections.getSubTypesOf(OperateHandler.class);
        for (Class cl : subTypesOf) {
            OperateHandler operateHandler = (OperateHandler)cl.newInstance();
            redisUtil.set(keyPrifix+"operate:"+operateHandler.method,String.join(",",operateHandler.necessaryColumn), RedisConfig.expireTime);

            allOperate.add(operateHandler.method);
        }
        redisUtil.set(keyPrifix+"operate:allOperate",String.join(",",allOperate), RedisConfig.expireTime);

    }
}
