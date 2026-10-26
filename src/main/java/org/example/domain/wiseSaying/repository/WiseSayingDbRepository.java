package org.example.domain.wiseSaying.repository;

import org.example.domain.system.KeywordType;
import org.example.domain.wiseSaying.entity.WiseSaying;

import org.example.domain.wiseSaying.simpledb.ExecuteResult;
import org.example.domain.wiseSaying.simpledb.SimpleDB;
import org.example.domain.wiseSaying.simpledb.Sql;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class WiseSayingDbRepository {
    public long lastId = -1;
    SimpleDB dbManager;
    public final String basePath = System.getProperty("user.dir") + "/db/wiseSaying/";

    public WiseSayingDbRepository() {
        this.dbManager = new SimpleDB();
        createTable();
        lastId = getLastId();
    }

    /**
     * wisesaying table
     */
    private void createTable() {
        dbManager.run("""
                CREATE TABLE IF NOT EXISTS wisesaying(
                    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                    PRIMARY KEY(id),
                    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    modifiedDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    author VARCHAR(100) NOT NULL,
                    content TEXT NOT NULL
                )
                """);
    }

    public long insert(WiseSaying newForm) {
         return dbManager.genSql()
                .append("""
                INSERT INTO wisesaying (content, author)
                VALUES(?, ?)
                """, newForm.getContent(), newForm.getAuth())
                .insert();
    }

    public long getLastId() {
        if (lastId < 0) {
            Long autoIncrementId = dbManager.genSql()
                    .append("""
                    SELECT AUTO_INCREMENT
                    FROM information_schema.tables
                    WHERE table_schema = '%s'
                    AND table_name = 'wisesaying'
                    """.formatted(dbManager.getDbName()))
                    .selectLong();
            if (autoIncrementId == null) {
                setLastId(0);
            } else {
                return autoIncrementId;
            }
        }

        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    public boolean deleteById(long id) {
        int rows = dbManager.genSql()
                .append("""
                DELETE FROM wisesaying WHERE id = ?
                """, id)
                .delete();
        return rows > 0;
    }

    public boolean update(WiseSaying newForm) {
        int rows = dbManager.genSql()
                .append("""
                        UPDATE wisesaying
                        SET content = ?, author = ?
                        WHERE id = ?
                        """, newForm.getContent(), newForm.getAuth(), newForm.getId())
                .update();
        return rows > 0;
    }

    public void build() {

        int chunkSize = 1000;
        int offset = 0;

        Path baseDir = Path.of(basePath);
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path filePath = baseDir.resolve("data.json");

        while(true) {
            Sql sql = dbManager.genSql()
                    .append("SELECT * FROM wisesaying ORDER BY id LIMIT ? OFFSET ?", chunkSize, offset);
             List<WiseSaying> result = sql.selectRows(WiseSaying.class);
             try(BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                 for (WiseSaying w: result) {
                     writer.write(w.toJson());
                 }

                 if (result.size() < chunkSize) break;
                 offset += chunkSize;
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
        }
    }

    public void truncateTable() {
        dbManager.run("TRUNCATE wisesaying");
    }

    /**
     * select like~ 절에 count over 까지 넣은 쿼리라 offset으로 아무값도 안나오는 경우 문제가 발생할수있음
     * @param {KeywordType} type
     * @param {String} keyword
     * @param {int} limit
     * @param {int} offset
     * @return {ExecuteResult}
     * @throws IOException
     */
    public ExecuteResult search(KeywordType type, String keyword, int limit, int offset) throws IOException{
        Sql sql = dbManager.genSql().append("SELECT *, COUNT(*) OVER() as total_count FROM wisesaying");
        switch (type) {
            // SELECT * FROM wisesaying WHERE content LIKE CONCAT('%', '', '%') LIMIT 5 OFFSET 0;
            case CONTENT -> sql.append("WHERE content LIKE CONCAT('%', ?, '%')", keyword);
            case AUTHOR -> sql.append("WHERE author LIKE CONCAT('%', ?, '%')", keyword);
            case ERROR -> throw new IOException("잘못된 검색 타입이 입력되었다.");
        }
        sql.append("ORDER BY id DESC")
                .append("LIMIT ? OFFSET ?", limit, offset);

        return sql.selectRows();
    }

    public boolean containsId(long id) {
        Map<String, Object> result = dbManager.genSql()
                .append("SELECT id FROM wisesaying WHERE id = ?", id)
                .selectRow();
        return !result.isEmpty();
    }

    public int getTotalCount() {
        Map<String, Object> result = dbManager.genSql().append("SELECT COUNT(*) as total_count FROM wisesaying").selectRow();
        Number count = (Number) result.getOrDefault("total_count", 0);
        return count.intValue();
    }

    public WiseSaying getById(long id) {
         return dbManager.genSql().append("SELECT * FROM wisesaying WHERE id = ?", id).selectRow(WiseSaying.class);
    }
}