package ek.zhou.ekspider.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class MyPageProcessor implements PageProcessor {
    private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setTimeOut(5000).setSleepTime(2000).setCharset("UTF-8");
    @Override
    public void process(Page page) {

    }

    @Override
    public Site getSite() {
        return site;
    }
}
