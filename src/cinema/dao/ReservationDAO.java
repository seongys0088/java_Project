package cinema.dao;

import cinema.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.util.Vector;

public class ReservationDAO {
    
    public List<Point> getReservedSeats(int scheduleId) {
        List<Point> seats = new ArrayList<>();
        String sql = "SELECT seat_row, seat_col FROM reservations WHERE schedule_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                seats.add(new Point(rs.getInt("seat_row"), rs.getInt("seat_col")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return seats;
    }

    public boolean addReservation(String userId, int movieId, int scheduleId, int row, int col) {
        String sql = "INSERT INTO reservations (user_id, movie_id, schedule_id, seat_row, seat_col) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setInt(2, movieId);
            pstmt.setInt(3, scheduleId);
            pstmt.setInt(4, row);
            pstmt.setInt(5, col);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ★★★ [수정] 상영 날짜(s.show_date) 추가 조회 ★★★
    public Vector<Vector<String>> getMyReservations(String userId) {
        Vector<Vector<String>> data = new Vector<>();
        // s.show_date 컬럼을 추가로 가져옵니다.
        String sql = "SELECT r.reservation_id, m.title, s.show_date, s.start_time, r.seat_row, r.seat_col " +
                     "FROM reservations r " +
                     "JOIN movies m ON r.movie_id = m.movie_id " +
                     "JOIN schedules s ON r.schedule_id = s.schedule_id " +
                     "WHERE r.user_id = ? ORDER BY s.show_date DESC, s.start_time DESC";
                     
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("reservation_id"));
                row.add(rs.getString("title"));
                // 날짜와 시간을 합쳐서 보여주거나 따로 저장
                String fullTime = rs.getString("show_date") + " " + rs.getString("start_time");
                row.add(fullTime); 
                row.add(rs.getInt("seat_row") + "행 " + rs.getInt("seat_col") + "열");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
    
    public boolean cancelReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}