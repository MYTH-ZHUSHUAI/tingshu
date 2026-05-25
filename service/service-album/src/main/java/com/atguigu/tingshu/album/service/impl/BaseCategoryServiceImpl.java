package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl
        extends ServiceImpl<BaseCategory1Mapper, BaseCategory1>
        implements BaseCategoryService {

    @Resource
    private BaseCategory1Mapper baseCategory1Mapper;

    @Resource
    private BaseCategory2Mapper baseCategory2Mapper;

    @Resource
    private BaseCategory3Mapper baseCategory3Mapper;


    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Resource
    private BaseAttributeMapper baseAttributeMapper;


    @Override
    public List<BaseAttribute> findAttribute(Long category1Id) {
        return baseAttributeMapper.selectAttribute(category1Id);
    }


    @Override
    public List<JSONObject> getBaseCategoryList() {

        // 1. 查询所有数据
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        // 2. 创建List封装最终数据
        List<JSONObject> finalList = new ArrayList<>();

        // 3. 封装 1 级分类
        // map的 k：指定的BaseCategoryView::getCategory1Id
        // map的 v：值为 getCategory1Id 所有数据的集合
        Map<Long, List<BaseCategoryView>> map1 = baseCategoryViews
                .stream()
                .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));


        // 遍历map，封装 1 级部分
        map1.forEach((categoryId1, baseCategoryViews1) -> {

            // JSONObject 等于 Map
            JSONObject jsonObject1 = new JSONObject();

            jsonObject1.put("categoryId", categoryId1);
            jsonObject1.put("categoryName", baseCategoryViews1.get(0).getCategory1Name());

            List<JSONObject> baseCategoryViews2List = new ArrayList<>();

            // 封装 2 级分类
            // map2的 k：指定的BaseCategoryView::getCategory2Id
            // map2的 v：值为 getCategory2Id 所有数据的集合
            Map<Long, List<BaseCategoryView>> map2 = baseCategoryViews1
                    .stream()
                    .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));


            map2.forEach((categoryId2, baseCategoryViews2) -> {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("categoryId", categoryId2);
                jsonObject2.put("categoryName", baseCategoryViews2.get(0).getCategory2Name());

                List<JSONObject> baseCategoryViews3List = new ArrayList<>();

                Map<Long, List<BaseCategoryView>> map3 = baseCategoryViews2
                        .stream()
                        .collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));

                map3.forEach((categoryId3, baseCategoryViews3) -> {
                    JSONObject jsonObject3 = new JSONObject();

                    jsonObject3.put("categoryId", categoryId3);
                    jsonObject3.put("categoryName", baseCategoryViews3.get(0).getCategory3Name());

                    baseCategoryViews3List.add(jsonObject3);
                });


                jsonObject2.put("categoryChild", baseCategoryViews3List);
                baseCategoryViews2List.add(jsonObject2);
            });


            jsonObject1.put("categoryChild", baseCategoryViews2List);

            finalList.add(jsonObject1);
        });


        return finalList;
    }


}
