package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.AlbumAttributeValueService;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.List;

@Slf4j
@Service
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

    @Resource
    private AlbumInfoMapper albumInfoMapper;

    @Resource
    private AlbumStatMapper albumStatMapper;

    @Resource
    private AlbumAttributeValueService albumAttributeValueService;

    @Resource
    private TrackInfoMapper trackInfoMapper;


    @Override
    public List<AlbumInfo> findUserAllAlbumList(Long userId) {

        Page<AlbumInfo> pageParam = new Page<>(1, 100);


        LambdaQueryWrapper<AlbumInfo> albumInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        albumInfoLambdaQueryWrapper.select(AlbumInfo::getId, AlbumInfo::getAlbumTitle);
        albumInfoLambdaQueryWrapper.eq(AlbumInfo::getUserId, userId);
        albumInfoLambdaQueryWrapper.orderByDesc(AlbumInfo::getCreateTime);

        IPage<AlbumInfo> page = albumInfoMapper.selectPage(pageParam, albumInfoLambdaQueryWrapper);

        return page.getRecords();
    }


    /**
     * 更新专辑信息
     *
     * @param albumInfoVo
     */
    @Override
    @Transactional
    public void updateAlbumInfo(AlbumInfoVo albumInfoVo, Long albumId) {

        AlbumInfo albumInfo = new AlbumInfo();
        BeanUtils.copyProperties(albumInfoVo, albumInfo);
        albumInfo.setId(albumId);
        boolean updated = this.updateById(albumInfo);
        if (!updated) {
            throw new GuiguException(400, "更新失败！");
        }


        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();

        if (CollectionUtils.isEmpty(albumAttributeValueVoList)) {
            return;
        }

        List<AlbumAttributeValue> albumAttributeValueList =
                albumAttributeValueVoList.stream().map(albumAttributeValueVo -> {
                    AlbumAttributeValue albumAttributeValue = new AlbumAttributeValue();
                    albumAttributeValue.setAlbumId(albumId);
                    BeanUtils.copyProperties(albumAttributeValueVo, albumAttributeValue);
                    return albumAttributeValue;
                }).toList();

        albumAttributeValueService.remove(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));

        if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
            albumAttributeValueService.saveBatch(albumAttributeValueList);
        }
    }


    /**
     * 根据 id 查询专辑信息
     *
     * @param albumId
     * @return
     */
    @Override
    public AlbumInfo getAlbumInfo(Long albumId) {

        AlbumInfo albumInfo = this.getById(albumId);

        List<AlbumAttributeValue> list = albumAttributeValueService.list(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));

        albumInfo.setAlbumAttributeValueVoList(list);

        return albumInfo;
    }


    @Override
    public IPage<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> page, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.findUserAlbumPage(page, albumInfoQuery);
    }

    /**
     * 根据id删除专辑
     *
     */
    @Override
    @Transactional
    public void removeAlbumInfo(Long albumId) {

        // 如果专辑不存在直接抛异常
        AlbumInfo albumInfo = this.getById(albumId);
        if (albumInfo == null) {
            throw new GuiguException(400, "专辑不存在！");
        }

        // 查询声音是否存在
        LambdaQueryWrapper<TrackInfo> trackInfoLQW = new LambdaQueryWrapper<>();
        trackInfoLQW.eq(TrackInfo::getAlbumId, albumId);

        Long trackCount = trackInfoMapper.selectCount(trackInfoLQW);

        if (trackCount > 0) {
            // 包含声音，不能删除
            throw new GuiguException(400, "专辑包含声音，不能删除！");

        } else {
            // 不包含声音，则删除专辑、专辑标签、专辑统计
            this.removeById(albumId);
            albumAttributeValueService.remove(new LambdaQueryWrapper<AlbumAttributeValue>().eq(AlbumAttributeValue::getAlbumId, albumId));
            albumStatMapper.delete(new LambdaQueryWrapper<AlbumStat>().eq(AlbumStat::getAlbumId, albumId));
        }


    }


    /**
     *
     * 新增专辑
     * <p>
     * 表在同一个数据库中，可以实现事务
     * 如果在不同库中，需要分布式事务
     *
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

            List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueVoList.stream().map(albumAttributeValueVo -> {
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
