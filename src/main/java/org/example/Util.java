package org.example;

public class Util {
    /**
     * 잘못된 값이 들어올 경우 -1 반환, 양수값만을 사용할것.
     * @param {string} str
     * @return int
     */
    static int safeParseInt(String str) {
        int id = -1;
        if (str.isEmpty()) return id;

        try {
            id = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            System.out.println("잘못된 값이 입력되었습니다: " + str);
        }

        return id;
    }
}
