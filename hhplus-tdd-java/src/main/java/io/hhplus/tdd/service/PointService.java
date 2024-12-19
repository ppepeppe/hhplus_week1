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
}
