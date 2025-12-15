package cinema.dao;

import cinema.util.DBUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cinema.domain.Movie;

public class MovieDAO {

    // ★★★ [수정] 날짜 및 검색 쿼리를 받는 메서드 (UserMainPanel용) ★★★
    public List<Movie> getMoviesForUser(LocalDate selectedDate, String searchQuery) {
        List<Movie> list = new ArrayList<>();
        
        // 1. 기본 쿼리: 상영 일정이 있는 영화만 표시
        String sql = "SELECT DISTINCT m.* FROM movies m " +
                     "JOIN schedules s ON m.movie_id = s.movie_id " +
                     "WHERE 1=1 ";
        
        // 2. 날짜 조건: 선택된 날짜와 같거나 이후의 상영 일정이 있는 영화만 표시
        if (selectedDate != null) {
            // 선택된 날짜가 오늘보다 이전이면, 상영 예정작 전체를 표시하는 것이 아니라,
            // 선택된 날짜의 상영 목록을 보여줘야 하므로, 여기서는 SQL에 직접 날짜를 비교합니다.
            // (날짜가 오늘 이전인 경우의 제어는 UserMainPanel에서 처리합니다.)
            sql += " AND s.show_date >= ? "; 
        }
        
        // 3. 검색 조건
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql += " AND m.title LIKE ? ";
        }
        
        sql += " ORDER BY m.movie_id DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            
            if (selectedDate != null) {
                pstmt.setString(paramIndex++, selectedDate.toString());
            }
            
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchQuery + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Movie(
                    rs.getInt("movie_id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("running_time"),
                    rs.getString("poster_path") 
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    // (기존 getAllMovies는 AdminMainPanel에서 호출되므로, 이름 변경 없이 기존 로직을 유지합니다.)
    public List<Movie> getAllMovies() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies ORDER BY movie_id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Movie(
                    rs.getInt("movie_id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    rs.getInt("running_time"),
                    rs.getString("poster_path") 
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean addMovie(String title, String genre, int time, String path) {
        String sql = "INSERT INTO movies (title, genre, running_time, poster_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, genre);
            pstmt.setInt(3, time);
            pstmt.setString(4, path); 
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updateMovie(int id, String title, String genre, int time, String path) {
        String sql = "UPDATE movies SET title=?, genre=?, running_time=?, poster_path=? WHERE movie_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, genre);
            pstmt.setInt(3, time);
            pstmt.setString(4, path);
            pstmt.setInt(5, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean deleteMovie(int movieId) {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }
}