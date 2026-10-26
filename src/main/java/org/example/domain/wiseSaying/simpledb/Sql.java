package org.example.domain.wiseSaying.simpledb;

import org.example.domain.wiseSaying.entity.SearchResult;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Sql {
    //    private final Connection conn;
    private final SimpleDB simpleDb;
    private List<String> sqlList = new ArrayList<>();
    private List<Object> sqlOptions = new ArrayList<>();


    public Sql(SimpleDB simpleDb) {
        this.simpleDb = simpleDb;
    }

    /**
     * devmode
     */
    public void printPSTMT(PreparedStatement pstmt) {
        if (!simpleDb.isDevMode()) return;

        System.out.println(pstmt.toString());
    }

    public Sql append(String part, Object... options) {
        String raw = part.strip();

        int formatIndex = raw.indexOf("?");
        if (formatIndex >= 0) {
            Collections.addAll(sqlOptions, options);
        }
        sqlList.add(raw);
        return this;
    }

    private String getAll() {
        StringBuilder sb = new StringBuilder();
        for (String s : sqlList) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    /**
     * append한것들을 clear()
     */
    public void flush() {
        sqlList.clear();
        sqlOptions.clear();
    }


    /**
     * 그냥 statement의 execute()를 실행
     */
    public void execute() {
        String sql = getAll();

        try(Connection conn = simpleDb.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            bindParameters(pstmt);
            printPSTMT(pstmt);
            pstmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            flush();
        }

    }

    /**
     *
     * @param pstmt
     * @return {ExecuteResult} 쿼리결과값 매핑한 맵selectRows와 타입변환해주는 함수를 담음
     * @throws SQLException
     */
    private ExecuteResult executeQuery(PreparedStatement pstmt) throws SQLException {
        bindParameters(pstmt);
        printPSTMT(pstmt);

        ResultSet rs = pstmt.executeQuery();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columnCount = rsMetaData.getColumnCount();

        List<Map<String, Object>> selectRows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String key = rsMetaData.getColumnName(i);
                Object value = rs.getObject(i);
                rowMap.put(key, value);
            }
            selectRows.add(rowMap);
        }

        return new ExecuteResult(selectRows);
    }

    /**
     *
     * @param {boolean} isGenKey
     * @return {long} isGenkey ? generatedKey : affectedRowsCount
     */
    private long executeUpdate(boolean isGenKey) {
        int genKey = isGenKey ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
        String sql = getAll();
        try {
            // conn의 트라이위트리소스 부분을 withConnection으로 넘김 transectionConn부분은 포함안시켜서 해제 안됨 주의
            return withConnection(conn -> {
                try (PreparedStatement pstmt = conn.prepareStatement(sql, genKey)) {
                    bindParameters(pstmt);
                    printPSTMT(pstmt);

                    int rows = pstmt.executeUpdate();
                    return isGenKey ? fetchGeneratedKey(pstmt) : rows;
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException("SQL 실행 실패: " + sql, e);
        } finally {
            flush();
        }
    }

    private long fetchGeneratedKey(PreparedStatement pstmt) throws SQLException {
        try (ResultSet keys = pstmt.getGeneratedKeys()) {
            return keys.next() ? keys.getLong(1) : -1L;
        }
    }

    private void bindParameters(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < sqlOptions.size(); i++) {
            pstmt.setObject(i + 1, sqlOptions.get(i));
        }
    }

    @FunctionalInterface
    private interface SqlFunction<T> {
        long apply(T t) throws SQLException;
    }

    private long withConnection(SqlFunction<Connection> action) throws SQLException {
        if (simpleDb.isTransactionConnected()) {
            return action.apply(simpleDb.getTransactionConn());
        }

        try (Connection conn = simpleDb.getConnection()) {
            return action.apply(conn);
        }
    }


    /**
     *
     * @return {long} generatedKey 반환
     */
    public long insert() {
        return executeUpdate(true);
    }

    public int update() {
        return (int) executeUpdate(false);
    }

    public int delete() {
        return (int) executeUpdate(false);
    }

    /**
     * 문제 발생시 null 반환
     *
     * @return {ExecuteResult}
     */
    public ExecuteResult selectRows() {
        String sql = getAll();
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return executeQuery(pstmt);

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            flush();
        }

        return null;
    }

    public <T> List<T> selectRows(Class<T> whoAreYou) {
        String sql = getAll();
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ExecuteResult result = executeQuery(pstmt);
            return result.listToGeneral(whoAreYou);

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            flush();
        }

        return new ArrayList<>();
    }

    /**
     * 문제 발생시 빈맵 반환
     * TODO: selectRows랑 어떻게 합쳐보자
     *
     * @return {map<string, object>}
     */
    public Map<String, Object> selectRow() {
        String sql = getAll();
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return executeQuery(pstmt).getSelectRows().getFirst();

        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            flush();
        }

        return new HashMap<>();
    }

    /**
     * TODO: Map 반환하는 selectRow랑 어떻게 합치지?
     * @param
     * @return T nullable
     */
    public <T> T selectRow(Class<T> who) {
        String sql = getAll();
        try (Connection conn = simpleDb.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return executeQuery(pstmt).listToGeneral(who).getFirst();

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            flush();
        }

        return null;
    }

    /**
     * DateTimeParseException이 던져질수있음.
     *
     * @return {localDateTime}
     */
    public LocalDateTime selectDatetime() {
        String upper = sqlList.getFirst().toUpperCase();
        sqlList.set(0, upper);

        Map<String, Object> result = selectRow();
        String nowTimestamp = getFirstResult(result).toString();
        return LocalDateTime.parse(nowTimestamp);
    }

    /**
     * 문제 발생시 null 반환
     * @return {long} nullable
     */
    public Long selectLong() {
        Map<String, Object> result = selectRow();
        try {
            Object temp = getFirstResult(result);
            if (temp == null) return null;
            return Long.parseLong(getFirstResult(result).toString());
        } catch (NumberFormatException e) {
            return null;
//            throw new NumberFormatException();
//            return -1L;
        }
    }

    public String selectString() {
        Map<String, Object> result = selectRow();
        Object firstResult = getFirstResult(result);

        if (firstResult != null) return firstResult.toString();
        return "";
    }

    /**
     *
     * @return {boolean} nullable
     */
    public Boolean selectBoolean() {
        Map<String, Object> result = selectRow();
        Object firstResult = getFirstResult(result);
        if (firstResult instanceof Integer) {
            return ((int) firstResult) == 1;
        } else if (firstResult instanceof Long) {
            return ((long) firstResult) == 1L;
        } else if (firstResult instanceof Boolean) {
            return (boolean) firstResult;
        }

        return null;
    }

    /**
     * map에 키가 하나있다고 가정, 그 키의 값을 반환
     *
     * @param {map} result
     * @return {object} nullable
     */
    private Object getFirstResult(Map<String, Object> result) {
        Object firstResult = null;
        for (String key : result.keySet()) {
            firstResult = result.get(key);
            break;
        }
        return firstResult;
    }

    public Sql appendIn(String part, Object... options) {
        String raw = part.strip();

        int formatIndex = raw.indexOf("?");
        if (formatIndex >= 0) {
            // .appendIn("WHERE id IN (?)", 1, 2, 3);
            // WHERE id IN ('1', '2', '3')
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < options.length; i++) {
                if (i != options.length - 1) {
                    sb.append("?,");
                } else {
                    sb.append("?");
                }
            }
            raw = raw.replace("?", sb.toString());
        }

        append(raw, options);

        return this;
    }

    /**
     * 중간값이 문제 생기면 그냥 null로 처리
     *
     * @return {list<long>}
     */
    public List<Long> selectLongs() {
        ExecuteResult result = selectRows();
        List<Object> values = new ArrayList<>();
        for ( Map<String, Object> m : result.getSelectRows()) {
            Object value = getFirstResult(m);
            if (value == null) continue;
            values.add(value);
        }

        return values.stream().map((s) -> {
            if (s instanceof Long) return (long) s;
            return null;
        }).toList();
    }

/*
    append(sqlPart, params...) — ? 바인딩 포함
    appendIn(sqlPart, params...) — IN (?) 의 ? 를 값 개수만큼 펼침
    insert() → 생성된 주키, update()/delete() → 영향받은 행 수
    selectRows() → List<Map<String, Object>>, selectRow() → Map<String, Object>
    selectRows(Article.class) / selectRow(Article.class) — 결과를 클래스에 매핑
    selectDatetime(), selectLong(), selectLongs(), selectString(), selectBoolean()
    */

}
