package com.hzgc.hbase.consumer;

import com.hzgc.dubbo.dynamicrepo.CapturePictureSearchService;
import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.dynamicrepo.CapturePictureSearchServiceImpl;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;

public class DubboConsumer {
    static {
        ElasticSearchHelper.getEsClient();
    }
    public static void main(String[] args) {
       /* //测试常规服务
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("consumer.xml");
        context.start();
        System.out.println("consumer start");
        CapturePictureSearchService capturePictureSearchService = (CapturePictureSearchService) context.getBean("CapturePictureSearchService");
        System.out.println("consumer");*/
        CapturePictureSearchService capturePictureSearchService = new CapturePictureSearchServiceImpl();
        SearchOption option = new SearchOption();
        //通过dubbo consumer订阅kafka的producer传参数
        option.setSearchType(SearchType.PERSON);
        option.setImageId("4d7f9083fd4b4b38be0ad53063e42877");
        option.setThreshold(0.15f);
        option.setSortParams("-similarity,-ipcId");
        option.setOffset(0);
        option.setCount(2000);
        capturePictureSearchService.search(option);
    }
}
