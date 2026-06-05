package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.common.util.PinYinUtils;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repo.AlbumIndexRepository;
import com.atguigu.tingshu.search.repo.SuggestIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private AlbumIndexRepository albumIndexRepository;

    @Resource
    private SuggestIndexRepository suggestIndexRepository;

    @Resource
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Resource
    private CategoryFeignClient categoryFeignClient;

    @Resource
    private UserInfoFeignClient userInfoFeignClient;

    @Resource
    private ThreadPoolExecutor albumUpperExecutor;

    // 根据yml配置自动创建client
    @Resource
    private ElasticsearchClient elasticsearchClient;


    /**
     * 使用es查询
     * 根据category1Id查询所有三级分类，根据热度排序
     */
    @Override
    public List<Map<String, Object>> channel(Long category1Id) {
        List<BaseCategory3> topCategories = categoryFeignClient.findTopBaseCategory3(category1Id).getData();
        if (CollectionUtils.isEmpty(topCategories)) {
            return List.of();
        }

        Map<Long, BaseCategory3> category3IdToMap = topCategories.stream()
                .collect(Collectors.toMap(BaseCategory3::getId, c -> c));

        List<Long> category3Ids = topCategories.stream().map(BaseCategory3::getId).toList();

        SearchRequest request = buildChannelDsl(category3Ids);

        SearchResponse<AlbumInfoIndex> response;
        try {
            response = elasticsearchClient.search(request, AlbumInfoIndex.class);
        } catch (IOException e) {
            throw new GuiguException(500, "查询失败");
        }

        return parseChannelResult(response, category3IdToMap);
    }

    private SearchRequest buildChannelDsl(List<Long> category3Ids) {
        SearchRequest.Builder reqBuilder = new SearchRequest.Builder();
        reqBuilder.index("albuminfo");
        reqBuilder.size(0);

        reqBuilder.query(q -> q.terms(t -> t
                .field("category3Id")
                .terms(tf -> tf.value(category3Ids.stream().map(FieldValue::of).toList()))
        ));

        reqBuilder.aggregations("groupByCategory3IdAgg", a -> a
                .terms(t -> t.field("category3Id").size(7))
                .aggregations("topTenHotScoreAgg", sa -> sa
                        .topHits(th -> th
                                .size(6)
                                .sort(s -> s.field(f -> f.field("hotScore").order(SortOrder.Desc)))
                                .source(src -> src.filter(f -> f.excludes("attributeValueIndexList")))
                        )
                )
        );

        return reqBuilder.build();
    }

    private List<Map<String, Object>> parseChannelResult(SearchResponse<AlbumInfoIndex> response,
                                                         Map<Long, BaseCategory3> category3IdToMap) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Aggregate> aggs = response.aggregations();
        if (aggs == null) {
            return result;
        }

        Aggregate categoryAgg = aggs.get("groupByCategory3IdAgg");
        if (categoryAgg == null || !categoryAgg.isLterms()) {
            return result;
        }

        List<LongTermsBucket> buckets = categoryAgg.lterms().buckets().array();
        for (LongTermsBucket bucket : buckets) {
            Map<String, Object> map = new HashMap<>();
            map.put("baseCategory3", category3IdToMap.get(bucket.key()));

            Aggregate topAlbumsAgg = bucket.aggregations().get("topTenHotScoreAgg");
            if (topAlbumsAgg != null && topAlbumsAgg.isTopHits()) {
                List<AlbumInfoIndex> albums = topAlbumsAgg.topHits().hits().hits().stream()
                        .map(hit -> {
                            String json = hit.source().toString();
                            return JSON.parseObject(json, AlbumInfoIndex.class);
                        }).toList();
                map.put("list", albums);
            } else {
                map.put("list", List.of());
            }

            result.add(map);
        }

        return result;
    }


    /**
     * 使用es查询专辑 - 主流程
     */
    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) {


        // 1. 构建dsl语句
        SearchRequest request = this.buildQueryDsl(albumIndexQuery);
        // 2. 调用查询方法
        SearchResponse<AlbumInfoIndex> response = null;
        try {
            response = elasticsearchClient.search(request, AlbumInfoIndex.class);
        } catch (IOException e) {
            throw new GuiguException(500, "查询失败");
        }
        //  3. 得到返回的结果集
        AlbumSearchResponseVo responseVO = this.parseSearchResult(response);

        responseVO.setPageSize(albumIndexQuery.getPageSize());
        responseVO.setPageNo(albumIndexQuery.getPageNo());
        // 获取总页数
        long totalPages = (responseVO.getTotal() + albumIndexQuery.getPageSize() - 1) / albumIndexQuery.getPageSize();
        responseVO.setTotalPages(totalPages);
        return responseVO;
    }


    /**
     * 构建查询dsl语句
     */
    private SearchRequest buildQueryDsl(AlbumIndexQuery albumIndexQuery) {

        String keyword = albumIndexQuery.getKeyword();
        Long category1Id = albumIndexQuery.getCategory1Id();
        Long category2Id = albumIndexQuery.getCategory2Id();
        Long category3Id = albumIndexQuery.getCategory3Id();
        List<String> attributeList = albumIndexQuery.getAttributeList();
        String order = albumIndexQuery.getOrder();

        SearchRequest.Builder reqBuilder = new SearchRequest.Builder();
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // must: match_all 保证永远不空
        boolQuery.must(m -> m.matchAll(f -> f));

        // should: 关键词命中加分
        boolQuery.should(s -> s.match(m -> m.field("albumTitle").query(keyword)));
        boolQuery.should(s -> s.match(m -> m.field("albumIntro").query(keyword)));

        // 高亮
        reqBuilder.highlight(h -> h
                .fields("albumTitle", f -> f.preTags("<span style=color:red>").postTags("</span>"))
                .fields("albumIntro", f -> f.preTags("<span style=color:red>").postTags("</span>")));

        // filter: 分类
        if (category1Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category1Id").value(category1Id)));
        }
        if (category2Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category2Id").value(category2Id)));
        }
        if (category3Id != null) {
            boolQuery.filter(f -> f.term(t -> t.field("category3Id").value(category3Id)));
        }

        // filter: 属性 nested 查询
        if (!CollectionUtils.isEmpty(attributeList)) {
            for (String attribute : attributeList) {
                String[] split = attribute.split(":");
                if (split.length == 2) {
                    boolQuery.filter(f -> f.nested(n -> n
                            .path("attributeValueIndexList")
                            .query(q -> q.bool(b -> b
                                    .must(m -> m.term(t -> t.field("attributeValueIndexList.attributeId").value(Long.valueOf(split[0]))))
                                    .must(m -> m.term(t -> t.field("attributeValueIndexList.valueId").value(Long.valueOf(split[1]))))
                            ))));
                }
            }
        }

        // 排序
        if (!StringUtils.hasText(order)) {
            // 默认: _score 优先，hotScore 辅助
            reqBuilder.sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)));
            reqBuilder.sort(s -> s.field(f -> f.field("hotScore").order(SortOrder.Desc)));
        } else {
            String[] split = order.split(":");
            if (split.length == 2) {
                String orderField = switch (split[0]) {
                    case "1" -> "hotScore";
                    case "2" -> "playStatNum";
                    case "3" -> "createTime";
                    default -> null;
                };
                if (orderField != null) {
                    SortOrder sortOrder = "asc".equals(split[1]) ? SortOrder.Asc : SortOrder.Desc;
                    reqBuilder.sort(s -> s.field(f -> f.field(orderField).order(sortOrder)));
                }
            }
            reqBuilder.sort(s -> s.field(f -> f.field("_score").order(SortOrder.Desc)));
        }

        // 字段过滤
        reqBuilder.source(s -> s.filter(f -> f.excludes("attributeValueIndexList")));

        // 分页
        int from = (albumIndexQuery.getPageNo() - 1) * albumIndexQuery.getPageSize();
        reqBuilder.from(from);
        reqBuilder.size(albumIndexQuery.getPageSize());

        reqBuilder.index("albuminfo").query(q -> q.bool(boolQuery.build()));
        return reqBuilder.build();
    }


    /**
     * 解析查询结果
     */
    private AlbumSearchResponseVo parseSearchResult(SearchResponse<AlbumInfoIndex> response) {
        AlbumSearchResponseVo searchResponseVo = new AlbumSearchResponseVo();
        HitsMetadata<AlbumInfoIndex> hits = response.hits();
        searchResponseVo.setTotal(hits.total().value());

        List<Hit<AlbumInfoIndex>> hitList = hits.hits();
        if (!CollectionUtils.isEmpty(hitList)) {
            List<AlbumInfoIndexVo> list = hitList.stream().map(hit -> {
                AlbumInfoIndexVo vo = new AlbumInfoIndexVo();
                BeanUtils.copyProperties(hit.source(), vo);

                Map<String, List<String>> highlightFields = hit.highlight();
                if (highlightFields != null) {
                    if (highlightFields.containsKey("albumTitle")) {
                        vo.setAlbumTitle(highlightFields.get("albumTitle").get(0));
                    }
                    if (highlightFields.containsKey("albumIntro")) {
                        vo.setAlbumIntro(highlightFields.get("albumIntro").get(0));
                    }
                }
                return vo;
            }).toList();
            searchResponseVo.setList(list);
        }
        return searchResponseVo;
    }


    @Override
    public void lowerAlbum(Long albumId) {
        // 下架前先按 albumId 清除搜索提词数据
        suggestIndexRepository.deleteByAlbumId(albumId);
        albumIndexRepository.deleteById(albumId);
    }


    @Override
    public void upperAlbum(Long albumId) {

        // Phase 1: 并行获取专辑信息 + 统计数据
        CompletableFuture<AlbumInfo> albumFuture = CompletableFuture.supplyAsync(
                () -> albumInfoFeignClient.getAlbumInfo(albumId).getData(), albumUpperExecutor);
        CompletableFuture<AlbumStatVo> statFuture = CompletableFuture.supplyAsync(
                () -> albumInfoFeignClient.getAlbumStatVo(albumId).getData(), albumUpperExecutor);

        AlbumInfo albumInfo = albumFuture.join();
        if (albumInfo == null) {
            log.warn("专辑不存在，跳过上架，albumId: {}", albumId);
            return;
        }

        // Phase 2: 并行获取分类信息 + 主播信息（依赖 albumInfo）
        CompletableFuture<BaseCategoryView> categoryFuture = CompletableFuture.supplyAsync(
                () -> categoryFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData(), albumUpperExecutor);
        CompletableFuture<UserInfoVo> userFuture = CompletableFuture.supplyAsync(
                () -> userInfoFeignClient.getUserInfoVo(albumInfo.getUserId()).getData(), albumUpperExecutor);

        AlbumStatVo statVo = statFuture.join();
        BaseCategoryView categoryView = categoryFuture.join();
        UserInfoVo userInfoVo = userFuture.join();

        // 组装 AlbumInfoIndex
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);

        List<AlbumAttributeValue> attrList = albumInfo.getAlbumAttributeValueVoList();
        if (attrList != null) {
            albumInfoIndex.setAttributeValueIndexList(attrList.stream().map(attr -> {
                AttributeValueIndex idx = new AttributeValueIndex();
                idx.setAttributeId(attr.getAttributeId());
                idx.setValueId(attr.getValueId());
                return idx;
            }).toList());
        }

        if (categoryView != null) {
            albumInfoIndex.setCategory1Id(categoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(categoryView.getCategory2Id());
            albumInfoIndex.setCategory3Id(categoryView.getCategory3Id());
        }

        albumInfoIndex.setAnnouncerName(userInfoVo != null ? userInfoVo.getNickname() : "");


        // todo 使用数据库真实数据
        int playNum = ThreadLocalRandom.current().nextInt(1, 10_000_001);
        int subscribeNum = ThreadLocalRandom.current().nextInt(1, 10_000_001);
        int buyNum = ThreadLocalRandom.current().nextInt(1, 100_001);
        int commentNum = ThreadLocalRandom.current().nextInt(1, 100_001);

        albumInfoIndex.setPlayStatNum(playNum);
        albumInfoIndex.setSubscribeStatNum(subscribeNum);
        albumInfoIndex.setBuyStatNum(buyNum);
        albumInfoIndex.setCommentStatNum(commentNum);

        double hotScore = Math.log10(playNum + 1) / 7.0 * 20
                + Math.log10(subscribeNum + 1) / 7.0 * 30
                + Math.log10(buyNum + 1) / 5.0 * 35
                + Math.log10(commentNum + 1) / 5.0 * 15;
        albumInfoIndex.setHotScore(hotScore);

        albumIndexRepository.save(albumInfoIndex);

        // 上架时添加搜索提词数据，支持中文/拼音/首字母自动补全
        saveSuggestIndex(albumInfoIndex);
    }

    /**
     * 保存搜索提词数据到 suggestinfo 索引
     * 三个维度：专辑标题、专辑简介、主播名称
     * 每种维度三种匹配方式：中文原文(keyword)、全拼(keywordPinyin)、首字母(keywordSequence)
     */
    private void saveSuggestIndex(AlbumInfoIndex albumInfoIndex) {
        // 专辑标题提词
        SuggestIndex titleSuggest = new SuggestIndex();
        titleSuggest.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        titleSuggest.setAlbumId(albumInfoIndex.getId());
        titleSuggest.setTitle(albumInfoIndex.getAlbumTitle());
        titleSuggest.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumTitle()}));
        titleSuggest.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAlbumTitle())}));
        titleSuggest.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAlbumTitle())}));
        suggestIndexRepository.save(titleSuggest);

        // 专辑简介提词
        SuggestIndex introSuggest = new SuggestIndex();
        introSuggest.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        introSuggest.setAlbumId(albumInfoIndex.getId());
        introSuggest.setTitle(albumInfoIndex.getAlbumIntro());
        introSuggest.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumIntro()}));
        introSuggest.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAlbumIntro())}));
        introSuggest.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAlbumIntro())}));
        suggestIndexRepository.save(introSuggest);

        // 主播名称提词
        SuggestIndex announcerSuggest = new SuggestIndex();
        announcerSuggest.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        announcerSuggest.setAlbumId(albumInfoIndex.getId());
        announcerSuggest.setTitle(albumInfoIndex.getAnnouncerName());
        announcerSuggest.setKeyword(new Completion(new String[]{albumInfoIndex.getAnnouncerName()}));
        announcerSuggest.setKeywordPinyin(new Completion(new String[]{PinYinUtils.toHanyuPinyin(albumInfoIndex.getAnnouncerName())}));
        announcerSuggest.setKeywordSequence(new Completion(new String[]{PinYinUtils.getFirstLetter(albumInfoIndex.getAnnouncerName())}));
        suggestIndexRepository.save(announcerSuggest);
    }


}
