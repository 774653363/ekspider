package ek.zhou.ekspider.entity;

import lombok.Data;

@Data
public class DownLoadInfo {
    //文件类型,不填默认是url中的文件名
    private String fileType;
    //存储的路径
    private String path;
    //指的是不填则默认uuid随机文件名选项uuid ,index 按顺序123,orgin原名字,
    private String fileName;
    //文件夹名称page.field.get(dirName)拿到的参数作为文件夹名称,不填则只以path为路径,填了就以path+page.field.get(dirName)作为路径
    private String dirName;
}
