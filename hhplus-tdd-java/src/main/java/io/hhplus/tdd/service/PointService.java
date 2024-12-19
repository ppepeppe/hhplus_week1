package io.hhplus.tdd.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {
    @Autowired
    private UserPointTable userPointTable;

    public UserPoint findUserById(long id) {
        UserPoint currentUser = userPointTable.selectById(id);

        if (currentUser == null) {

            return UserPoint.empty(id); // 유저가 없을 때 기본값 반환
        }

        return currentUser;
        }
    public UserPoint chargeUserPoint(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        if (amount > 1_000_000) {
            throw new IllegalArgumentException("충전 금액은 1,000,000원을 초과할 수 없습니다.");
        }

        UserPoint currentUser = userPointTable.selectById(id);
        long changePoint = amount;
        if (currentUser == null) {
            return userPointTable.insertOrUpdate(id, amount);
        }

        if (currentUser.point() != 0) {
            long currentUserPoint = currentUser.point();
            changePoint += currentUserPoint;
        }
        if (changePoint > 1_000_000) {
            throw new IllegalArgumentException("포인트 합계는 1,000,000원을 초과할 수 없습니다.");
        }

        return userPointTable.insertOrUpdate(id, changePoint);
        }

    public UserPoint updateUserPoint(long id, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        UserPoint currentUser = userPointTable.selectById(id);
        if (currentUser == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        long currentUserPoint = currentUser.point();
        if (currentUserPoint < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        long changePoint = currentUserPoint - amount;

        return userPointTable.insertOrUpdate(id, changePoint);
    }
}
