package com.dodam.dicegame.dicegame.util;

import com.dodam.dicegame.dicegame.dto.ScoreResults;

import java.util.List;

public class DataUtil {


    /**
     * ScoreResults를 targetNumber와 score 차이에 따라 정렬합니다.
     */
    public static void sortScoreResults(List<ScoreResults> scoreResultsList) {
        scoreResultsList.sort((result1, result2) -> {
            int diff1 = result1.getTargetNumber() - result1.getScore();
            int diff2 = result2.getTargetNumber() - result2.getScore();

            // 양수와 음수 우선 비교
            if (diff1 >= 0 && diff2 < 0) {
                return -1; // diff1이 양수이고 diff2가 음수면 result1이 더 앞
            } else if (diff1 < 0 && diff2 >= 0) {
                return 1; // diff1이 음수이고 diff2가 양수면 result2가 더 앞
            }

            // 음수끼리 비교 (절대값 기준으로 정렬)
            if (diff1 < 0 && diff2 < 0) {
                return Integer.compare(Math.abs(diff1), Math.abs(diff2));
            }

            // 양수끼리 비교 (절대값 기준으로 정렬)
            return Integer.compare(Math.abs(diff1), Math.abs(diff2));
        });
    }

}
