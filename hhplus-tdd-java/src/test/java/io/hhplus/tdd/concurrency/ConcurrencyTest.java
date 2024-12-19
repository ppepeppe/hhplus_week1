package io.hhplus.tdd.concurrency;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @BeforeEach
    public void setUp() {
        // 초기 데이터 설정
        userPointTable.insertOrUpdate(1L, 100L); // 유저 ID 1, 포인트 30
    }

    @Test
    public void testConcurrentPointUsage() throws InterruptedException {
        // Given
        long userId = 1L;
        long useAmount = 10L;
        int concurrentRequests = 10; // 동시에 실행할 쓰레드 수 (10회 사용 요청)

        // 동시성 테스트 실행
        chargePointsConcurrently(userId, useAmount, concurrentRequests);

        // Then: 포인트 상태 검증
        UserPoint finalState = pointService.findUserById(userId);

        // 기대 결과는 100 - 10 * 10 = 0 (중복 차감이 발생하면 이보다 큰 값이 나타날 가능성 있음)
        assertEquals(0L, finalState.point(), "포인트가 예상치와 다릅니다.");
    }

    private void chargePointsConcurrently(Long id, Long amount, int times) throws InterruptedException {
        Logger log = LoggerFactory.getLogger(PointController.class);

        ExecutorService executorService = Executors.newFixedThreadPool(times);
        CountDownLatch latch = new CountDownLatch(times);

        List<Future<String>> results = new ArrayList<>(); // 실행 결과 저장

        IntStream.range(0, times).forEach(i -> {
            results.add(executorService.submit(() -> {
                awaitLatch(latch);
                log.debug("Thread {}: 쓰레드 요청 point: {}, id : {}", Thread.currentThread().getName(), amount, id);

                try {
                    pointService.updateUserPoint(id, amount); // 포인트 사용 요청
                    log.debug("Thread {}: 성공 point : {}, id : {}", Thread.currentThread().getName(), amount, id);
                    return "Success";
                } catch (Exception e) {
                    log.error("Thread {}: 실패 id : {}, 에러메세지 : {}", Thread.currentThread().getName(), id, e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }));
            latch.countDown();
        });

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        log.debug("모든 쓰레드 끝");
        // 실행 결과 로깅
        results.forEach(result -> {
            try {
                log.info("성공 쓰레드: {}", result.get());

            } catch (Exception e) {
                log.error("쓰레드 실패: {}", e.getMessage());
            }
        });
        log.debug("동시성 테스트 끝");
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("쓰레드 인터럽트", e);
        }
    }
}
