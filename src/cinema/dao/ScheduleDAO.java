package cinema.dao;

import cinema.util.DBUtil;
import cinema.domain.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    public List<String> getDatesByMovie(int movieId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT show_date FROM schedules WHERE movie_id = ? ORDER BY show_date";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) list.add(rs.getString("show_date"));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Schedule> getSchedulesByMovieAndDate(int movieId, String date) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE movie_id = ? AND show_date = ? ORDER BY start_time";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId); pstmt.setString(2, date);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                list.add(new Schedule(rs.getInt("schedule_id"), rs.getInt("movie_id"), rs.getString("screen_name"), rs.getString("show_date"), rs.getString("start_time")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    // ★ 날짜 검색 기능
    public List<Schedule> searchSchedules(String dateQuery) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        
        if (dateQuery != null && !dateQuery.trim().isEmpty()) {
            sql += " WHERE show_date LIKE ?";
        }
        sql += " ORDER BY show_date DESC, start_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (dateQuery != null && !dateQuery.trim().isEmpty()) {
                pstmt.setString(1, "%" + dateQuery + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                list.add(new Schedule(rs.getInt("schedule_id"), rs.getInt("movie_id"), rs.getString("screen_name"), rs.getString("show_date"), rs.getString("start_time")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean addSchedule(int movieId, String screen, String date, String time) {
        String sql = "INSERT INTO schedules (movie_id, screen_name, show_date, start_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId); pstmt.setString(2, screen); pstmt.setString(3, date); pstmt.setString(4, time);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean updateSchedule(int id, int movieId, String screenName, String date, String startTime) {
        String sql = "UPDATE schedules SET movie_id=?, screen_name=?, show_date=?, start_time=? WHERE schedule_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId); pstmt.setString(2, screenName); pstmt.setString(3, date); pstmt.setString(4, startTime); pstmt.setInt(5, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM schedules WHERE schedule_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}