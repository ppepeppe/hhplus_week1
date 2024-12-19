package io.hhplus.tdd.select;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PointServiceSelectUnitTest {
    @Mock
    private UserPointTable userPointTable; // Mock 객체

    @InjectMocks
    private PointService pointService; // 테스트 대상 클래스

    public PointServiceSelectUnitTest() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    public void 포인트조회_성공200ok() {
        // Given
        long userId = 1L;
        UserPoint mockPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(mockPoint);

        // When
        UserPoint result = pointService.findUserById(userId);

        // Then
        assertEquals(1000L, result.point());

        // Verify 메서드 호출 검증
        verify(userPointTable, times(1)).selectById(userId); // 메서드 호출 검증
    }

    @Test
    public void 포인트_조회_유저가_없을시200ok() {
        // Given
        long userId = 999L; // 존재하지 않는 ID

        // When
        UserPoint result = pointService.findUserById(userId);

        // Then
        assertEquals(0, result.point()); // 초기값을 반환해야 함
    }
}
