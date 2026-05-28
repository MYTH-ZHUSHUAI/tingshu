package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.ImageFileService;
import com.atguigu.tingshu.album.service.TrackFileService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.TrackStatService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.model.file.ImageFile;
import com.atguigu.tingshu.model.file.TrackFile;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

    @Resource
    private TrackInfoMapper trackInfoMapper;

    @Resource
    private AlbumInfoMapper albumInfoMapper;

    @Resource
    private TrackStatService trackStatService;

    @Resource
    private TrackFileService trackFileService;

    @Resource
    private ImageFileService imageFileService;

    @Override
    public IPage<TrackListVo> findUserTrackPage(Page<TrackListVo> page, TrackInfoQuery trackInfoQuery) {
        return trackInfoMapper.findUserTrackPage(page, trackInfoQuery);
    }

    @Override
    @Transactional
    public void saveTrackInfo(TrackInfoVo trackInfoVo) {

        // 1. 保存声音基本信息
        TrackInfo trackInfo = new TrackInfo();
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        trackInfo.setUserId(1L); // todo userId
        trackInfo.setSource(SystemConstant.TRACK_SOURCE_UPLOAD);
        trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);

        // orderNum = 当前专辑下 MAX(order_num) + 1
        LambdaQueryWrapper<TrackInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(TrackInfo::getOrderNum)
                .eq(TrackInfo::getAlbumId, trackInfoVo.getAlbumId())
                .orderByDesc(TrackInfo::getOrderNum)
                .last(" limit 1");
        TrackInfo lastTrack = trackInfoMapper.selectOne(wrapper);
        int orderNum = (lastTrack != null && lastTrack.getOrderNum() != null) ? lastTrack.getOrderNum() + 1 : 0;
        trackInfo.setOrderNum(orderNum);

        trackInfoMapper.insert(trackInfo);

        // 2. 初始化声音统计 ×4
        saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY);
        saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT);
        saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE);
        saveTrackStat(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT);

        // 3. 专辑 include_track_count + 1
        LambdaUpdateWrapper<AlbumInfo> albumUpdateWrapper = new LambdaUpdateWrapper<>();
        albumUpdateWrapper.setSql("include_track_count = include_track_count + 1")
                .eq(AlbumInfo::getId, trackInfoVo.getAlbumId());
        albumInfoMapper.update(null, albumUpdateWrapper);

        // 4. 绑定音频文件：refCount+1, status=1
        TrackFile trackFile = trackFileService.getOne(
                new LambdaQueryWrapper<TrackFile>().eq(TrackFile::getMediaFileId, trackInfoVo.getMediaFileId()));
        if (trackFile != null) {
            trackFile.setRefCount(trackFile.getRefCount() + 1);
            trackFile.setStatus(1);
            trackFileService.updateById(trackFile);
        }

        // 5. 绑定封面图片：refCount+1, status=1
        if (trackInfoVo.getCoverUrl() != null) {
            ImageFile imageFile = imageFileService.getOne(
                    new LambdaQueryWrapper<ImageFile>().eq(ImageFile::getFileUrl, trackInfoVo.getCoverUrl()));
            if (imageFile != null) {
                imageFile.setRefCount(imageFile.getRefCount() + 1);
                imageFile.setStatus(1);
                imageFileService.updateById(imageFile);
            }
        }
    }

    @Override
    public TrackInfo getTrackInfo(Long trackId) {
        TrackInfo trackInfo = this.getById(trackId);

        if (trackInfo == null) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }

        return trackInfo;
    }

    @Override
    @Transactional
    public void updateTrackInfo(Long trackId, TrackInfoVo trackInfoVo) {

        // 查询旧数据，用于比对文件变更
        TrackInfo oldTrackInfo = this.getById(trackId);
        String oldMediaFileId = oldTrackInfo != null ? oldTrackInfo.getMediaFileId() : null;
        String oldCoverUrl = oldTrackInfo != null ? oldTrackInfo.getCoverUrl() : null;

        TrackInfo trackInfo = new TrackInfo();
        BeanUtils.copyProperties(trackInfoVo, trackInfo);
        trackInfo.setId(trackId);
        trackInfoMapper.updateById(trackInfo);

        // 音频文件变更：旧-1，新+1
        String newMediaFileId = trackInfoVo.getMediaFileId();
        if (newMediaFileId != null && !newMediaFileId.equals(oldMediaFileId)) {
            if (oldMediaFileId != null) {
                TrackFile oldTrackFile = trackFileService.getOne(
                        new LambdaQueryWrapper<TrackFile>().eq(TrackFile::getMediaFileId, oldMediaFileId));
                if (oldTrackFile != null && oldTrackFile.getRefCount() > 0) {
                    oldTrackFile.setRefCount(oldTrackFile.getRefCount() - 1);
                    oldTrackFile.setStatus(oldTrackFile.getRefCount() > 0 ? 1 : 0);
                    trackFileService.updateById(oldTrackFile);
                }
            }
            TrackFile newTrackFile = trackFileService.getOne(
                    new LambdaQueryWrapper<TrackFile>().eq(TrackFile::getMediaFileId, newMediaFileId));
            if (newTrackFile != null) {
                newTrackFile.setRefCount(newTrackFile.getRefCount() + 1);
                newTrackFile.setStatus(1);
                trackFileService.updateById(newTrackFile);
            }
        }

        // 封面图片变更：旧-1，新+1
        String newCoverUrl = trackInfoVo.getCoverUrl();
        if (newCoverUrl != null && !newCoverUrl.equals(oldCoverUrl)) {
            if (oldCoverUrl != null) {
                ImageFile oldImageFile = imageFileService.getOne(
                        new LambdaQueryWrapper<ImageFile>().eq(ImageFile::getFileUrl, oldCoverUrl));
                if (oldImageFile != null && oldImageFile.getRefCount() > 0) {
                    oldImageFile.setRefCount(oldImageFile.getRefCount() - 1);
                    oldImageFile.setStatus(oldImageFile.getRefCount() > 0 ? 1 : 0);
                    imageFileService.updateById(oldImageFile);
                }
            }
            ImageFile newImageFile = imageFileService.getOne(
                    new LambdaQueryWrapper<ImageFile>().eq(ImageFile::getFileUrl, newCoverUrl));
            if (newImageFile != null) {
                newImageFile.setRefCount(newImageFile.getRefCount() + 1);
                newImageFile.setStatus(1);
                imageFileService.updateById(newImageFile);
            }
        }
    }

    @Override
    @Transactional
    public void removeTrackInfo(Long trackId) {

        TrackInfo trackInfo = this.getById(trackId);
        if (trackInfo == null) {
            return;
        }

        // 1. 删除声音记录
        trackInfoMapper.deleteById(trackId);

        // 2. 删除声音统计
        trackStatService.remove(new LambdaQueryWrapper<TrackStat>().eq(TrackStat::getTrackId, trackId));

        // 3. 专辑 include_track_count - 1
        LambdaUpdateWrapper<AlbumInfo> albumUpdateWrapper = new LambdaUpdateWrapper<>();
        albumUpdateWrapper.setSql("include_track_count = include_track_count - 1")
                .eq(AlbumInfo::getId, trackInfo.getAlbumId());
        albumInfoMapper.update(null, albumUpdateWrapper);

        // 4. 音频文件引用计数-1
        TrackFile trackFile = trackFileService.getOne(
                new LambdaQueryWrapper<TrackFile>().eq(TrackFile::getMediaFileId, trackInfo.getMediaFileId()));
        if (trackFile != null && trackFile.getRefCount() > 0) {
            trackFile.setRefCount(trackFile.getRefCount() - 1);
            trackFile.setStatus(trackFile.getRefCount() > 0 ? 1 : 0);
            trackFileService.updateById(trackFile);
        }

        // 5. 封面图片引用计数-1
        if (trackInfo.getCoverUrl() != null) {
            ImageFile imageFile = imageFileService.getOne(
                    new LambdaQueryWrapper<ImageFile>().eq(ImageFile::getFileUrl, trackInfo.getCoverUrl()));
            if (imageFile != null && imageFile.getRefCount() > 0) {
                imageFile.setRefCount(imageFile.getRefCount() - 1);
                imageFile.setStatus(imageFile.getRefCount() > 0 ? 1 : 0);
                imageFileService.updateById(imageFile);
            }
        }
    }

    private void saveTrackStat(Long trackId, String statType) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(statType);
        trackStat.setStatNum(0);
        trackStatService.save(trackStat);
    }
}
