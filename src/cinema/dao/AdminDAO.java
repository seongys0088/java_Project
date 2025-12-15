package cinema.dao;

import cinema.util.DBUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;

public class AdminDAO {
    
    // 1. 총 예매 건수 가져오기
    public int getTotalReservations() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM reservations";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // 2. 총 매점 매출 가져오기
    public long getTotalSnackSales() {
        long total = 0;
        String sql = "SELECT SUM(total_price) FROM snack_orders";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) total = rs.getLong(1);
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }
    
    // [신규] 총 티켓 매출 (가정: 12,000원/장)
    public long getTotalTicketSales() {
        return (long) getTotalReservations() * 12000L;
    }
    
    // [추가] 평균 객단가/거래액
    public long getAverageTransactionValue() {
        long totalSales = getTotalTicketSales() + getTotalSnackSales();
        int totalReservations = getTotalReservations();
        if (totalReservations == 0) return 0;
        return totalSales / totalReservations;
    }
    
    // [추가] 매점/티켓 매출 비율 (예: 35:65)
    public String getSnackTicketRatio() {
        long snackSales = getTotalSnackSales();
        long ticketSales = getTotalTicketSales();
        long totalSales = snackSales + ticketSales;

        if (totalSales == 0) return "0:0";

        double snackRatio = (double) snackSales / totalSales;
        
        int snackPercent = (int) Math.round(snackRatio * 100);
        int ticketPercent = 100 - snackPercent;
        
        return snackPercent + ":" + ticketPercent;
    }

    // [매출 분석용] 특정 날짜의 총 매출액 가져오기 (티켓 + 스낵)
    public long getDailyTotalSales(LocalDate date) {
        long ticketSales = 0;
        long snackSales = 0;
        
        String dayStart = date.toString(); 
        String dayEnd = date.plusDays(1).toString();
        
        // 1. 티켓 매출 (reservations + schedules)
        String ticketSql = "SELECT COUNT(r.reservation_id) AS cnt " +
                           "FROM reservations r JOIN schedules s ON r.schedule_id = s.schedule_id " +
                           "WHERE s.show_date = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(ticketSql)) {
            pstmt.setString(1, dayStart); 
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ticketSales = (long) rs.getInt("cnt") * 12000L;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        // 2. 스낵 매출 (snack_orders)
        String snackSql = "SELECT SUM(total_price) AS sum_price " +
                          "FROM snack_orders " +
                          "WHERE order_date >= TO_DATE(?, 'YYYY-MM-DD') AND order_date < TO_DATE(?, 'YYYY-MM-DD')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(snackSql)) {
            pstmt.setString(1, dayStart);
            pstmt.setString(2, dayEnd);
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    snackSales = rs.getLong("sum_price");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        return ticketSales + snackSales;
    }

    // [매출 분석용] 주간 매출 (7일치) 데이터 가져오기
    public long[] getWeeklySales(LocalDate startOfWeek) {
        long[] weeklySales = new long[7];
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            weeklySales[i] = getDailyTotalSales(date);
        }
        return weeklySales;
    }
    
    // [MoM] 특정 월의 티켓 매출액 가져오기
    public long getTicketSalesByMonth(LocalDate date) {
        long sales = 0;
        int totalReservations = 0;
        
        String firstDayOfMonth = date.withDayOfMonth(1).toString();
        String firstDayOfNextMonth = date.plusMonths(1).withDayOfMonth(1).toString();
        
        String sql = "SELECT COUNT(r.reservation_id) AS cnt " +
                     "FROM reservations r JOIN schedules s ON r.schedule_id = s.schedule_id " +
                     "WHERE s.show_date >= ? AND s.show_date < ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, firstDayOfMonth);
            pstmt.setString(2, firstDayOfNextMonth);
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalReservations = rs.getInt("cnt");
                }
            }
            sales = (long) totalReservations * 12000L;
        } catch (Exception e) { e.printStackTrace(); }
        return sales;
    }
    
    // [MoM] 특정 월의 매점 매출액 가져오기
    public long getSnackSalesByMonth(LocalDate date) {
        long sales = 0;
        
        String firstDayOfMonth = date.withDayOfMonth(1).toString();
        String firstDayOfNextMonth = date.plusMonths(1).withDayOfMonth(1).toString();
        
        String snackSql = "SELECT SUM(total_price) AS sum_price " +
                          "FROM snack_orders " +
                          "WHERE order_date >= TO_DATE(?, 'YYYY-MM-DD') AND order_date < TO_DATE(?, 'YYYY-MM-DD')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(snackSql)) {
            
            pstmt.setString(1, firstDayOfMonth);
            pstmt.setString(2, firstDayOfNextMonth);
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sales = rs.getLong("sum_price");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sales;
    }
    
    // [MoM] 특정 월의 총 예약 건수 가져오기
    public int getTotalReservationsByMonth(LocalDate date) {
        int totalReservations = 0;
        
        String firstDayOfMonth = date.withDayOfMonth(1).toString();
        String firstDayOfNextMonth = date.plusMonths(1).withDayOfMonth(1).toString();
        
        String sql = "SELECT COUNT(r.reservation_id) AS cnt " +
                     "FROM reservations r JOIN schedules s ON r.schedule_id = s.schedule_id " +
                     "WHERE s.show_date >= ? AND s.show_date < ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, firstDayOfMonth);
            pstmt.setString(2, firstDayOfNextMonth);
            
            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalReservations = rs.getInt("cnt");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return totalReservations;
    }

    // [매출 분석용] 영화별 매출 기여도
    public Vector<String[]> getMovieSalesContributionDetailed() {
        Vector<String[]> data = new Vector<>();
        final long ticketPrice = 12000L; 
        final int totalReservations = getTotalReservations();
        final long totalTicketSales = getTotalTicketSales();

        String sql = "SELECT m.title, m.poster_path, COUNT(r.reservation_id) as reservation_count " +
                     "FROM movies m " +
                     "LEFT JOIN reservations r ON m.movie_id = r.movie_id " +
                     "GROUP BY m.title, m.poster_path " +
                     "ORDER BY reservation_count DESC";
        
        if (totalReservations == 0) return data;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
                String path = rs.getString("poster_path");
                int count = rs.getInt("reservation_count");
                long salesAmount = (long) count * ticketPrice;
                
                double contribution = (totalTicketSales > 0) ? (double) salesAmount / totalTicketSales : 0.0;
                
                String[] row = new String[4];
                row[0] = title;
                row[1] = String.valueOf(salesAmount);
                row[2] = String.format("%.1f%%", contribution * 100);
                row[3] = path;
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // [AdminHomePanel] 예매 수 가져오기 (Title, Count)
    public Vector<String[]> getMovieReservationCounts() {
        Vector<String[]> data = new Vector<>();
        String sql = "SELECT m.title, COUNT(r.reservation_id) as cnt " +
                     "FROM movies m " +
                     "LEFT JOIN reservations r ON m.movie_id = r.movie_id " +
                     "GROUP BY m.title " +
                     "ORDER BY cnt DESC " +
                     "FETCH FIRST 5 ROWS ONLY"; 

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String[] row = new String[2];
                row[0] = rs.getString("title");
                row[1] = rs.getString("cnt");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // [AdminHomePanel] 1위 영화 상세 정보 가져오기
    public String[] getTopRankMovieDetail() {
        String[] detail = null;
        
        String sql = "SELECT m.title, m.genre, m.running_time, m.poster_path " +
                     "FROM movies m " +
                     "JOIN (SELECT movie_id, COUNT(*) as cnt FROM reservations GROUP BY movie_id ORDER BY cnt DESC) r " +
                     "ON m.movie_id = r.movie_id " +
                     "WHERE ROWNUM = 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                detail = new String[4];
                detail[0] = rs.getString("title");
                detail[1] = rs.getString("genre");
                detail[2] = rs.getString("running_time");
                detail[3] = rs.getString("poster_path");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return detail;
    }
    
    // [RankingPanel] 상세 영화 순위 (오버로드 1: 검색 기능 포함)
    public Vector<String[]> getMovieRankingsDetailed(String searchQuery) {
        Vector<String[]> data = new Vector<>();
        
        String sql = "SELECT m.title, m.genre, m.running_time, m.poster_path, COUNT(r.reservation_id) AS reserve_count " +
                     "FROM movies m " +
                     "LEFT JOIN reservations r ON m.movie_id = r.movie_id " +
                     "GROUP BY m.title, m.genre, m.running_time, m.poster_path " +
                     "HAVING 1=1 "; 

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql += " AND m.title LIKE ? ";
        }
        
        sql += " ORDER BY reserve_count DESC, m.title ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + searchQuery + "%");
            }

            ResultSet rs = pstmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String[] row = new String[6];
                row[0] = String.valueOf(rank++); // 순위
                row[1] = rs.getString("title");
                row[2] = String.valueOf(rs.getInt("reserve_count"));
                row[3] = rs.getString("genre");
                row[4] = rs.getString("running_time");
                row[5] = rs.getString("poster_path");
                data.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
    
    // [RankingPanel] 상세 영화 순위 (오버로드 2: 검색 없는 호출)
    public Vector<String[]> getMovieRankingsDetailed() {
        return getMovieRankingsDetailed(null);
    }
    
    // (기존 메서드 호환성 유지용)
    public Vector<Vector<String>> getMovieRankings() {
        return new Vector<>(); 
    }
}