package org.example.domain.wiseSaying.simpledb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class SimpleDB {
    private String dbName;
    private String username;
    private String password;
    private String url;
    private boolean devMode;


    private Connection transactionConn = null;

    public SimpleDB() {
        String host = System.getProperty("db_host");
        String username = System.getProperty("db_username");
        String password = System.getProperty("db_password");
        String appEnv = System.getProperty("app_env");
        String dbName = System.getProperty("db_name");

        if (appEnv.equals("dev")) dbName += "__test";
        this.dbName = dbName;
        this.username = username;
        this.password = password;
        this.url = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul", host, dbName);
    }
/*
    public SimpleDB(String host, String username, String password, String dbName) {
        this.url = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul", host, dbName);
        this.username = username;
        this.password = password;
    }
*/

    public String getDbName() {
        return dbName;
    }

    public boolean isTransactionConnected() {
        if (Objects.isNull(transactionConn)) return false;
        try {
            return !transactionConn.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getTransactionConn() {
        return transactionConn;
    }

    public void setTransactionConn(Connection transactionConn) {
        this.transactionConn = transactionConn;
    }


    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean isOn) {
        this.devMode = isOn;
    }

    public Connection getConnection() {
        if (Objects.nonNull(transactionConn)) return transactionConn;

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (RuntimeException | SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void run(String sql, Object... options) {
        genSql().append(sql, options).execute();
    }

    public Sql genSql() {
        return new Sql(this);
    }

    /**
     * transcation용으로 저장한 conn닫는 용도
     */
    public void close() {
        try {
            if (isTransactionConnected()) {
                transactionConn.close();
                transactionConn = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * connection close를 잊지마~
     */
    public void startTransaction() {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            transactionConn = conn;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit(){
        try {
            if (!isTransactionConnected()) {
                System.out.println("저장된 연결이 없습니다~");
                return;
            }

            transactionConn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    /**
     * 트랜잭션 롤백이 성공하던 말던 일단 연결닫도록 했음
     */
    public void rollback() {
        try {
            if (!isTransactionConnected()) {
                System.out.println("저장된 연결이 없습니다~");
                return;
            }

            transactionConn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }
}
