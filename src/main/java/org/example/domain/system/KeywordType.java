package org.example.domain.system;

public enum KeywordType {
    CONTENT, AUTHOR, ERROR;

    /**
     * 빈값이 들어올 경우 CONTENT 반환
     * @param {string} string
     * @return {KeywordType}
     */
    public static KeywordType create(String string) {
        if (string.trim().isEmpty()) {
            return KeywordType.CONTENT;
        }

        KeywordType type = KeywordType.ERROR;
        try {
            type = KeywordType.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }
        return type;
    }
}
