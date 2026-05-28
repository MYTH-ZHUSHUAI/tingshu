package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}