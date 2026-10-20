package org.example.domain.wiseSaying.controller;

import org.example.AppTestRunner;

import org.example.TestUtil;
import org.example.domain.wiseSaying.repository.WiseSayingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WiseSayingControllerTest {
    @BeforeEach
    void beforeEach() {
        new WiseSayingController(TestUtil.genScanner("")).allClear();
    }

    @Test
    @DisplayName("등록")
    void t1() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                종료
                """).replace("\r\n", "\n");

        assertThat(out)
                .contains("명언 :")
                .contains("작가 :")
                .contains("1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("목록")
    void t2() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                목록
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        1 / 작자미상 / 현재를 사랑하라.
                        """);
    }

    @Test
    @DisplayName("목록-페이징")
    void t2_1() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 20; i++) {
            String line = """
                    등록
                    명언 %d
                    작자 %d
                    """.formatted(i, i);
            sb.append(line);
        }

        final String out = AppTestRunner.run(sb.toString() +"""
                목록?page=1
                목록?page=2
                목록?page=3
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        20 / 작자 19 / 명언 19
                        19 / 작자 18 / 명언 18
                        18 / 작자 17 / 명언 17
                        17 / 작자 16 / 명언 16
                        16 / 작자 15 / 명언 15
                        ----------------------
                        페이지 : [1] / 2 / 3 / 4
                        """)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        15 / 작자 14 / 명언 14
                        14 / 작자 13 / 명언 13
                        13 / 작자 12 / 명언 12
                        12 / 작자 11 / 명언 11
                        11 / 작자 10 / 명언 10
                        ----------------------
                        페이지 : 1 / [2] / 3 / 4
                        """)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        10 / 작자 9 / 명언 9
                        9 / 작자 8 / 명언 8
                        8 / 작자 7 / 명언 7
                        7 / 작자 6 / 명언 6
                        6 / 작자 5 / 명언 5
                        ----------------------
                        페이지 : 1 / 2 / [3] / 4
                        """);
    }

    @Test
    @DisplayName("수정")
    void t3() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                수정?id=1
                현재와 자신을 사랑하라.
                홍길동
                목록
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("명언(기존) : 현재를 사랑하라.")
                .contains("작가(기존) : 작자미상")
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        1 / 홍길동 / 현재와 자신을 사랑하라.
                        """);
    }

    @Test
    @DisplayName("삭제")
    void t4() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                삭제?id=1
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("1번 명언이 삭제되었습니다.");
    }

    @Test
    @DisplayName("빌드")
    void t5() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                빌드
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("data.json 파일의 내용이 갱신되었습니다.");

        WiseSayingRepository wsRepo = new WiseSayingRepository();
        File file = new File(wsRepo.basePath + "data.json");
        assertTrue(file.exists());


        try(
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
        ) {
            String line = "";
            StringBuilder lines = new StringBuilder();
            while ((line = br.readLine()) != null) {
                lines.append(line);
            }
            assertThat(lines.toString())
                    .contains("\"id\": 1")
                    .contains("\"content\": \"현재를 사랑하라.\",")
                    .contains("\"author\": \"작자미상\"");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("검색")
    void t6() {
        final String out = AppTestRunner.run("""
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                등록
                나 자신에 대한 자신감을 잃으면 온 세상이 나의 적이 된다.
                랄프 왈도 에머슨
                목록?keywordType=content&keyword=자신감
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        3 / 랄프 왈도 에머슨 / 나 자신에 대한 자신감을 잃으면 온 세상이 나의 적이 된다.
                        """);
    }
    @Test
    @DisplayName("검색-페이징")
    void t6_1() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            String line = """
                    등록
                    명언 %d
                    작자 %d
                    """.formatted(i, i);
            sb.append(line);
        }
        for (int i = 0; i < 10; i++) {
            String line = """
                    등록
                    명언2 %d
                    작자2 %d
                    """.formatted(i, i);
            sb.append(line);
        }

        final String out = AppTestRunner.run(sb.toString() +"""
                목록?keywordType=content&keyword=명언&page=2
                목록?keywordType=author&keyword=작자2&page=1
                종료
                """).replace("\r\n", "\n");;

        assertThat(out)
                .contains("""
                        ----------------------
                        검색타입 : content
                        검색어 : 명언
                        ----------------------
                        번호 / 작가 / 명언
                        ----------------------
                        15 / 작자2 4 / 명언2 4
                        14 / 작자2 3 / 명언2 3
                        13 / 작자2 2 / 명언2 2
                        12 / 작자2 1 / 명언2 1
                        11 / 작자2 0 / 명언2 0
                        ----------------------
                        페이지 : 1 / [2] / 3 / 4
                        """)
                .contains("""
                        ----------------------
                        검색타입 : author
                        검색어 : 작자2
                        ----------------------
                        번호 / 작가 / 명언
                        ----------------------
                        20 / 작자2 9 / 명언2 9
                        19 / 작자2 8 / 명언2 8
                        18 / 작자2 7 / 명언2 7
                        17 / 작자2 6 / 명언2 6
                        16 / 작자2 5 / 명언2 5
                        ----------------------
                        페이지 : [1] / 2
                        """);
    }
}