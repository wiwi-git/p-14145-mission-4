package org.example.domain.wiseSaying.controller;

import org.example.Util;
import org.example.domain.system.KeywordType;
import org.example.domain.system.ParseCmd;
import org.example.domain.wiseSaying.entity.WiseSaying;
import org.example.domain.wiseSaying.service.WiseSayingService;

import java.util.List;
import java.util.Scanner;

public class WiseSayingController {
    WiseSayingService wsService = new WiseSayingService();
    Scanner sc;

    public WiseSayingController(Scanner sc) {
        this.sc = sc;
    }

    public void register() {
        String text = "";
        String auth = "";

        System.out.print("명언 : ");
        text = sc.nextLine();

        System.out.print("작가 : ");
        auth = sc.nextLine();

        int id = wsService.register(text, auth);

        System.out.println(id + "번 명언이 등록되었습니다.");
    }

    public void printList(ParseCmd cmd) {
        final int MAX_PRINT_LIMIT = 5;

        String keyword = cmd.getParam("keyword").trim();
        KeywordType type = KeywordType.create(cmd.getParam("keywordType").trim());
        String strPage = cmd.getParam("page").trim();

        if (type == KeywordType.ERROR) {
            return;
        }

        int page = 0;
        int offset = 0;
        if (!strPage.isEmpty()) {
            page = Util.safeParseInt(cmd.getParam("page")) - 1;
            if (page < 0 ) page = 0;
            offset = page * MAX_PRINT_LIMIT;
        }

        if (keyword.isEmpty()) {
            System.out.println("""
                        번호 / 작가 / 명언
                        ----------------------""");
        } else {
            System.out.printf("""
                        ----------------------
                        검색타입 : %s
                        검색어 : %s
                        ----------------------
                        번호 / 작가 / 명언
                        ----------------------
                        """, type.toString().toLowerCase(), keyword);
        }

        int count = 0;
        int passCount = offset;

        List<WiseSaying> result = wsService.search(type, keyword);

        for (WiseSaying w : result) {
            if (passCount >= 0) {
                passCount--;
                if (passCount >= 0) continue;
            }
            if (count >= MAX_PRINT_LIMIT) break;

            String printLine = w.toString();
            System.out.println(printLine);
            count++;
        }

        int currentPage = offset / MAX_PRINT_LIMIT + 1;

        StringBuilder pageLine = new StringBuilder("페이지 : ");
        int total = result.size();
        int maxPage = total / MAX_PRINT_LIMIT;
        if (total % MAX_PRINT_LIMIT > 0) maxPage ++;


        for (int i = 1; i < maxPage; i++) {
            String pageNumber = "";

            if (i == currentPage) {
                pageNumber = "[%d] / ".formatted(currentPage);
            }
            else pageNumber = i + " / ";

            pageLine.append(pageNumber);
        }
        if (currentPage == maxPage) pageLine.append("[").append(maxPage).append("]");
        else pageLine.append(maxPage);

        System.out.println("----------------------");
        System.out.println(pageLine);

    }

    public void delete(ParseCmd cmd) {
        int id = Util.safeParseInt(cmd.getParam("id").trim());

        if (!wsService.containsKey(id)) {
            System.out.println(id + "번 명언은 존재하지 않습니다.");
            return;
        }
        wsService.delete(id);
        System.out.println(id + "번 명언이 삭제되었습니다.");
    }

    public void update(ParseCmd cmd) {
        int id = Util.safeParseInt(cmd.getParam("id").trim());
        if (!wsService.containsKey(id)) {
            System.out.println(id + "번 명언은 존재하지 않습니다.");
            return;
        }

        WiseSaying form = wsService.getForm(id);

        System.out.println("명언(기존) : " + form.getText());
        System.out.print("명언 : ");
        form.setText(sc.nextLine());

        System.out.println("작가(기존) : " + form.getAuth());
        System.out.print("작가 : ");
        form.setAuth(sc.nextLine());

        wsService.update(id, form);
    }

    public void build() {
        wsService.build();
        System.out.println("data.json 파일의 내용이 갱신되었습니다.");
    }

    public void allClear() {
        wsService.allClear();
    }

    public void addSampleData() {
        for (int i = 1; i < 11; i++) {
            wsService.register("명언 " + i, "작자미상 " + i);
        }
    }

    public int getTotalCount() {
        return wsService.getTotalCount();
    }
}

