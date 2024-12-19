package io.hhplus.tdd.use;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PointServiceUseUnitTest {
    @Mock
    private UserPointTable userPointTable; // Mock 객체

    @InjectMocks
    private PointService pointService; // 테스트 대상 클래스

    public PointServiceUseUnitTest() {
       MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    public void 포인트사용_성공200ok() {
        // Given
        long userId = 1L;
        long existingPoints = 1000L;
        long usePoints = 500L;

        UserPoint existingUser = new UserPoint(userId, existingPoints, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUser);
        when(userPointTable.insertOrUpdate(userId, 500L))
                .thenReturn(new UserPoint(userId, 500L, System.currentTimeMillis()));
        // When
        UserPoint result = pointService.updateUserPoint(userId, usePoints);

        // Then
        assertEquals(500L, result.point()); // 차감 후 포인트 검증

        // Verify 메서드 호출 검증
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, 500L);
    }

    @Test
    public void 포인트사용_유저가_없을시200ok() {
        // Given
        long userId = 999L;
        long usePoints = 500L;

        when(userPointTable.selectById(userId)).thenReturn(null);

        // When&Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.updateUserPoint(userId, usePoints));
        assertEquals("존재하지 않는 유저입니다.", exception.getMessage());

        // Verify 메서드 호출 검증
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    public void 포인트사용_사용금액_0이하시_사용_금액은_0보다_커야_합니다() {
        // Given
        long userId = 1L;
        long existingPoints = 1000L;
        long usePoints = -500L;

        UserPoint existingUser = new UserPoint(userId, existingPoints, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.updateUserPoint(userId, usePoints));
        assertEquals("사용 금액은 0보다 커야 합니다.", exception.getMessage());

        // Verify 메서드 호출 검증
        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }

    @Test
    public void 포인트사용_사용금액_초과시_포인트가_부족합니다() {
        // Given
        long userId = 1L;
        long existingPoints = 1000L;
        long usePoints = 1500L;

        UserPoint existingUser = new UserPoint(userId, existingPoints, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(existingUser);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.updateUserPoint(userId, usePoints));
        assertEquals("포인트가 부족합니다.", exception.getMessage());

        // Verify 메서드 호출 검증
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
    }
}
