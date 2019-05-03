package com.cyjz.lucene01;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Lucene01ApplicationTests {

    @Test
    public void manageTest() throws IOException {
        //创建文档列表，保存多个文件
        List<Document> documentList = new ArrayList<>();
         //采集文件系统中的文档数据，放入lucene中

        //指定文件所在目录
        File dir = new File("F:\\JavaProject\\lucene\\lucene01\\src\\main\\resources\\doc");
        //循环文件夹，取出文件
        for(File file : dir.listFiles()){
            //文件名称
            String fileName = file.getName();
            //文件内容
            String fileContent = FileUtils.readFileToString(file, "utf-8");
            //文件大小
            Long fileSize = FileUtils.sizeOf(file);

            //文档对象
            Document document = new Document();
            //第一个参数叫域名，第二个参数叫域值，第三个参数叫是否存储，是为yes，不存储为no
            //是否分词:
            //是否索引
            //是否存储
            // fileName : 要分词，并且不是一个整体，分词有意义，要索引，要存储
            // fileContent : 要分词，要根据内容进行搜索，分词有意义，要索引，不需要
            // fileSize: 要分词，数字要对比，搜索文档可以搜索大小，lucene内部对数字进行了分词算法，
            // 要索引，要根据大小进行搜索，要存储，要显示文档大小
            TextField nameField = new TextField("fileName",fileName, Field.Store.YES);
            TextField contentField = new TextField("fileContent",fileContent, Field.Store.NO);
            LongField sizeField = new LongField("fileSize", fileSize, Field.Store.YES);

            //将所有的域都存入文档中
            document.add(nameField);
            document.add(contentField);
            document.add(sizeField);

            //将文档存入文档集合中
            documentList.add(document);
        }

        //创建分词器,StandardAnalyzer标准分词器对英文分词很好，对中文是单字分词
        Analyzer analyzer = new IKAnalyzer();

        //指定索引和文档存储目录
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //创建写对象的初始化对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        //创建索引和文档写对象
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        //将文档加入到索引和文档的写对象中
        for (Document document : documentList) {
            indexWriter.addDocument(document);
        }
        indexWriter.commit();
        indexWriter.close();
    }

    @Test
    public void testIndexSearch() throws Exception{

        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();
        //创建查询对象，第一个参数是默认搜索域，第二参数是分词器
        //默认搜索域作用：如果搜索语法中指定域名从指定域中搜索，如果搜索时只写了关键字，则从默认搜索域中进行搜索
        QueryParser queryParser = new QueryParser("fileContent",analyzer);
        //查询语法：域名:搜索的关键字
        Query query = queryParser.parse("fileName:lucene");
        //指定索引和文档目录
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        
        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }

    }
    /**
     * 作者 ZYL
     * 功能描述 : 删除索引
     * 日期 2019/4/27 18:21  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testIndexDel() throws  Exception{

        //创建分词器,StandardAnalyzer标准分词器对英文分词很好，对中文是单字分词
        Analyzer analyzer = new IKAnalyzer();

        //指定索引和文档存储目录
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //创建写对象的初始化对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        //创建索引和文档写对象
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        //删除所有索引
//        indexWriter.deleteAll();
        //根据名称删除
        //Term词元，就是一个词，第一个参数：域名，第二参数：要删除的含有次关键字的词的数据
//        indexWriter.deleteDocuments(new Term("fileName","lucene"));
        indexWriter.commit();
        indexWriter.close();
    }
    /**
     * 作者 ZYL
     * 功能描述 : 更新索引
     * 日期 2019/4/27 18:20  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testIndexUpdate() throws Exception{
        //创建分词器,StandardAnalyzer标准分词器对英文分词很好，对中文是单字分词
        Analyzer analyzer = new IKAnalyzer();

        //指定索引和文档存储目录
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //创建写对象的初始化对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        //创建索引和文档写对象
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
//        indexWriter.deleteAll();
        indexWriter.deleteDocuments(new Term("fileName","lucene"));
        //根据文件名称进行更新
        Term term = new Term("fieldName","lucene");
        //更新的对象
        Document document = new Document();
        document.add(new TextField("fileName","lucene更新更新更新", Field.Store.YES));
        document.add(new TextField("fileContent","xxxxxxxxxxxxxxxxxxx", Field.Store.NO));
        document.add(new LongField("fileSize",1099, Field.Store.YES));
        indexWriter.updateDocument(term,document);
        indexWriter.commit();
        indexWriter.close();
    }

    @Test
    public void testIndexTermQuery() throws Exception{
        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();
        //创建查询对象，第一个参数是默认搜索域，第二参数是分词器
        //默认搜索域作用：如果搜索语法中指定域名从指定域中搜索，如果搜索时只写了关键字，则从默认搜索域中进行搜索
//        QueryParser queryParser = new QueryParser("fileContent",analyzer);
//        //查询语法：域名:搜索的关键字
//        Query query = queryParser.parse("fileName:lucene");
        TermQuery termQuery = new TermQuery(new Term("fileName","lucene"));
        //指定索引和文档目录
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(termQuery, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }
    }
    
    /**
     * 作者 ZYL
     * 功能描述 : 根据数字范围查询
     * 日期 2019/4/27 18:48  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testNumericRangeQuery() throws Exception{
        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();
        //根据数字范围查询
        //查询文件大小，大于100小于1000的文章
        //第一个参数，域名，第二个参数：最小值，第三个参数:最大值，第四个：是否最小值，第五个：是否包含最大值
        Query query = NumericRangeQuery.newLongRange("fileSize",100l,1000l,true,true);
        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }
    }
    /**
     * 作者 ZYL
     * 功能描述 : booleanQuery组合查询
     * 日期 2019/4/27 19:29  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testBooleanQuery() throws Exception{
        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();
        //根据数字范围查询
        //查询文件大小，大于100小于1000的文章
        //第一个参数，域名，第二个参数：最小值，第三个参数:最大值，第四个：是否最小值，第五个：是否包含最大值
        //数值的
        Query numericRangeQuery = NumericRangeQuery.newLongRange("fileSize",100l,1000l,true,true);
        //文本的
        TermQuery termQuery = new TermQuery(new Term("fileName","lucene"));
        //booleanQuery组合查询
        //查询文件名称包含lucene，并且，文件大小大于等于100小于等于10000
        BooleanQuery booleanQuery = new BooleanQuery();
        //Occur有三个选项
        //是逻辑条件
        //MUST相当于and关键字，是并且的意思
        //MUST_NOT 相当于‘非’的意思
        //SHOULD相当于or的意思
        //注意：单独使用MUST_NOT没有意义
        booleanQuery.add(numericRangeQuery, BooleanClause.Occur.MUST);
        booleanQuery.add(termQuery, BooleanClause.Occur.MUST);

        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(booleanQuery, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }
    }
    /**
     * 作者 ZYL
     * 功能描述 : 查询所有文件
     * 日期 2019/4/27 19:38  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testMatchAllDocsQuery() throws Exception{
        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();

        MatchAllDocsQuery query = new MatchAllDocsQuery();

        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }
    }
    /**
     * 作者 ZYL
     * 功能描述 : 多域查询
     * 日期 2019/4/27 19:41  
     * 参数 
     * 返回值 void
     */
    @Test
    public void testMultiFieldQuery() throws Exception{
        //创建分词器(创建索引和搜索时的索引所用的分词器必须是一致)
        Analyzer analyzer = new IKAnalyzer();

        String[] fields = {"fileName","fileContent"};
        //从文件名称和文件内容中查询，只要含有lucene的就查出来
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(fields,analyzer);
        //输入需要搜索的关键字
        Query query = multiFieldQueryParser.parse("lucene");

        Directory directory = FSDirectory.open(new File("F:\\luceneDir"));
        //索引和文档的读对象
        IndexReader indexReader = DirectoryReader.open(directory);
        //创建索引的搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //搜索：第一个参数为查询语句对象，第二个参数为查询多少条
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.err.println(topDocs.totalHits);

        //从搜索结果对象中获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        for (ScoreDoc scoreDoc : scoreDocs) {
            //获取docId，
            int docId = scoreDoc.doc;
            //获取文档
            Document document = indexReader.document(docId);
            //get域名可以取出值
            System.err.println("fileName:" + document.get("fileName"));
            System.err.println("fileContent:" + document.get("fileContent"));
            System.err.println("fileSize:" + document.get("fileSize"));
            System.err.println("--------------------------------");

        }
    }
}
