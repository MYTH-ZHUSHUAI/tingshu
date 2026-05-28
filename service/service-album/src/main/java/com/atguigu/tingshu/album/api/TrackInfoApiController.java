package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
public class TrackInfoApiController {

    @Resource
    private TrackInfoService trackInfoService;

    @Resource
    private VodService vodService;

    @PostMapping("uploadTrack")
    public Result<Map<String, Object>> uploadTrack(MultipartFile file) {
        Map<String, Object> map = vodService.uploadTrack(file);
        return Result.ok(map);
    }

    @PostMapping("saveTrackInfo")
    public Result<Void> saveTrackInfo(@RequestBody TrackInfoVo trackInfoVo) {
        trackInfoService.saveTrackInfo(trackInfoVo);
        return Result.ok();
    }

    @PostMapping("findUserTrackPage/{startPage}/{limit}")
    public Result<IPage<TrackListVo>> findUserTrackPage(@PathVariable Long startPage,
                                                         @PathVariable Long limit,
                                                         @RequestBody TrackInfoQuery trackInfoQuery) {
        Page<TrackListVo> page = new Page<>(startPage, limit);
        IPage<TrackListVo> pageModel = trackInfoService.findUserTrackPage(page, trackInfoQuery);
        return Result.ok(pageModel);
    }

    @DeleteMapping("removeTrackInfo/{trackId}")
    public Result<Void> removeTrackInfo(@PathVariable Long trackId) {
        trackInfoService.removeTrackInfo(trackId);
        return Result.ok();
    }

    @GetMapping("getTrackInfo/{trackId}")
    public Result<TrackInfo> getTrackInfo(@PathVariable Long trackId) {
        TrackInfo trackInfo = trackInfoService.getTrackInfo(trackId);
        return Result.ok(trackInfo);
    }

    @PutMapping("updateTrackInfo/{trackId}")
    public Result<Void> updateTrackInfo(@PathVariable Long trackId,
                                         @RequestBody TrackInfoVo trackInfoVo) {
        trackInfoService.updateTrackInfo(trackId, trackInfoVo);
        return Result.ok();
    }
}