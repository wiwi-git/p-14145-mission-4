package org.example.domain.wiseSaying.controller;

import org.example.domain.system.KeywordType;
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

    public void printList() {
        System.out.println("""
                번호 / 작가 / 명언
                ----------------------""");
        for (int id = wsService.lastId; id > 0; id--) {
            WiseSaying form = wsService.getForm(id);
            if (form == null) continue;

            String printLine = form.toString();
            System.out.println(printLine);
        }
    }

    public void delete(int id) {
        if (!wsService.containsKey(id)) {
            System.out.println(id + "번 명언은 존재하지 않습니다.");
            return;
        }
        wsService.delete(id);
        System.out.println(id + "번 명언이 삭제되었습니다.");
    }

    public void update(int id) {
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

    public void search(KeywordType type, String keyword) {
        List<WiseSaying> result = wsService.search(type, keyword);

        System.out.printf("""
                ----------------------
                검색타입 : %s
                검색어 : %s
                ----------------------
                번호 / 작가 / 명언
                ----------------------
                """, type.toString().toLowerCase(), keyword);

        for (WiseSaying w : result) {
            System.out.println(w.toString());
        }
    }
}

