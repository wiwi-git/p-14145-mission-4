package org.example.domain.wiseSaying.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class WiseSaying {
    private long id;
    private String content = "";
    private String auth = "";

    public WiseSaying(){}
    public WiseSaying(long id) {
        this.id = id;
    }

    public WiseSaying(long id, String content, String auth) {
        this.id = id;
        this.content = content;
        this.auth = auth;
    }

    public long getId() {
        return id;
    }

    public String getAuth() {
        return auth;
    }

    public String getContent() {
        return content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String toString() {
        return  id + " / " + auth + " / " + content;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        return """
//            {
//                "id": %d,
//                "content": "%s",
//                "author": "%s"
//            }
//            """.formatted(id, content, auth);
    }

    public void setData(Map<String, Object> rowMap) {
        setAuth((String) rowMap.get("author"));
        setContent((String) rowMap.get("content"));
        setId((Long) rowMap.get("id"));
    }
}