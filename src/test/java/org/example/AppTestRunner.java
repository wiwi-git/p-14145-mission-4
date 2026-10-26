package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class AppTestRunner {
    public static String run(String input) {
//        EnvLoader.load();

        ByteArrayOutputStream out = TestUtil.setOutToByteArray();

        Scanner sc = TestUtil.genScanner(input);
        App app = new App(sc);

        try {
            if (!app.getCurrentAppEnv().equals("dev")) {
                throw new IOException("dev가 아닌 곳에서 테스트를 진행할 수 없습니다.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        app.run(false);

        TestUtil.clearSetOutToByteArray();

        return out.toString();
    }
}
