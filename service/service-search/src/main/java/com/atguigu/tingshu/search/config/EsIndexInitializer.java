package com.atguigu.tingshu.search.config;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class EsIndexInitializer {

    private static final Logger LOG = Logger.getLogger(EsIndexInitializer.class.getName());

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void initIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(AlbumInfoIndex.class);
        if (!indexOps.exists()) {
            indexOps.create();
            LOG.info("ES 索引 [albuminfo] 创建成功");
        }
    }
}
