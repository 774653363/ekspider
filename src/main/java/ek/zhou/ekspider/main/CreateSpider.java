package ek.zhou.ekspider.main;

import com.alibaba.fastjson.JSONObject;
import ek.zhou.ekspider.config.RedisConfig;
import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.entity.Spider;
import ek.zhou.ekspider.spider.*;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Component
public class CreateSpider  {
    private static final String keyPrifix= "ekspider:spiders:";
    private static HashMap<String,OperateHandler> operateHandlerHashMap = new HashMap<>();
    public static MySpider mySpider;
    @Autowired
    public void setMySpider(MySpider mySpider) {
        CreateSpider.mySpider = mySpider;
    }
    @Autowired
    public static RedisUtil redisUtil;
    @Autowired
    public  void setRedisUtil(RedisUtil redisUtil) {
        CreateSpider.redisUtil = redisUtil;
    }




    public static ExecutorService pool;
    @Autowired
    @Qualifier(value = "crawlExecutorPool")
    public void setPool(ExecutorService pool) {
        CreateSpider.pool = pool;
    }


    public static MyRedisPipeline myRedisPipeline;
    @Autowired
    public void setMyRedisPipeline(MyRedisPipeline myRedisPipeline) {
        CreateSpider.myRedisPipeline = myRedisPipeline;
    }


    public static MyPipeline myPipeline;
    @Autowired
    public void setMyPipeline(MyPipeline myPipeline) {
        CreateSpider.myPipeline = myPipeline;
    }


    public static MyDownloader myDownloader;
    @Autowired
    public void setMyDownloader(MyDownloader myDownloader) {
        CreateSpider.myDownloader = myDownloader;
    }

    protected static Logger logger = LoggerFactory.getLogger(CreateSpider.class);

    public static void initOperateList(){
        {
            //比如可以获取有Pay注解的class
            Reflections reflections = new Reflections("ek.zhou.ekspider.*");

            Set<Class<? extends OperateHandler>> subTypesOf = reflections.getSubTypesOf(OperateHandler.class);

            for (Class cl : subTypesOf) {
                OperateHandler operateHandler = null;
                try {
                    operateHandler = (OperateHandler)cl.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                operateHandlerHashMap.put(operateHandler.method,operateHandler);

            }
        }
    }
    //创建新爬虫
    public static void create(Spider spider) {
        PageProcessor pageProcessor = (PageProcessor) Proxy.newProxyInstance(
                MyPageProcessor.class.getClassLoader(),
                new Class[]{PageProcessor.class},
                new InvocationHandler() {
                    @Override
                    //invoke 代表的是执行代理对象的方法
                    //method：代表目标对象的方法字节码对象
                    //args:代表目标对象的响应的方法的参数
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object invoke = method.invoke(new MyPageProcessor(), args);
                        if ("getSite".equals(method.getName())) {
                            Site site = spider.getSite();
                            if (site == null) {
                                return invoke;
                            }
                            return site;
                        }
                        if ("process".equals(method.getName())) {
                            Page page = (Page) args[0];

                           try{
                               page.putField("url", page.getUrl());
                               page.putField("html", page.getHtml());
                               for (OperateElement getElement : spider.getOperateElements()
                               ) {
                                   operate(page, getElement);
                               }
                               List<String> pipeline = spider.getPipeline();
                               if (pipeline != null && pipeline.size() > 0) {
                                   Map<String, Object> all = page.getResultItems().getAll();
                                   for (String exit : all.keySet()
                                   ) {
                                       if (!pipeline.contains(exit)) {
                                           page.getResultItems().put(exit, null);
                                       }
                                   }
                                   page.getResultItems().put("pipelineId", page.getResultItems().get(pipeline.get(0)));
                               }
                           }catch (Exception e){
                               String spiderName = (String) page.getRequest().getExtras().get("spiderName");
                               redisUtil.sSetAndTime(keyPrifix+spiderName+":errorUrl", RedisConfig.expireTime,page.getUrl().get());
                               logger.error("url:"+page.getUrl().get()+"出错了!错误原因:"+e.getMessage());
                               e.printStackTrace();
                           }
                        }

                        return invoke;
                    }
                });
        List<String> pipeline = spider.getPipeline();
        Pipeline p = null;
        if (pipeline != null && pipeline.size() > 0) {
            p = myRedisPipeline;
        } else {
            p = myPipeline;
        }
        //对错误url进行重爬的位置
        //将redis中错误的请求添加到爬虫里,重新爬取
        String spiderName = spider.getName();
        List<Request> requestList = new ArrayList<>();
        List<Object> list = new ArrayList<>();
        list.addAll(spider.getStartUrl());

        if(redisUtil.sGetSetSize(keyPrifix+spiderName+":errorUrl")>0){
            Set<Object> errorList = redisUtil.sGet(keyPrifix + spiderName + ":errorUrl");
            //将启动url放到爬虫里
            list.addAll(errorList);
            //将错误url添加到爬虫里

        }
        for (Object errorUrl:list
        ) {
            Request request = new Request((String)errorUrl);
            request.setExtras(new HashMap<>());
            request.getExtras().put("spiderName",spiderName);
            request.getExtras().put("skipExists",spider.getSkipExists());
            requestList.add(request);
        }



        MySpider
                .create(pageProcessor)
                .addPipeline(p)
                .setDownloader(myDownloader)
                .addRequest(requestList.toArray(new Request[0]))
                .thread(spider.getThread())
                .run();
    }

    //对操作元素进行解析并执行操作
    public static void operate(Page page, OperateElement operateElement) throws Exception {
        //解析表达式
        operateElement=parseExp(operateElement);
        OperateElement detailOperate = operateElement.getDetailOperate();
        List<OperateElement> detailOperates = operateElement.getDetailOperates();
        //解析表达式
        if (null != detailOperate) {
            detailOperate = CreateSpider.parseExp(detailOperate);
        }
        if(detailOperates!=null&&detailOperates.size()>0) {
            for (OperateElement opt : detailOperates
            ) {
                opt = CreateSpider.parseExp(opt);
            }
        }
        operateElement.setDetailOperate(detailOperate);
        operateElement.setDetailOperates(detailOperates);

        //type由两部分组成,第一部分是操作类型,第二部分是存到page.field的方式-a表示all,-g表示get,-s表示Selectable
        String type = operateElement.getOpType().split("-")[0];
        if(type==null){
            return;
        }

        OperateHandler operateHandler = operateHandlerHashMap.get(type);
        operateHandler.handle(page,operateElement);


        //如果DetailOperate的Resource不存在则使用父类的Name作为Resource
        if (operateElement.getDetailOperate() != null && operateElement.getDetailOperate().getResource() == null && operateElement.getName() != null) {
            operateElement.getDetailOperate().setResource(operateElement.getName());
        }
        //如果DetailOperates下的元素的Resource不存在则使用父类的Name作为Resource
        if(operateElement.getDetailOperates()!=null&&operateElement.getDetailOperates().size()>0){
            for (OperateElement opt:operateElement.getDetailOperates()
                 ) {
                if (opt != null && opt.getResource() == null && operateElement.getName() != null) {
                    opt.setResource(operateElement.getName());
                }
            }
        }
        //不是for类型则执行DetailOperate
        if (!"for".equals(operateElement.getOpType())) {
            if (operateElement.getDetailOperate() != null) {
                operate(page, operateElement.getDetailOperate());
            }
            if(operateElement.getDetailOperates()!=null&&operateElement.getDetailOperates().size()>0){
                for (OperateElement opt:operateElement.getDetailOperates()
                ) {
                    if (opt!= null) {
                        operate(page, opt);
                    }

                }

            }
        }

    }

    public static OperateElement parseExp(OperateElement operateElement){
        //解析表达式
        String exp = operateElement.getExp();
        //不为空时进行表达式的解析
        if(!StringUtils.isEmpty(exp)){
            String[] split = exp.split(operateElement.getSplitStr());
            if(split.length>0){
                String type = split[0];
                String realType = type.split("-")[0];
                OperateHandler operateHandler = operateHandlerHashMap.get(realType);
                List<String> expressionColumn = operateHandler.expressionColumn;
                String jsonString = JSONObject.toJSONString(operateElement);
                JSONObject jsonObject = JSONObject.parseObject(jsonString);
                for (int i = 0 ;i<split.length&&i<expressionColumn.size();i++
                ) {
                    String col = expressionColumn.get(i);
                    String ele = split[i];
                    if("null".equals(ele)||StringUtils.isEmpty(ele)){
                        continue;
                    }
                    if(col.contains(".")){
                        String[] strs = col.split("\\.");
                        JSONObject parent = jsonObject.getJSONObject(strs[0]);
                        if(parent==null){
                            parent = new JSONObject();
                            jsonObject.put(strs[0],parent);
                        }
                        parent.put(strs[1],ele);
                    }else{
                        jsonObject.put(col,ele);
                    }
                }
                operateElement = JSONObject.toJavaObject(jsonObject, OperateElement.class);
            }
        }
        return operateElement;
    }

}
