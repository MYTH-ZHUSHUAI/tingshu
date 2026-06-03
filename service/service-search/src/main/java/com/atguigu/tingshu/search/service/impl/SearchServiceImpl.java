package com.atguigu.tingshu.search.service.impl;

import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
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

        // 1. 专辑基本信息
        AlbumInfo albumInfo = albumInfoFeignClient.getAlbumInfo(albumId).getData();
        if (albumInfo == null) {
            log.error("专辑不存在，albumId: {}", albumId);
            return;
        }
        BeanUtils.copyProperties(albumInfo, albumInfoIndex);
        // BeanUtils 无法转换 Integer → String
        albumInfoIndex.setIsFinished(albumInfo.getIsFinished() != null ? albumInfo.getIsFinished().toString() : "0");

        // 2. 专辑属性值
        List<AlbumAttributeValue> attrList = albumInfo.getAlbumAttributeValueVoList();
        if (attrList != null) {
            albumInfoIndex.setAttributeValueIndexList(attrList.stream().map(attr -> {
                AttributeValueIndex idx = new AttributeValueIndex();
                idx.setAttributeId(attr.getAttributeId());
                idx.setValueId(attr.getValueId());
                return idx;
            }).toList());
        }


        // 3. 分类信息
        BaseCategoryView categoryView = categoryFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData();
        if (categoryView != null) {
            albumInfoIndex.setCategory1Id(categoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(categoryView.getCategory2Id());
            albumInfoIndex.setCategory3Id(categoryView.getCategory3Id());
        }

        // 4. 主播信息
        UserInfoVo userInfoVo = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId()).getData();
        albumInfoIndex.setAnnouncerName(userInfoVo != null ? userInfoVo.getNickname() : "");

        // 5. 统计数据（播放量、订阅量、购买量、评论数）
        AlbumStatVo statVo = albumInfoFeignClient.getAlbumStatVo(albumId).getData();
        if (statVo != null) {
            albumInfoIndex.setPlayStatNum(statVo.getPlayStatNum() != null ? statVo.getPlayStatNum() : 0);
            albumInfoIndex.setSubscribeStatNum(statVo.getSubscribeStatNum() != null ? statVo.getSubscribeStatNum() : 0);
            albumInfoIndex.setBuyStatNum(statVo.getBuyStatNum() != null ? statVo.getBuyStatNum() : 0);
            albumInfoIndex.setCommentStatNum(statVo.getCommentStatNum() != null ? statVo.getCommentStatNum() : 0);
        }




        // 6. 热度值
        albumInfoIndex.setHotScore(0d);

        albumIndexRepository.save(albumInfoIndex);
    }
}
