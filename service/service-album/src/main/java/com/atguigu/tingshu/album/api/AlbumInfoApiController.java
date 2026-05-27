package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
public class AlbumInfoApiController {

    @Resource
    private AlbumInfoService albumInfoService;


    /**
     * 分页查询自己的专辑
     */
    @PostMapping("findUserAlbumPage/{startPage}/{limit}")
    public Result findUserAlbumPage(@RequestBody AlbumInfoQuery albumInfoQuery,
                                  @PathVariable Long startPage,
                                  @PathVariable Long limit) {
        albumInfoQuery.setUserId(1L); // TODO userid

        Page<AlbumListVo> page = new Page<>(startPage, limit);
        IPage<AlbumListVo> pageModel = albumInfoService.findUserAlbumPage(page, albumInfoQuery);
        return Result.ok(pageModel);
    }


    /**
     * 新建专辑
     * @param albumInfoVo
     * @return
     */
    @PostMapping("saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {

        albumInfoService.saveAlbumInfo(albumInfoVo);
        return Result.ok();
    }
}


