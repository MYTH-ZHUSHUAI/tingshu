package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.BaseCategory3;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BaseCategory3Mapper extends BaseMapper<BaseCategory3> {

    @Select("SELECT c3.* " +
            "FROM base_category3 c3 " +
            "JOIN base_category2 c2 ON c3.category2_id = c2.id " +
            "WHERE c2.category1_id = #{category1Id} AND c3.is_top = 1 " +
            "ORDER BY c3.order_num LIMIT 7")
    List<BaseCategory3> selectTopByCategory1Id(@Param("category1Id") Long category1Id);
}
