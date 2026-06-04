package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repo.AlbumIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private AlbumIndexRepository albumIndexRepository;

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
        if (responseVO == null){
            throw new GuiguException(500, "查询结果为空");
        }

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
        Integer pageNo = albumIndexQuery.getPageNo();
        Integer pageSize = albumIndexQuery.getPageSize();

        // 1. 创建查询请求
        SearchRequest.Builder reqbuilder = new SearchRequest.Builder();





        return null;
    }


    /**
     * 解析查询结果
     */
    private AlbumSearchResponseVo parseSearchResult(SearchResponse<AlbumInfoIndex> response) {


        return null;
    }


    @Override
    public void lowerAlbum(Long albumId) {
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

        // 等待剩余结果
        AlbumStatVo statVo = statFuture.join();
        BaseCategoryView categoryView = categoryFuture.join();
        UserInfoVo userInfoVo = userFuture.join();

        // 组装 AlbumInfoIndex
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);
        albumInfoIndex.setIsFinished(albumInfo.getIsFinished() != null ? albumInfo.getIsFinished().toString() : "0");

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

        if (statVo != null) {
            albumInfoIndex.setPlayStatNum(statVo.getPlayStatNum() != null ? statVo.getPlayStatNum() : 0);
            albumInfoIndex.setSubscribeStatNum(statVo.getSubscribeStatNum() != null ? statVo.getSubscribeStatNum() : 0);
            albumInfoIndex.setBuyStatNum(statVo.getBuyStatNum() != null ? statVo.getBuyStatNum() : 0);
            albumInfoIndex.setCommentStatNum(statVo.getCommentStatNum() != null ? statVo.getCommentStatNum() : 0);
        }

        albumInfoIndex.setHotScore(0d);
        albumIndexRepository.save(albumInfoIndex);
    }


}
