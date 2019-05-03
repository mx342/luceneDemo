package com.cyjz.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SolrApplicationTests {


    @Value("${spring.data.solr.host}")
    private String solrHost = "";

    /**
     * 作者 ZYL
     * 功能描述 : 生成索引
     * 日期 2019/5/1 17:30
     * 参数
     * 返回值 void
     */
    @Test
    public void testIndexCreate() throws Exception {
        //连接solr服务端
        SolrServer solrServer = new HttpSolrServer(solrHost);

        SolrInputDocument doc = new SolrInputDocument();
        //域要先定义后使用，必须要要注意要有id主键域
        //solr中没有专用的修改方法，会根据id进行查找，如果找到了，删除原来的，根据新的进行查找
        doc.addField("id","a001");
        doc.addField("product_name","台灯111");
        doc.addField("product_price","12.5");



        solrServer.add(doc);
        solrServer.commit();
    }
    /**
     * 作者 ZYL
     * 功能描述 : 删除操作
     * 日期 2019/5/1 17:44
     * 参数
     * 返回值 void
     */
    @Test
    public void testIndexDel() throws Exception{
        SolrServer solrServer = new HttpSolrServer(solrHost);

        //根据主键id进行删除
        solrServer.deleteById("a001");
//        solrServer.deleteByQuery("*:*");

        solrServer.commit();
    }
    /**
     * 作者 ZYL
     * 功能描述 : 简单查询
     * 日期 2019/5/1 17:31
     * 参数 null
     * 返回值
     */
    @Test
    public void tesIndexSearch1() throws Exception{
        SolrServer solrServer = new HttpSolrServer(solrHost);

        //创建solr的查询条件对象
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("product_name:台灯");

        //查询并获取查询响应对象
        QueryResponse queryResponse = solrServer.query(solrQuery);
        //从查询响应中获取查询结果集对象
        SolrDocumentList results = queryResponse.getResults();
        //遍历查询结果集
        //打印查询的一共的多少条
        System.err.println("总查询条数:" + results.getNumFound());
        for (SolrDocument doc : results) {
            System.err.println("==============" + doc.get("id"));
            System.err.println("==============" + doc.get("product_name"));
            System.err.println("==============" + doc.get("product_price"));
            System.err.println("---------------------------------------------------");
        }
        solrServer.commit();
    }
    /**
     * 作者 ZYL
     * 功能描述 : 复杂查询
     * 日期 2019/5/1 19:34
     * 参数
     * 返回值 void
     */
    @Test
    public void tesIndexSearch2() throws Exception {
        SolrServer solrServer = new HttpSolrServer(solrHost);

        //创建solr的查询条件对象
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("台灯");
        //设置默认搜索与
        solrQuery.set("df","product_keywords");
        //设置过滤条件
        solrQuery.addFilterQuery("product_price:[1 TO 100]");
        //设置排序，降序
        solrQuery.setSort("product_price", SolrQuery.ORDER.desc);
        //设置分页
        solrQuery.setStart(0);
        //设置查询多少条
        solrQuery.setRows(10);
        //设置高亮hl
        solrQuery.setHighlight(true);
        //设置高亮显示的域
        String highlightingField = "product_name";
        solrQuery.addHighlightField(highlightingField);
        //设置高亮前缀
        solrQuery.setHighlightSimplePre("<span style=\"color:red\" >");
        //设置高亮后缀
        solrQuery.setHighlightSimplePost("</span>");

        //=========================查询并获取查询响应对象=======================
        QueryResponse queryResponse = solrServer.query(solrQuery);
        //从查询响应中获取查询结果集对象
        SolrDocumentList results = queryResponse.getResults();
        //遍历查询结果集
        //打印查询的一共的多少条
        System.err.println("总查询条数:" + results.getNumFound());
        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
        for (SolrDocument doc : results) {
            System.err.println("==============" + doc.get("id"));
            Map<String, List<String>> highlightingById = highlighting.get(doc.get("id"));
            if(highlightingById.containsKey(highlightingField)){
                List<String> msgList = highlightingById.get(highlightingField);
                for (String s : msgList) {
                    System.err.println("==============highlighting:" + s);
                }
            }
            System.err.println("==============" + doc.get("product_name"));
            System.err.println("==============" + doc.get("product_price"));
            System.err.println("---------------------------------------------------");
        }
    }
}
