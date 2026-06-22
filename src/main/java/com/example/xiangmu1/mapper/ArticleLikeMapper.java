package com.example.xiangmu1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiangmu1.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    @Select("""
            SELECT article_id AS articleId, COUNT(*) AS likeCount
            FROM article_like
            GROUP BY article_id
            ORDER BY likeCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> selectHotArticleLikeCounts(@Param("limit") int limit);
}
