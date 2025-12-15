package cinema.dao;

import cinema.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import cinema.domain.Screen;

public class ScreenDAO {

    // 1. 모든 상영관 목록 가져오기
    public List<Screen> getAllScreens() {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM screens ORDER BY screen_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Screen(
                    rs.getInt("screen_id"),
                    rs.getString("screen_name"),
                    rs.getInt("total_row"),
                    rs.getInt("total_col")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. 상영관 이름으로 정보 찾기 (예매창 띄울 때 필요)
    public Screen getScreenByName(String name) {
        String sql = "SELECT * FROM screens WHERE screen_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Screen(
                    rs.getInt("screen_id"),
                    rs.getString("screen_name"),
                    rs.getInt("total_row"),
                    rs.getInt("total_col")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // 3. 상영관 추가
    public boolean addScreen(String name, int row, int col) {
        String sql = "INSERT INTO screens (screen_name, total_row, total_col) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, row);
            pstmt.setInt(3, col);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateScreen(int id, String name, int row, int col) {
        String sql = "UPDATE screens SET screen_name=?, total_row=?, total_col=? WHERE screen_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, row);
            pstmt.setInt(3, col);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // [기존 deleteScreen 수정] 이름 대신 ID로 삭제하는 것이 더 안전함 (오버로딩)
    public boolean deleteScreen(int id) {
        String sql = "DELETE FROM screens WHERE screen_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}