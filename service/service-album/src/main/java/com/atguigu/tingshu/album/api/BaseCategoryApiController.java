package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album/category")
public class BaseCategoryApiController {

    @Resource
    private BaseCategoryService baseCategoryService;


    @GetMapping("getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryView(@PathVariable Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryService.getCategoryView(category3Id);
        return Result.ok(baseCategoryView);
    }


    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList() {

        List<JSONObject> list = baseCategoryService.getBaseCategoryList();

        return Result.ok(list);
    }


    @GetMapping("findAttribute/{category1Id}")
    public Result findAttribute(@PathVariable Long category1Id) {

        List<BaseAttribute> list = baseCategoryService.findAttribute(category1Id);

        return Result.ok(list);
    }

    /**
     * 根据一级分类id查询三级分类
     */
    @GetMapping("findTopBaseCategory3/{category1Id}")
    public Result findTopBaseCategory3(@PathVariable Long category1Id) {

        List<BaseCategory3> list = baseCategoryService.findTopBaseCategory3(category1Id);

        return Result.ok(list);
    }


}

