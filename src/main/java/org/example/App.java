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

    public void run() {

        SystemController sysController = new SystemController();
        WiseSayingController wsController = new WiseSayingController(sc);

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
                    if (keyword.isEmpty()) wsController.printList();
                    else {
                        KeywordType type = KeywordType.create(cmd.getParam("keywordType").trim());
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
