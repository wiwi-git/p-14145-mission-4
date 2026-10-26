package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EnvLoader {
    public static void load() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(".env"));

            for (String line : lines) {
                if (line.trim().isBlank() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                String key = parts[0].trim();
                String value = parts[1].trim();
                System.setProperty(key, value);
            }
        } catch (IOException e) {
            System.out.println(".env 로드 실패");
        }
    }
}
