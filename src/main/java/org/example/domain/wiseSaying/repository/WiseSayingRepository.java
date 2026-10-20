package org.example.domain.wiseSaying.repository;

import org.example.domain.system.KeywordType;
import org.example.domain.wiseSaying.entity.WiseSaying;

import java.io.*;
import java.util.*;

public class WiseSayingRepository {
    public final String basePath = System.getProperty("user.dir") + "/db/wiseSaying/";

    private void writeFileForm(String filename, WiseSaying form) throws IOException {
        File folder = new File(basePath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(basePath + filename);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(basePath + filename);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(form.toJson());

        fw.close();
        pw.close();
    }

    public int writeLastId(int id) throws IOException {
        if (id < 1) {
            System.out.println("잘못된 아이디가 입력되었습니다.");
            return -1;
        }

        File folder = new File(basePath);
        File file = new File(basePath + "lastId.txt");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(id);

        fw.close();
        pw.close();

        return id;
    }

    public void write(int id, WiseSaying newForm) throws IOException {
        writeFileForm(id + ".json", newForm);
        writeLastId(id);
    }

    public int getLastId() throws IOException{
        File file = new File(basePath + "lastId.txt");
        if (!file.exists()) {
            return 0;
        }

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine().trim();

        // TODO: id int 검사
        int lastId = Integer.parseInt(line);

        fr.close();
        br.close();

        return lastId;
    }

    public Map<Integer, WiseSaying> getSavedForms() throws IOException{
        File folder = new File(basePath);
        File[] files = folder.listFiles();
        Map<Integer, WiseSaying> map = new HashMap<>();

        if (files != null) {
            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".json")) {
                    continue;
                }
                if (file.getName().startsWith("data")) {
                    continue;
                }

                WiseSaying form = readFileForm(file);
                map.put(form.getId(), form);
            }// for
        }// files not null

        return map;
    }

    private WiseSaying readFileForm(File file) throws IOException{
//        StringBuilder result = new StringBuilder();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        /*
        String line = "";
        while ((line = br.readLine()) != null) {
            result.append(line);
        }
        String rawForm = result.toString() ;
        String[] splited = rawForm.split("/");

        if (splited.length != 3) {
            System.out.println("파일 형식이 정상적이지 않습니다.");
            return new WiseSaying(-1);
        }
        // TODO: id가 int인지 검사?

        int id = Integer.parseInt(splited[0].trim());
        WiseSaying form = new WiseSaying(id, splited[1].trim(), splited[2].trim());
         */

        // TODO: 언제 한번 text to json 하는거 제대로 만들어봐도 좋을듯?
        String line = "";
        WiseSaying form = new WiseSaying(-1);

        while ((line = br.readLine()) != null) {
            String str = line.trim();
            if (str.startsWith("\"")) {
                if (str.endsWith(",")) str = str.substring(0, str.length() - 1);

                String[] keyAndValue = str.split(":");
                if (keyAndValue.length != 2) {
                    System.out.printf("json파싱중에 예상치 못한 상황이 발생했다! line: %s%n", str);
                    continue;
                }
                String key = keyAndValue[0].replace("\"",  "").trim();
                String value = keyAndValue[1].replace("\"",  "").trim();

                switch (key) {
                    case "id" :
                        // TODO: 항상 parseInt 가 걱정되네 safe버전을 만들어놔야하는데
                        form.setId(Integer.parseInt(value));
                        break;
                    case "content":
                        form.setText(value);
                        break;
                    case "author":
                        form.setAuth(value);
                        break;
                    default: break;
                }
            }
        }


        fr.close();
        br.close();

        return form;
    }

    public void delete(String filename) {
        File file = new File(basePath + filename);
        if (!file.exists()) return;
        file.delete();
    }

    public void update(int id, WiseSaying newForm) throws IOException {
        String filename = id + ".json";
        writeFileForm(filename, newForm);
    }

    public void build(Map<Integer, WiseSaying> map) throws IOException {
        File file = new File(basePath + "data.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);

        WiseSaying[] formArr = map.values().toArray(new WiseSaying[0]);
        Arrays.sort(formArr, new Comparator<WiseSaying>() {
            @Override
            public int compare(WiseSaying o1, WiseSaying o2) {
                return Integer.compare(o1.getId(), o2.getId());
            }
        });

        pw.println("[");

        int index = 0;
        for (WiseSaying form : formArr) {
            index++;

            pw.println("\t{");
            pw.println("\t\t\"id\": " + form.getId() + ",");
            pw.println("\t\t\"content\": \"" + form.getText() + "\",");
            pw.println("\t\t\"author\": \"" + form.getAuth() + "\"");

            if (index < formArr.length) {
                pw.println("\t},");
            } else {
                pw.println("\t}");
            }
        }

        pw.println("]");

        pw.close();
        fw.close();
    }

    public void allClear() {
        File folder = new File(basePath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }// for
        }// files not null
    }

    public List<WiseSaying> search(KeywordType type, String keyword) throws IOException{
        File folder = new File(basePath);
        File[] files = folder.listFiles();

        List<WiseSaying> result = new ArrayList<>();

        if (files == null) return result;

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) {
                continue;
            }
            if (file.getName().startsWith("data")) {
                continue;
            }

            WiseSaying form = readFileForm(file);
            switch (type) {
                case KeywordType.AUTHOR:
                    if (form.getAuth().contains(keyword)) {
                        result.add(form);
                    }

                    break;
                case KeywordType.CONTENT:
                    if (form.getText().contains(keyword)) {
                        result.add(form);
                    }
            }
        }// for

        return result.stream()
                .sorted(Comparator
                        .comparing(WiseSaying::getId)
                        .reversed()
                ).toList();
    }
}
