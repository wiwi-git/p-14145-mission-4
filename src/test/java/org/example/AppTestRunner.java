package org.example;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;

public class AppTestRunner {
    public static String run(String input) {
        ByteArrayOutputStream out = TestUtil.setOutToByteArray();

        Scanner sc = TestUtil.genScanner(input);
        App app = new App(sc);
        app.run(false);

        TestUtil.clearSetOutToByteArray();

        return out.toString();
    }
}
