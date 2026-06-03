package com.atguigu.tingshu.search;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class UpperAlbumTimingTest {

    private static final String BASE_URL = "http://localhost:8502/api/search/albumInfo/upperAlbum/";

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void batchUpperAlbum() {
        long totalStart = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        for (long albumId = 1; albumId <= 1600; albumId++) {
            long start = System.currentTimeMillis();
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        BASE_URL + albumId, String.class);
                long elapsed = System.currentTimeMillis() - start;

                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount++;
                    log.info("id={}, 耗时={}ms, 成功", albumId, elapsed);
                } else {
                    failCount++;
                    log.warn("id={}, 耗时={}ms, HTTP状态异常: {}", albumId, elapsed, response.getStatusCode());
                }
            } catch (Exception e) {
                long elapsed = System.currentTimeMillis() - start;
                failCount++;
                log.error("id={}, 耗时={}ms, 异常: {}", albumId, elapsed, e.getMessage());
            }
        }

        long totalElapsed = System.currentTimeMillis() - totalStart;
        log.info("==========================================");
        log.info("全部完成!");
        log.info("总耗时: {}ms ({}s)", totalElapsed, totalElapsed / 1000.0);
        log.info("成功: {}, 失败: {}, 总计: {}", successCount, failCount, 1600);
        log.info("平均耗时: {}ms", totalElapsed / 1600.0);
        log.info("==========================================");
    }
}
