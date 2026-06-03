package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.search.repo.AlbumIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


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


    @Override
    public void upperAlbum(Long albumId) {


        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();


        // 1. 根据albumId查询专辑信息
        Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
        AlbumInfo albumInfo = albumInfoResult.getData();
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);


        // 2. 根据albumId查询专辑专辑属性信息


        // 3. 根据albumId查询专辑状态 - 播放量等


        // 4. 根据albumId查询分类信息
        Result<BaseCategoryView> categoryViewResult = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id());
        BeanUtils.copyProperties(categoryViewResult.getData(), albumInfoIndex);


        // 5. 根据albumId查询所属人信息
        Result<UserInfoVo> userInfoVo = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
        BeanUtils.copyProperties(userInfoVo.getData(), albumInfoIndex);


//        albumInfoIndex.setId();
//        albumInfoIndex.setAlbumTitle();
//        albumInfoIndex.setAlbumIntro();
//        albumInfoIndex.setAnnouncerName();
//        albumInfoIndex.setCoverUrl();
//        albumInfoIndex.setIncludeTrackCount();
//        albumInfoIndex.setIsFinished();
//        albumInfoIndex.setPayType();
//        albumInfoIndex.setCreateTime();
//        albumInfoIndex.setCategory1Id();
//        albumInfoIndex.setCategory2Id();
//        albumInfoIndex.setCategory3Id();
//        albumInfoIndex.setPlayStatNum();
//        albumInfoIndex.setSubscribeStatNum();
//        albumInfoIndex.setBuyStatNum();
//        albumInfoIndex.setCommentStatNum();
//        albumInfoIndex.setHotScore();
//        albumInfoIndex.setAttributeValueIndexList();


        albumIndexRepository.save(albumInfoIndex);

        return;
    }
}
