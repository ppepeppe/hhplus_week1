package io.hhplus.tdd.history;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


public class PointServiceHistoryUnitTest {
    @InjectMocks
    private PointService pointService;
    @Mock
    private PointHistoryTable pointHistoryTable;

    public PointServiceHistoryUnitTest() {
         MockitoAnnotations.openMocks(this); // Mockito 초기화
     }

    @Test
    public void 특정_유저_사용내역조회_성공200ok() {
        // Given
        long userId = 1L;
        PointHistory chargeHistory = new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory useHistory = new PointHistory(2L, userId, 500L, TransactionType.USE, System.currentTimeMillis());

        // Mock 동작 정의
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of(chargeHistory, useHistory));

        // When
        List<PointHistory> result = pointService.findPointHistoryById(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals(chargeHistory, result.get(0));
        assertEquals(useHistory, result.get(1));

        // Verify 메서드 호출 검증
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    public void 특정_유저_사용내역_조회_내역이_없을시_200ok() {
        // Given
        long userId = 1L;

        // Mock 정의
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of());

        // When
        List<PointHistory> result = pointService.findPointHistoryById(userId);

        // Then
        assertEquals(0, result.size());

        // Verify 메서드 호출 검증
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }
}

