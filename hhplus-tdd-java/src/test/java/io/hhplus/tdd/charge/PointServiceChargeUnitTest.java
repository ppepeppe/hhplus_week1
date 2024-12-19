package io.hhplus.tdd.charge;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class PointServiceChargeUnitTest {
    @Mock
    private UserPointTable userPointTable; // Mock 객체

    @InjectMocks
    private PointService pointService; // 테스트 대상 클래스

    public PointServiceChargeUnitTest() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }
    @Test
    public void 포인트충전_성공200ok() {
        // Given 아이디가 1, 포인트가 1000인 user 세팅
        long userId = 1L;
        UserPoint existingUser = new UserPoint(userId, 1000L, System.currentTimeMillis());
        // 추가 충전 500
        long chargeAmount = 500L;

        // 추가 중전 금액이 포함된 1500 값이 update 되도록 mock 세팅
        when(userPointTable.selectById(userId)).thenReturn(existingUser);
        when(userPointTable.insertOrUpdate(userId, 1500L))
               .thenReturn(new UserPoint(userId, 1500L, System.currentTimeMillis()));

        // When
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);
        // Then
        assertEquals(1500L, result.point()); // 기존 포인트에 충전 금액이 더해졌는지 검증
        // Verify 메서드 호출 검증
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, 1500L);
    }

    @Test
    public void 포인트충전_유저가_없을시_200ok() {
        // Given
        long userId = 999L; // 존재하지 않는 유저 ID
        long chargeAmount = 500L;

        // selectById가 존재하지 않는 유저를 조회하면 기본값 반환
        when(userPointTable.selectById(userId)).thenReturn(null);
        // insertOrUpdate로 새로운 유저가 추가되고 충전 금액이 저장됨
        when(userPointTable.insertOrUpdate(userId, chargeAmount))
                .thenReturn(new UserPoint(userId, chargeAmount, System.currentTimeMillis()));

        // When
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);

        // Then
        assertEquals(chargeAmount, result.point()); // 충전 금액이 그대로 반영되는지 검증
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, chargeAmount);
    }

    @Test
    public void 포인트충전_포인트0_이하_충전_금액은_0보다_커야_합니다() {
        // Given
        long userId = 1L;
        long negativeAmount = -500L;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, negativeAmount);
        });

        // 예외 메시지 검증
        assertEquals("충전 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    public void 포인트충전_포인트금액_초과_충전_금액은_1000000원을_초과할_수_없습니다() {
        // Given
        long userId = 1L;
        long excessiveAmount = 1_000_001L; // 초과 금액

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, excessiveAmount);
        });

        // 예외 메시지 검증
        assertEquals("충전 금액은 1,000,000원을 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 포인트충전_합계초과_포인트_합계는_1000000원을_초과할_수_없습니다() {
        //Given
        long userId = 1L;
        long existingPoints = 900_000L;
        long chargePoints = 200_000L;

        UserPoint existingUser = new UserPoint(userId, existingPoints, System.currentTimeMillis());
        // Mock 설정: 기존 유저의 포인트 반환
        when(userPointTable.selectById(userId)).thenReturn(existingUser);

        // When & Then: 예외 발생 검증
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, chargePoints);
        });

        assertEquals("포인트 합계는 1,000,000원을 초과할 수 없습니다.", exception.getMessage());
        // Verify 메소드 호출 검증
        verify(userPointTable, times(1)).selectById(userId); // selectById 호출 검증
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong()); // insertOrUpdate는 호출되지 않아야 함
    }

    @Test
    public void 포인트충전_200ok() {
        // Given
        long userId = 1L;
        long existingPoints = 800_000L; // 기존 포인트
        long chargeAmount = 200_000L; // 합계 1,000,000L → 허용됨

        UserPoint existingUser = new UserPoint(userId, existingPoints, System.currentTimeMillis());

        // Mock 설정
        when(userPointTable.selectById(userId)).thenReturn(existingUser);
        when(userPointTable.insertOrUpdate(userId, 1_000_000L))
                .thenReturn(new UserPoint(userId, 1_000_000L, System.currentTimeMillis()));

        // When
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);

        // Then
        assertEquals(1_000_000L, result.point()); // 포인트 합산 검증
        // Verify 메소드 호출 검증
        verify(userPointTable, times(1)).selectById(userId); // selectById 호출 검증
        verify(userPointTable, times(1)).insertOrUpdate(userId, 1_000_000L); // insertOrUpdate 호출 검증
    }
}
