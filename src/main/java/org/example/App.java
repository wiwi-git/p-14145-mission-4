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

    public String getCurrentAppEnv() {
        return System.getProperty("app_env");
    }

    public void run(boolean addSample) {

        SystemController sysController = new SystemController();
        WiseSayingController wsController = new WiseSayingController(sc);

        if (addSample && (wsController.getTotalCount() == 0)) {
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
                    wsController.printList(cmd);
                    break;
                case DELETE:
                    wsController.delete(cmd);
                    break;
                case UPDATE:
                    wsController.update(cmd);
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
