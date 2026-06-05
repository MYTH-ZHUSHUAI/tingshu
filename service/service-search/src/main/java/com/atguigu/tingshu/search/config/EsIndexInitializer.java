package com.atguigu.tingshu.search.config;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;



/**
 * es索引初始化
 */
@Component
public class EsIndexInitializer {

    private static final Logger LOG = Logger.getLogger(EsIndexInitializer.class.getName());

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void initIndex() {
        IndexOperations albumInfoIndexOps = elasticsearchOperations.indexOps(AlbumInfoIndex.class);
        IndexOperations suggestIndexOps = elasticsearchOperations.indexOps(SuggestIndex.class);
        if (!albumInfoIndexOps.exists()) {
            albumInfoIndexOps.create();
            LOG.info("ES 索引 [albuminfo] 创建成功");
        }

        if (!suggestIndexOps.exists()){
            suggestIndexOps.create();
            LOG.info("ES 索引 [suggestinfo] 创建成功");
        }
    }
}
