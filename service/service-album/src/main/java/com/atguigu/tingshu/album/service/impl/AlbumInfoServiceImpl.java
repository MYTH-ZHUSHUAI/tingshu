package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlbumInfoServiceImpl
        extends ServiceImpl<AlbumInfoMapper, AlbumInfo>
        implements AlbumInfoService {

    @Resource
    private AlbumInfoMapper albumInfoMapper;

    @Resource
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Resource
    private AlbumStatMapper albumStatMapper;

    @Resource
    private AlbumAttributeValueService albumAttributeValueService;

    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> page, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.findUserAlbumPage(page, albumInfoQuery);
    }


    /**
     * 表在同一个数据库中，可以实现事务
     * 如果在不同库中，需要分布式事务
     *
     * @param albumInfoVo
     */
    @Override
    @Transactional
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo) {

        // 1. 添加专辑的基本信息 album_info
        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);

        // 设置userid
        albumInfo.setUserId(1L); // TODO userid
        // 设置系统审核状态
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);  // TODO 系统审核功能
        // 设置免费数
        if (!albumInfoVo.getPayType().equals(SystemConstant.ALBUM_PAY_TYPE_FREE)) {
            albumInfo.setTracksForFree(3);
        }

        // id 自动回填
        this.save(albumInfo);

        // 2. 专辑标签名称和标签值 album_attribute_value

        List<AlbumAttributeValue> albumAttributeValueVoList = albumInfo.getAlbumAttributeValueVoList();

        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {

            List<AlbumAttributeValue> albumAttributeValues =
                    albumAttributeValueVoList.stream().map(albumAttributeValueVo -> {
                        AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();

                        BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);

                        // 手动设置专辑id
                        albumAttributeValue.setAlbumId(albumInfo.getId());

                        return albumAttributeValue;
                    }).toList();


            albumAttributeValueService.saveBatch(albumAttributeValues);
        }


        // 3. 添加专辑订阅量、播放量等初始值 album_stat

        //专辑统计 0401-播放量 0402-订阅量 0403-购买量 0403-评论数
        saveAlbumStat4Times(albumInfo, SystemConstant.ALBUM_STAT_PLAY);
        saveAlbumStat4Times(albumInfo, SystemConstant.ALBUM_STAT_SUBSCRIBE);
        saveAlbumStat4Times(albumInfo, SystemConstant.ALBUM_STAT_BROWSE);
        saveAlbumStat4Times(albumInfo, SystemConstant.ALBUM_STAT_COMMENT);
    }


    private void saveAlbumStat4Times(AlbumInfo albumInfo, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumInfo.getId());
        albumStat.setStatType(statType);
        albumStat.setStatNum(0);

        albumStatMapper.insert(albumStat);
    }

}
