package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.search.repo.AlbumIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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

    @Override
    public void upperAlbum(Long albumId) {

        // Phase 1: 并行获取专辑信息 + 统计数据
        CompletableFuture<AlbumInfo> albumFuture = CompletableFuture.supplyAsync(
                () -> albumInfoFeignClient.getAlbumInfo(albumId).getData(), albumUpperExecutor);
        CompletableFuture<AlbumStatVo> statFuture = CompletableFuture.supplyAsync(
                () -> albumInfoFeignClient.getAlbumStatVo(albumId).getData(), albumUpperExecutor);

        AlbumInfo albumInfo = albumFuture.join();
        if (albumInfo == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
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
