package io.hhplus.tdd;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointServiceTest {
    @Autowired
    private PointService pointService; // 실제 서비스 객체

    @Test
    public void testPointFlow_ChargeAndUse() {
        // Given
        long userId = 1L;
        pointService.chargeUserPoint(userId, 1000L); // ID 1번 유저 포인트 추가

        // Step 1: 포인트 충전
        UserPoint afterCharge = pointService.chargeUserPoint(userId, 500L); // 1000 + 500
        assertEquals(1500L, afterCharge.point(), "충전 후 포인트는 1500이어야 한다.");

        // Step 2: 포인트 사용
        UserPoint afterUse = pointService.updateUserPoint(userId, 300L); // 1500 - 300
        assertEquals(1200L, afterUse.point(), "300 포인트 사용 후 남은 포인트는 1200이어야 한다.");

        // Step 3: 포인트 조회
        UserPoint finalState = pointService.findUserById(userId);
        assertEquals(1200L, finalState.point(), "최종 조회 시 포인트는 1200이어야 한다.");
    }

    @Test
    public void testPointFlow_pointHistory() {
        // Give
        long userId = 2L;
        long chargePoint = 1000L;
        long usePoint = 500L;

        // Given
        pointService.chargeUserPoint(userId, chargePoint);
        pointService.updateUserPoint(userId, usePoint);
        List<PointHistory> userHistory = pointService.findPointHistoryById(userId);

        // Then
        assertEquals(List.of(
                    new PointHistory(4L, userId, chargePoint, TransactionType.CHARGE, 1),
                    new PointHistory(5L, userId, usePoint, TransactionType.USE, 1)),
                userHistory);

    }
}

