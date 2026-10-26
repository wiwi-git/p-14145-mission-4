package org.example.domain.wiseSaying.service;

import org.example.domain.system.KeywordType;
import org.example.domain.wiseSaying.entity.WiseSaying;
import org.example.domain.wiseSaying.repository.WiseSayingDbRepository;
import org.example.domain.wiseSaying.simpledb.ExecuteResult;

import java.io.IOException;
import java.util.List;

public class WiseSayingService {
    WiseSayingDbRepository wsRepository = new WiseSayingDbRepository();
//    public Map<Integer, WiseSaying> savedForms = getSavedForms();

    public long register(String content, String auth) {
        long id = wsRepository.getLastId() + 1;

        WiseSaying newForm = new WiseSaying(id, content, auth);
        long genKey = wsRepository.insert(newForm);
        if (genKey >= 0) wsRepository.setLastId(genKey);

        return id;
    }

    public void deleteById(long id) {
        wsRepository.deleteById(id);
    }

    public boolean containsId(long id) {
        return wsRepository.containsId(id);
    }

    public WiseSaying getById(long id) {
        return wsRepository.getById(id);
//        return savedForms.get(id);
    }

    public void update(WiseSaying newForm) {
        wsRepository.update(newForm);
    }

    public void build() {
        wsRepository.build();
    }

    public void allClear() {
        wsRepository.truncateTable();
    }

    public ExecuteResult search(KeywordType type, String keyword, int limit, int offset) {
        try {
            return wsRepository.search(type, keyword, limit, offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalCount() {
        return wsRepository.getTotalCount();
//        return savedForms.size();
    }
}
