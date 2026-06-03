package com.atguigu.tingshu.search;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class UpperAlbumTimingTest {

    private static final String BASE_URL = "http://localhost:8502/api/search/albumInfo/upperAlbum/";
    private static final int TOTAL = 1600;
    private static final int THREADS = 4;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void batchUpperAlbum() throws InterruptedException {
        long totalStart = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        for (long albumId = 1; albumId <= TOTAL; albumId++) {
            long id = albumId;
            executor.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(
                            BASE_URL + id, String.class);
                    long elapsed = System.currentTimeMillis() - start;

                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                        log.info("id={}, 耗时={}ms, 成功", id, elapsed);
                    } else {
                        failCount.incrementAndGet();
                        log.warn("id={}, 耗时={}ms, HTTP状态异常: {}", id, elapsed, response.getStatusCode());
                    }
                } catch (Exception e) {
                    long elapsed = System.currentTimeMillis() - start;
                    failCount.incrementAndGet();
                    log.error("id={}, 耗时={}ms, 异常: {}", id, elapsed, e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.MINUTES);

        long totalElapsed = System.currentTimeMillis() - totalStart;
        log.info("==========================================");
        log.info("全部完成!");
        log.info("线程数: {}", THREADS);
        log.info("总耗时: {}ms ({}s)", totalElapsed, totalElapsed / 1000.0);
        log.info("成功: {}, 失败: {}, 总计: {}", successCount.get(), failCount.get(), TOTAL);
        log.info("平均耗时: {}ms", (double) totalElapsed / TOTAL);
        log.info("吞吐量: {} req/s", TOTAL * 1000.0 / totalElapsed);
        log.info("==========================================");
    }
}
