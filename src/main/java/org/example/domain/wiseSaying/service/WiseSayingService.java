package org.example.domain.wiseSaying.service;

import org.example.domain.system.KeywordType;
import org.example.domain.wiseSaying.entity.WiseSaying;
import org.example.domain.wiseSaying.repository.WiseSayingRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WiseSayingService {
    WiseSayingRepository wsRepository = new WiseSayingRepository();
    public Map<Integer, WiseSaying> savedForms = getSavedForms();
    public int lastId = getLastId();

    public int register(String text, String auth) {
        int id = lastId + 1;

        WiseSaying newForm = new WiseSaying(id, text, auth);
        savedForms.put(id, newForm);
        try {
            wsRepository.write(id, newForm);
            int result = wsRepository.writeLastId(id);
            if (result >= 0) lastId = result;
        } catch (IOException e) {
            System.out.println(e);;
            throw new RuntimeException(e);
        }

        return id;
    }

    private int getLastId() {
        try {
            return wsRepository.getLastId();
        } catch (IOException e) {
            System.out.println(e);;
            throw new RuntimeException(e);
        }
    }

    private Map<Integer, WiseSaying> getSavedForms() {
        try {
            return wsRepository.getSavedForms();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int id) {
        wsRepository.delete(id + ".json");
        savedForms.remove(id);
    }

    public boolean containsKey(int id) {
        return savedForms.containsKey(id);
    }

    public WiseSaying getForm(int id) {
        return savedForms.get(id);
    }

    public void update(int id, WiseSaying newForm) {
        try {
            wsRepository.update(id, newForm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void build() {
        try {
            wsRepository.build(savedForms);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void allClear() {
        wsRepository.allClear();
    }

    public List<WiseSaying> search(KeywordType type, String keyword) {
        try {
            return wsRepository.search(type, keyword);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalCount() {
        return savedForms.size();
    }
}
