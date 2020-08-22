package ek.zhou.ekspider.spider.handler.impl;

import ek.zhou.ekspider.entity.DownLoadInfo;
import ek.zhou.ekspider.entity.OperateElement;
import ek.zhou.ekspider.spider.handler.OperateHandler;
import ek.zhou.ekspider.util.HandlerUtils;
import ek.zhou.ekspider.util.SpringContextUtil;
import ek.zhou.ekspider.util.UrlNameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
@Component
public class DownloadHandler extends OperateHandler {

    private ExecutorService pool = null;

    public DownloadHandler() {
        this.method = "download";
        String[] necessary = {"resource","downLoadInfo"};
        this.necessaryColumn.addAll(Arrays.asList(necessary));
        String[] expression = {"resource","downLoadInfo.fileType","downLoadInfo.dirName","downLoadInfo.fileName","downLoadInfo.path"};
        this.expressionColumn.addAll(Arrays.asList(expression));
    }

    @Override
    public void handle(Page page, OperateElement operateElement) {
        try {
            pool = (ExecutorService) SpringContextUtil.getBean("crawlExecutorPool");
            String resource = operateElement.getResource();
            if (!StringUtils.isEmpty(resource)) {
                //获取需要下载的资源url
                Object obj = page.getResultItems().get(resource);
                List<String> urls = new ArrayList<>();
                if (obj instanceof String) {
                    urls.add((String) obj);
                }
                if (obj instanceof List) {
                    urls = (List<String>) obj;
                }
                //获取下载信息
                DownLoadInfo downLoadInfo = operateElement.getDownLoadInfo();
                //获取存放路径
                String path = downLoadInfo.getPath();
                //获取文件夹名称
                String dirName = page.getResultItems().get(downLoadInfo.getDirName());
                if (dirName == null) {
                    dirName = downLoadInfo.getDirName();
                }

                if (!StringUtils.isEmpty(path) && !StringUtils.isEmpty(resource) && downLoadInfo != null) {
                    String fileName = downLoadInfo.getFileName();

                    int index = 0;
                    for (String url : urls
                    ) {
                        index++;
                        //存储的文件名
                        String saveFileName = null;
                        //获取源文件名
                        String fileNameFromUrl = UrlNameUtil.getFileNameFromUrl(url);
                        //如果文件类型为空就从url中获取文件类型
                        String extension = downLoadInfo.getFileType();
                        if (extension == null) {
                            extension = FilenameUtils.getExtension(fileNameFromUrl);
                        }

                        if ("orgin".equals(fileName)) {
                            saveFileName = fileNameFromUrl;
                        } else if ("index".equals(fileName)) {
                            saveFileName = index + "." + extension;
                        }
                        else if ("uuid".equals(fileName)){
                            saveFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
                        }else{
                            if(fileName==null){
                                saveFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
                            }else{

                                if(null==page.getResultItems().get(fileName)){
                                    saveFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
                                }else{
                                    saveFileName = page.getResultItems().get(fileName)+ "." + extension;
                                }
                            }


                        }
                        //复制文件
                        String filePath = path + "/" + saveFileName;
                        if (dirName != null) {
                            File dir = new File(path + "/" + dirName);
                            if(!dir.exists()){
                                dir.mkdirs();
                            }
                            filePath = path + "/" + dirName + "/" + saveFileName;
                        }
                        File file = new File(filePath);
                        URL u = new URL(url);
                        //异步处理下载任务
                        Runnable task = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FileUtils.copyURLToFile(u,file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        pool.execute(task);
                    }


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
