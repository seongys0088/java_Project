package cinema.dao;

import cinema.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import cinema.domain.Snack;

public class SnackDAO {
    
    // 목록 가져오기
    public List<Snack> getAllSnacks() {
        List<Snack> list = new ArrayList<>();
        String sql = "SELECT * FROM snacks ORDER BY category, name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {
                list.add(new Snack(
                    rs.getInt("snack_id"),
                    rs.getString("name"),
                    rs.getInt("price"),
                    rs.getString("category"),
                    rs.getString("image_path"),
                    rs.getString("is_soldout") // DB에서는 "Y" 또는 "N"으로 저장됨
                ));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    // [수정됨] 메뉴 추가 (boolean isSoldOut 받음)
    public boolean addSnack(String name, int price, String category, String path, boolean isSoldOut) {
        String sql = "INSERT INTO snacks (name, price, category, image_path, is_soldout) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, price);
            pstmt.setString(3, category);
            pstmt.setString(4, path);
            pstmt.setString(5, isSoldOut ? "Y" : "N"); // boolean -> "Y"/"N" 변환
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }
    
    // [수정됨] 메뉴 수정 (boolean isSoldOut 받음) - 오류가 났던 부분 해결
    public boolean updateSnack(int id, String name, int price, String category, String path, boolean isSoldOut) {
        String sql = "UPDATE snacks SET name=?, price=?, category=?, image_path=?, is_soldout=? WHERE snack_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, price);
            pstmt.setString(3, category);
            pstmt.setString(4, path);
            pstmt.setString(5, isSoldOut ? "Y" : "N"); // boolean -> "Y"/"N" 변환
            pstmt.setInt(6, id);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    // 메뉴 삭제
    public boolean deleteSnack(int id) {
        String sql = "DELETE FROM snacks WHERE snack_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) { return false; }
    }

    // 주문 내역 저장
    public int addOrder(String userId, String details, int totalPrice) {
        String sql = "INSERT INTO snack_orders (user_id, order_details, total_price) VALUES (?, ?, ?)";
        String[] generatedColumns = {"order_id"};
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, details);
            pstmt.setInt(3, totalPrice);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    // 내 주문 내역 조회
    public Vector<Vector<String>> getOrderHistory(String userId) {
        Vector<Vector<String>> data = new Vector<>();
        String sql = "SELECT order_id, order_details, total_price, order_date FROM snack_orders WHERE user_id = ? ORDER BY order_id DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(String.valueOf(rs.getInt("order_id"))); 
                row.add(rs.getString("order_date")); 
                row.add(rs.getString("order_details")); 
                row.add(rs.getInt("total_price") + "원"); 
                data.add(row);
            }
        } catch(Exception e) { e.printStackTrace(); }
        return data;
    }
}