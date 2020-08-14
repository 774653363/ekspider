package ek.zhou.ekspider.spider;

import ek.zhou.ekspider.config.RedisConfig;
import ek.zhou.ekspider.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyRedisPipeline implements Pipeline {

    @Autowired
    RedisUtil redisUtil;
    private static final String keyPrifix= "ekspider:spiders:";
    @Override
    public void process(ResultItems resultItems, Task task) {
       //获取信息
        String key = resultItems.get("pipelineId");
        String spiderName = (String) resultItems.getRequest().getExtras().get("spiderName");
        //将当前url添加到redis的成功url列表中
        String url = resultItems.getRequest().getUrl();

        redisUtil.sSetAndTime(keyPrifix+spiderName+":successUrl", RedisConfig.expireTime,resultItems.getRequest().getUrl());

        if(StringUtils.isEmpty(key)){
            return;
        }
        Map<String,Object> value = new HashMap<>();
        Map<String, Object> all = resultItems.getAll();
        for (String k:all.keySet()
             ) {
            if(all.get(k)!=null){
                value.put(k,all.get(k));
            }
        }
        value.remove("pipelineId");
        key = keyPrifix+spiderName+":info:"+key;
        redisUtil.set(key,value,RedisConfig.expireTime);
    }
}
