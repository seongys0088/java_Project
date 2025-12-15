package cinema.dao;

import cinema.util.DBUtil;
import cinema.domain.User;
import java.sql.*;
import java.util.Vector;

public class UserDAO {
    // ⚠️ 본인의 DB 설정으로 맞춰주세요!
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xepdb1";
    private static final String DB_USER = "system"; 
    private static final String DB_PW = "1111";

    // DB 연결 헬퍼 메서드
    private Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(URL, DB_USER, DB_PW);
    }

    // [기능 1] 로그인 (User 객체에 role을 포함하여 반환)
    public User login(String inputId, String inputPw) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, inputId);
            pstmt.setString(2, inputPw);
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("role") // role을 포함하여 반환
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // [기능 2] 회원가입
    public boolean addUser(String id, String pw, String name, int age) {
        String sql = "INSERT INTO users (user_id, password, name, age, role, created_at) VALUES (?, ?, ?, ?, 'USER', SYSDATE)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            pstmt.setString(3, name);
            pstmt.setInt(4, age);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("아이디 중복 발생");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // [기능 3] 비밀번호 확인 (관리자 모드 재인증 시 사용)
    public boolean verifyPassword(String inputId, String inputPw) {
        String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, inputId);
            pstmt.setString(2, inputPw);
            
            try(ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // [수정] 사용자 권한/상태 업데이트 (BANNED 역할 사용)
    public boolean updateUserRole(String userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        if (newRole.equals("ADMIN") || newRole.equals("USER") || newRole.equals("BANNED")) {
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newRole);
                pstmt.setString(2, userId);
                return pstmt.executeUpdate() > 0;
            } catch (Exception e) { 
                e.printStackTrace(); 
                return false;
            }
        }
        return false;
    }
    
    // ★★★ [수정] 모든 사용자 목록 가져오기 (관리자 제외) ★★★
    public Vector<Vector<String>> getAllUsers() {
        Vector<Vector<String>> data = new Vector<>();
        // ★ role이 'ADMIN'이 아닌 사용자만 조회하도록 조건 추가
        String sql = "SELECT user_id, name, age, role, created_at FROM users WHERE role != 'ADMIN' ORDER BY created_at DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                Vector<String> row = new Vector<>();
                String role = rs.getString("role");
                
                row.add(rs.getString("user_id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("age"));
                row.add(role); // 권한 (USER/BANNED)
                row.add(rs.getString("created_at").split(" ")[0]); 
                
                // BANNED 역할에 따라 '정지' 또는 '활성' 상태 표시
                String status = role.equals("BANNED") ? "정지" : "활성"; 
                row.add(status); 
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
}