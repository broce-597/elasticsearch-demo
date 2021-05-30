package com.broce.demo.tool;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ES工具类
 */
@Component
public class ElasticSearchUtils {

    @Resource
    private RestHighLevelClient esClient;

//    @Resource
//    private ObjectMapper objectMapper;

    /**
     * 分页获取数据
     * @param index 索引
     * @param matchQueryBuilder 查询对象
     * @param query 分页对象
     * @param <T>
     * @return
     */
    public <T> R<IPage<T>> page(String index, QueryBuilder matchQueryBuilder, Query query,Class<T> clazz) {
        try {
            IPage<T> pageModel=Condition.getPage(query);

            // 不可以超过1w条，超过就返回提示走查询条件筛选
            if(pageModel.getCurrent()*pageModel.getSize()>10000){
                return R.fail("查询结果超过10000条，请输入查询条件筛选后再查询");
            }
            // 先获取总数
            R<Long> count= getRecordCount(index,matchQueryBuilder);
            if (!count.isSuccess()){
                return R.fail(count.getMsg());
            }
            if(count.getData()<=0){
                pageModel.setTotal(0);
                return  R.data(pageModel);
            }
            pageModel.setTotal(count.getData());

            // 1.构建SearchRequest请求对象，指定索引库
            SearchRequest searchRequest=new SearchRequest();
            searchRequest.indices(index);
            // 2.构建SearchSourceBuilder查询对象
            SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
            // 3.构建QueryBuilder对象指定查询方式和查询条件
            // 4.将QueryBuilder对象设置到SearchSourceBuilder中
            searchSourceBuilder.query(matchQueryBuilder);
            // 5.字段过滤
            //searchSourceBuilder.fetchSource(new String[]{""},new String[]{""});
            // 6.指定分页
            searchSourceBuilder.from((int)pageModel.offset());
            searchSourceBuilder.size((int)pageModel.getSize());
            // 真实总数
            //searchSourceBuilder.trackTotalHits(true);
            // 7.排序
            List<String> ascs=null;
            List<String> descs=null;
            if (StringUtil.isNotBlank(query.getAscs())){
               ascs= Func.toStrList(query.getAscs());
            }
            if (StringUtil.isNotBlank(query.getDescs())){
                descs= Func.toStrList(query.getDescs());
            }
            sort(searchSourceBuilder,ascs,descs);
            // 8.将SearchSourceBuilder设置到SearchRequest中
            searchRequest.source(searchSourceBuilder);
            // 9.设置option
            searchRequest.indicesOptions(IndicesOptions.fromOptions(true,true,true,false));
            // 10.调用方法查询数据
            SearchResponse searchResponse= esClient.search(searchRequest,RequestOptions.DEFAULT);
            List<T> list=getSearchResult(searchResponse,clazz);
            pageModel.setRecords(list);
            return R.data(pageModel);
        }catch (Exception e){
            e.printStackTrace();
            return R.fail("查询分页数据失败:"+e.getMessage());
        }
    }

    /**
     * 通过提交获取单条数据
     * @param index
     * @param matchQueryBuilder
     * @param <T>
     * @return
     */
    public <T> R<T> getById(String index,QueryBuilder matchQueryBuilder,Class<T> clazz){
        try {
            // 1.构建SearchRequest请求对象，指定索引库
            SearchRequest searchRequest=new SearchRequest();
            searchRequest.indices(index);
            // 2.构建SearchSourceBuilder查询对象
            SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
            // 3.构建QueryBuilder对象指定查询方式和查询条件
            // 4.将QueryBuilder对象设置到SearchSourceBuilder中
            searchSourceBuilder.query(matchQueryBuilder);
            // 8.将SearchSourceBuilder设置到SearchRequest中
            searchRequest.source(searchSourceBuilder);
            // 9.设置option
            searchRequest.indicesOptions(IndicesOptions.fromOptions(true,true,true,false));
            // 10.调用方法查询数据
            SearchResponse searchResponse= esClient.search(searchRequest,RequestOptions.DEFAULT);
            T model=getSingleSearchResult(searchResponse,clazz);
            if (model!=null)
                return R.data(model);
            else
                return R.fail("未获取到数据");
        }catch (Exception e){
            e.printStackTrace();
            return R.fail("通过主键Id获取单条数据失败:"+e.getMessage());
        }
    }

    /**
     * 通过提交获取单条数据
     * @param index
     * @param matchQueryBuilder
     * @param ascs
     * @param descs
     * @param <T>
     * @return
     */
    public <T> R<List<T>> getList(String index,QueryBuilder matchQueryBuilder,Class<T> clazz,List<String> ascs,List<String> descs){
        try {
            // 1.构建SearchRequest请求对象，指定索引库
            SearchRequest searchRequest=new SearchRequest();
            searchRequest.indices(index);
            // 2.构建SearchSourceBuilder查询对象
            SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
            // 3.构建QueryBuilder对象指定查询方式和查询条件
            // 4.将QueryBuilder对象设置到SearchSourceBuilder中
            searchSourceBuilder.query(matchQueryBuilder);
            // 5.排序
            sort(searchSourceBuilder,ascs,descs);
            // 8.将SearchSourceBuilder设置到SearchRequest中
            searchRequest.source(searchSourceBuilder);
            // 9.设置option
            searchRequest.indicesOptions(IndicesOptions.fromOptions(true,true,true,false));
            // 10.调用方法查询数据
            SearchResponse searchResponse= esClient.search(searchRequest,RequestOptions.DEFAULT);
            List<T> list=getSearchResult(searchResponse,clazz);
            if (list!=null)
                return R.data(list);
            else
                return R.fail("未获取到数据");
        }catch (Exception e){
            e.printStackTrace();
            return R.fail("通过条件获取列表数据失败:"+e.getMessage());
        }
    }

    /**
     * es排序
     * @param searchSourceBuilder
     * @param ascs
     * @param descs
     */
    private void sort(SearchSourceBuilder searchSourceBuilder,List<String> ascs,List<String> descs){
        // 7.排序
        Boolean isSort=false;
        if (ascs!=null&&ascs.size()>0){
            for (String asc : ascs) {
                searchSourceBuilder.sort(asc, SortOrder.ASC);
            }
            isSort=true;
        }
        if (descs!=null&&descs.size()>0){
            for (String desc : descs) {
                searchSourceBuilder.sort(desc, SortOrder.DESC);
            }
            isSort=true;
        }
        if(!isSort){
            //  ES 给出的评分，根据评分来排序
            searchSourceBuilder.sort("_score", SortOrder.DESC);
        }
    }

    /**
     * 获取总数
     * @param index
     * @param matchQueryBuilder
     * @return
     */
    public R<Long> getRecordCount(String index, QueryBuilder matchQueryBuilder){
        // 通过CountRequest查询获得count
        CountRequest countRequest = new CountRequest();
        // 绑定索引名
        countRequest.indices(index);
        countRequest.query(matchQueryBuilder);

        CountResponse response = null;
        try {
            response = esClient.count(countRequest, RequestOptions.DEFAULT);
            return R.data(response.getCount());
        } catch (IOException e) {
            e.printStackTrace();
            return R.fail("查询总数出错:"+e.getMessage());
        }
    }

    /**
     * 将查询结果转为list
     * @param searchResponse
     * @param <T>
     * @return
     */
    private <T> List<T> getSearchResult(SearchResponse searchResponse,Class<T> clazz) {
        SearchHit[] searchHits=searchResponse.getHits().getHits();

        List<T> dataList=new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            String str= JSON.toJSONString(searchHit.getSourceAsMap());
            dataList.add(JSON.parseObject(str,clazz));
            //dataList.add(objectMapper.convertValue(searchHit.getSourceAsMap(),clazz));
        }

        return dataList;
    }

    /**
     * 将查询结果转为list
     * @param searchResponse
     * @param <T>
     * @return
     */
    private <T> T getSingleSearchResult(SearchResponse searchResponse,Class<T> clazz) {
        SearchHit[] searchHits=searchResponse.getHits().getHits();

        for (SearchHit searchHit : searchHits) {
            String str= JSON.toJSONString(searchHit.getSourceAsMap());
            return JSON.parseObject(str,clazz);
            //return objectMapper.convertValue(searchHit.getSourceAsMap(),clazz);
        }

        return null;
    }

    /**
     * 查询条件构造：like
     * @param matchQueryBuilder
     * @param val
     */
    public void genWhereToLike(BoolQueryBuilder matchQueryBuilder, Object val,String fieldName){
        if (Objects.nonNull(val)){
            matchQueryBuilder.must(QueryBuilders.wildcardQuery(fieldName,"*"+val+"*"));
        }
    }

    /**
     * 查询条件构造
     * @param matchQueryBuilder
     * @param val
     */
    public void genWhere(BoolQueryBuilder matchQueryBuilder, Object val,String fieldName){
        if (Objects.nonNull(val)){
            matchQueryBuilder.must(QueryBuilders.matchQuery(fieldName,val.toString()));
        }
    }

    /**
     * 查询条件构造:大于gt
     * @param matchQueryBuilder
     * @param val
     */
    public void genWhereToGT(BoolQueryBuilder matchQueryBuilder, Object val,String fieldName){
        if (Objects.nonNull(val)){
            matchQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).gt(val));
        }
    }

    /**
     * 查询条件构造:小于lt
     * @param matchQueryBuilder
     * @param val
     */
    public void genWhereToLT(BoolQueryBuilder matchQueryBuilder, Object val,String fieldName){
        if (Objects.nonNull(val)){
            matchQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).lt(val));
        }
    }

    /**
     * 查询条件构造：区间
     * @param matchQueryBuilder
     * @param start
     * @param end
     * @param fieldName
     */
    public void genWhereToDateRange(BoolQueryBuilder matchQueryBuilder, Date start, Date end, String fieldName){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (start!=null&&end!=null){
            matchQueryBuilder.must(QueryBuilders.rangeQuery(fieldName)
                    .from(dateFormat.format(start))
                    .to(dateFormat.format(end)));
        }else if (start!=null){
            matchQueryBuilder.must(QueryBuilders.rangeQuery(fieldName)
                    .from(dateFormat.format(start)));
        }else if(end!=null){
            matchQueryBuilder.must(QueryBuilders.rangeQuery(fieldName)
                    .to(dateFormat.format(end)));
        }
    }
}
