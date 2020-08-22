package ek.zhou.ekspider.entity;

import lombok.Data;

import java.util.List;

@Data
public class OperateElement {
    //操作类型
    private String opType;
    //xpath,css,jsonPath,正则的处理格式
    private String pattern;
    //处理后保存内容的key
    private String name;
    //合成url
    private UrlMerge urlInfo;
    //数据来源
    private String resource ;
    //进一步操作元素
    private OperateElement detailOperate;
    //进一步操作元素集合
    private List<OperateElement> detailOperates;
    //创建爬虫
    private Spider createSpider;
    //通过爬虫文件的路径创建爬虫
    private String createSpiderUrl;
    //下载信息
    private DownLoadInfo downLoadInfo;
    //表达式
    private String exp;
    //表达式分隔符,如和exp中的参数发生冲突可以自定义该参数
    private String splitStr="#";
}
