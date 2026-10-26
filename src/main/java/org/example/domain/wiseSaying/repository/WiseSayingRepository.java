package org.example.domain.wiseSaying.repository;

import org.example.Util;
import org.example.domain.system.KeywordType;
import org.example.domain.wiseSaying.entity.WiseSaying;

import java.io.*;
import java.util.*;

/**
 * 레거시 - 사용안하는중 WiseSayingDbRepository 로 변경
 */
public class WiseSayingRepository {
    public final String basePath = System.getProperty("user.dir") + "/db/wiseSaying/";
    public int lastId = -1;

    public WiseSayingRepository() {
        try {
            lastId = readLastIdFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFileForm(String filename, WiseSaying form) throws IOException {
        File folder = new File(basePath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(basePath + filename);
        if (!file.exists()) {
            file.createNewFile();
        }

        try(
            FileWriter fw = new FileWriter(basePath + filename);
            PrintWriter pw = new PrintWriter(fw);
        ){
            pw.println(form.toJson());
        }
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
        try(
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
        ){
            pw.println(id);
        }

        return id;
    }

    public void write(int id, WiseSaying newForm) throws IOException {
        writeFileForm(id + ".json", newForm);
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    private int readLastIdFile() throws IOException{
        File file = new File(basePath + "lastId.txt");
        if (!file.exists()) {
            return 0;
        }

        try(
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
        ) {

            String line = br.readLine().trim();
            int lastId = Util.safeParseInt(line);
            if (lastId < 0) throw new NumberFormatException();

            return lastId;
        }
    }

    public Map<Long, WiseSaying> getSavedForms() throws IOException{
        File folder = new File(basePath);
        File[] files = folder.listFiles();
        Map<Long, WiseSaying> map = new HashMap<>();

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

        // TODO: 언제 한번 text to json 하는거 제대로 만들어봐도 좋을듯?
        String line = "";
        WiseSaying form = new WiseSaying(-1);

        try(
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
        ) {
            while ((line = br.readLine()) != null) {
                String str = line.trim();
                if (str.startsWith("\"")) {
                    if (str.endsWith(",")) str = str.substring(0, str.length() - 1);

                    String[] keyAndValue = str.split(":", 2);
                    if (keyAndValue.length != 2) {
                        System.out.printf("json파싱중에 예상치 못한 상황이 발생했다! line: %s%n", str);
                        continue;
                    }
                    String key = keyAndValue[0].replace("\"",  "").trim();
                    String value = keyAndValue[1].replace("\"",  "").trim();

                    switch (key) {
                        case "id" :
                            int id = Util.safeParseInt(value);
                            if (id < 0) {
                                System.out.println("잘못된 데이터가 발견되었습니다. file:" + file.getName());
                                continue;
                            }
                            form.setId(id);
                            break;
                        case "content":
                            form.setContent(value);
                            break;
                        case "author":
                            form.setAuth(value);
                            break;
                        default: break;
                    }
                }
            }//while
        }// try

        return form;
    }

    public boolean deleteById(int id) {
        String filename = id + ".json";

        File file = new File(basePath + filename);
        if (!file.exists()) return false;
        return file.delete();
    }

    public void update(int id, WiseSaying newForm) throws IOException {
        String filename = id + ".json";
        writeFileForm(filename, newForm);
    }

    public void build(Map<Integer, WiseSaying> map) throws IOException {
        File file = new File(basePath + "data.json");
        if (!file.exists()) {
            boolean _ = file.createNewFile();
        }

        WiseSaying[] formArr = map.values().toArray(new WiseSaying[0]);
        Arrays.sort(formArr, new Comparator<WiseSaying>() {
            @Override
            public int compare(WiseSaying o1, WiseSaying o2) {
                return Long.compare(o1.getId(), o2.getId());
            }
        });

        try(
            FileWriter fw = new FileWriter(file);
            PrintWriter pw = new PrintWriter(fw);
        ) {

            pw.println("[");

            int index = 0;
            for (WiseSaying form : formArr) {
                index++;

                pw.println("\t{");
                pw.println("\t\t\"id\": " + form.getId() + ",");
                pw.println("\t\t\"content\": \"" + form.getContent() + "\",");
                pw.println("\t\t\"author\": \"" + form.getAuth() + "\"");

                if (index < formArr.length) {
                    pw.println("\t},");
                } else {
                    pw.println("\t}");
                }
            }

            pw.println("]");
        }// try
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
                    if (form.getContent().contains(keyword)) {
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
