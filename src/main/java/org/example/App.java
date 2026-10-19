package org.example;

import org.example.domain.system.KeywordType;
import org.example.domain.system.ParseCmd;
import org.example.domain.system.SystemController;
import org.example.domain.wiseSaying.controller.WiseSayingController;

import java.util.Scanner;

public class App {
    Scanner sc;

    public App(Scanner sc) {
        this.sc = sc;
    }

    public void run(boolean addSample) {

        SystemController sysController = new SystemController();
        WiseSayingController wsController = new WiseSayingController(sc);

        final int MAX_PRINT_LIMIT = 5;

        if (addSample) {
            wsController.addSampleData();
        }

        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            String rawCommand = sc.nextLine().trim();
            ParseCmd cmd = new ParseCmd(rawCommand);
            switch (cmd.getCommand()) {
                case EXIT:
                    sysController.exit();
                    return;
                case REGISTER:
                    wsController.register();
                    break;
                case LIST:
                    String keyword = cmd.getParam("keyword").trim();
                    if (keyword.isEmpty()) {
                        String strPage = cmd.getParam("page").trim();
                        int page = 0;
                        int offset = 0;
                        if (!strPage.isEmpty()) {
                            page = Util.safeParseInt(cmd.getParam("page")) - 1;
                            if (page < 0 ) page = 0;
                            offset = page * MAX_PRINT_LIMIT;
                        }

                        System.out.println("""
                        번호 / 작가 / 명언
                        ----------------------""");
                        wsController.printList(MAX_PRINT_LIMIT, offset);
                    } else {
                        KeywordType type = KeywordType.create(cmd.getParam("keywordType").trim());
                        System.out.printf("""
                        ----------------------
                        검색타입 : %s
                        검색어 : %s
                        ----------------------
                        번호 / 작가 / 명언
                        ----------------------
                        """, type.toString().toLowerCase(), keyword);
                        wsController.search(type, keyword);
                    }
                    break;
                case DELETE:
                    wsController.delete(Util.safeParseInt(cmd.getParam("id").trim()));
                    break;
                case UPDATE:
                    wsController.update(Util.safeParseInt(cmd.getParam("id").trim()));
                    break;
                case BUILD:
                    wsController.build();
                    break;
                case ERROR:
                    break;
            }
        } //while
    }
}
