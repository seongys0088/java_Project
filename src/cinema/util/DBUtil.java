package cinema.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {
    // 오라클 설정 (본인 환경에 맞게 수정)
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xepdb1";
    private static final String ID = "system";
    private static final String PW = "1111";

    // 연결 객체를 주는 공통 메서드
    public static Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(URL, ID, PW);
    }
}