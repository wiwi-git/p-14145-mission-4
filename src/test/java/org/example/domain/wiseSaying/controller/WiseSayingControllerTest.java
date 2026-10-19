package org.example.domain.wiseSaying.controller;

import org.example.AppTestRunner;

import org.example.TestUtil;
import org.example.domain.wiseSaying.repository.WiseSayingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
                """);

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
                """);

        assertThat(out)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        1 / 작자미상 / 현재를 사랑하라.
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
                """);

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
                """);

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
                """);

        assertThat(out)
                .contains("data.json 파일의 내용이 갱신되었습니다.");

        WiseSayingRepository wsRepo = new WiseSayingRepository();
        File file = new File(wsRepo.basePath + "data.json");
        assertTrue(file.exists());

        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

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
                """);

        assertThat(out)
                .contains("""
                        번호 / 작가 / 명언
                        ----------------------
                        3 / 랄프 왈도 에머슨 / 나 자신에 대한 자신감을 잃으면 온 세상이 나의 적이 된다.
                        """);
    }
}