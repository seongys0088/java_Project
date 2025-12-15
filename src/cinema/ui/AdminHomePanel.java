package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.dao.AdminDAO;
import cinema.util.UIUtils;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Vector;
// *** UIUtils.java의 FONT_TITLE, FONT_MAIN 등을 사용함 ***

public class AdminHomePanel extends JPanel {
    private AdminDAO adminDAO;
    private JLabel totalVisitorsLabel; // 총 방문자 수 (예매 건수로 대체)
    private JLabel totalSalesLabel;    // 오늘 총 매출 (티켓+스낵)
    private JLabel reservationRateLabel; // 예매율
    private JLabel runningMoviesLabel; // 상영 영화 수 (임시값)

    // 통계 레이블 (대시보드 상단 4개)
    private JLabel todaySalesValue;
    private JLabel visitorCountValue;
    private JLabel reservationRateValue;
    private JLabel movieCountValue;
    
    private JPanel rankChartPanel; // 실시간 예매 현황 (좌측 그래프)
    private JPanel topMoviePanel; // 예매율 1위 영화 (우측 카드)

    public AdminHomePanel() {
        adminDAO = new AdminDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40)); 

        // 1. 상단 타이틀
        JLabel title = new JLabel("대시보드");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(UIUtils.COLOR_TEXT);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.BG_MAIN);
        headerPanel.add(title, BorderLayout.WEST);
        
        // 날짜/시간 표시 (우측 상단)
        JLabel dateTimeLabel = new JLabel(java.time.LocalDate.now().toString().replace("-", ".") + " 화요일");
        dateTimeLabel.setForeground(Color.GRAY);
        headerPanel.add(dateTimeLabel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. 메인 콘텐츠 (스크롤 X, 고정)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UIUtils.BG_MAIN);
        
        // [2-1] 통계 카드 영역 (4개)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(UIUtils.BG_MAIN);
        statsPanel.setPreferredSize(new Dimension(0, 150));
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        todaySalesValue = new JLabel("0원");
        visitorCountValue = new JLabel("0명");
        reservationRateValue = new JLabel("0.0%");
        movieCountValue = new JLabel("0편");

        statsPanel.add(createDashboardStatCard("오늘 총 매출", todaySalesValue, "Money"));
        statsPanel.add(createDashboardStatCard("총 방문자 수", visitorCountValue, "Person"));
        statsPanel.add(createDashboardStatCard("예매율", reservationRateValue, "Ticket"));
        statsPanel.add(createDashboardStatCard("상영 영화 수", movieCountValue, "Movie"));
        
        mainContent.add(statsPanel);
        mainContent.add(Box.createVerticalStrut(20));

        // [2-2] 실시간 예매 현황 + 1위 영화
        JPanel rankPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        rankPanel.setBackground(UIUtils.BG_MAIN);

        // 좌측: 실시간 예매 현황 (그래프 영역)
        rankChartPanel = new JPanel(new BorderLayout());
        rankChartPanel.setBackground(Color.WHITE);
        rankChartPanel.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15));
        rankChartPanel.add(new JLabel("실시간 예매 현황 (TOP 4)", SwingConstants.CENTER), BorderLayout.NORTH); // 임시 타이틀
        rankPanel.add(rankChartPanel);

        // 우측: 예매율 1위 영화
        topMoviePanel = new JPanel(new BorderLayout());
        topMoviePanel.setBackground(Color.WHITE);
        topMoviePanel.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15));
        rankPanel.add(topMoviePanel);

        mainContent.add(rankPanel);
        mainContent.add(Box.createVerticalGlue()); // 아래로 밀기
        
        // 스크롤이 필요 없는 구성이므로, mainContent를 그대로 추가
        add(mainContent, BorderLayout.CENTER);

        refreshData();
    }
    
    // 통계 카드 생성 메서드 (대시보드 상단용)
    private JPanel createDashboardStatCard(String title, JLabel valueLabel, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLbl = new JLabel(icon + " " + title);
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setFont(UIUtils.FONT_MAIN);

        valueLabel.setForeground(UIUtils.COLOR_TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        // 임시 변화율 표시 (디자인 맞춤용)
        JLabel changeRate = new JLabel("↑ +0.0%");
        changeRate.setForeground(new Color(40, 167, 69));
        changeRate.setFont(new Font("SansSerif", Font.BOLD, 12));
        card.add(changeRate, BorderLayout.SOUTH);
        
        return card;
    }
    
    // 실시간 예매 현황 (좌측 그래프 영역) 업데이트
    private void updateRankChart(int totalReservations) {
        rankChartPanel.removeAll();
        rankChartPanel.setLayout(new BoxLayout(rankChartPanel, BoxLayout.Y_AXIS));
        rankChartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("실시간 예매 현황");
        title.setFont(UIUtils.FONT_SUBTITLE);
        rankChartPanel.add(title);
        rankChartPanel.add(Box.createVerticalStrut(15));
        
        Vector<String[]> rankings = adminDAO.getMovieReservationCounts();
        
        if (rankings.isEmpty()) {
            rankChartPanel.add(new JLabel("예매 데이터가 없습니다."));
        } else {
            for (int i = 0; i < Math.min(4, rankings.size()); i++) {
                String[] row = rankings.get(i);
                String movieTitle = row[0];
                int count = Integer.parseInt(row[1]);
                double rate = (totalReservations > 0) ? (double) count / totalReservations * 100.0 : 0.0;
                
                JPanel rankItem = createRankChartItem(movieTitle, rate);
                rankChartPanel.add(rankItem);
                rankChartPanel.add(Box.createVerticalStrut(10));
            }
        }
        rankChartPanel.revalidate();
        rankChartPanel.repaint();
    }
    
    // 순위 항목 (그래프 막대) 생성
    private JPanel createRankChartItem(String title, double rate) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.FONT_MAIN);
        titleLbl.setPreferredSize(new Dimension(100, 0));
        item.add(titleLbl, BorderLayout.WEST);

        // 그래프 막대
        int barWidth = (int) (rate * 2.5); // 최대 100% * 2.5
        if (barWidth < 5) barWidth = 5;
        
        JPanel bar = new JPanel();
        bar.setBackground(UIUtils.COLOR_ACCENT);
        bar.setPreferredSize(new Dimension(barWidth, 15));
        
        JLabel rateLbl = new JLabel(String.format("%.1f%%", rate));
        rateLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        rateLbl.setForeground(UIUtils.COLOR_TEXT);
        rateLbl.setBorder(new EmptyBorder(0, 5, 0, 0));

        JPanel barContainer = new JPanel(new BorderLayout(5, 0));
        barContainer.setBackground(Color.WHITE);
        barContainer.add(bar, BorderLayout.WEST);
        barContainer.add(rateLbl, BorderLayout.CENTER);
        
        item.add(barContainer, BorderLayout.CENTER);
        
        return item;
    }

    // 1위 영화 정보 카드 업데이트
    private void updateTopMovieCard(int totalReservations) {
        topMoviePanel.removeAll();
        topMoviePanel.setLayout(new BoxLayout(topMoviePanel, BoxLayout.Y_AXIS));
        topMoviePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] topMovie = adminDAO.getTopRankMovieDetail();
        
        if (topMovie == null || totalReservations == 0) {
            topMoviePanel.add(new JLabel("예매 데이터가 없습니다."));
        } else {
            String title = topMovie[0];
            String genre = topMovie[1];
            String time = topMovie[2];
            String path = topMovie[3];
            
            // 1. 순위/제목
            JLabel rankTitle = new JLabel("예매율 1위");
            rankTitle.setFont(UIUtils.FONT_SUBTITLE);
            rankTitle.setForeground(UIUtils.COLOR_ACCENT);

            JLabel movieTitle = new JLabel(title);
            movieTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
            movieTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            topMoviePanel.add(rankTitle);
            topMoviePanel.add(Box.createVerticalStrut(10));
            topMoviePanel.add(movieTitle);
            topMoviePanel.add(Box.createVerticalStrut(15));
            
            // 2. 포스터
            JLabel poster = new JLabel();
            poster.setPreferredSize(new Dimension(180, 250));
            poster.setAlignmentX(Component.CENTER_ALIGNMENT);
            poster.setOpaque(true); 
            poster.setBackground(new Color(240, 240, 240));
            
            if(path != null) {
                ImageIcon ic = new ImageIcon(path);
                if (ic.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    poster.setIcon(new ImageIcon(ic.getImage().getScaledInstance(180, 250, Image.SCALE_SMOOTH)));
                } else poster.setText("No Image");
            } else poster.setText("No Image");

            topMoviePanel.add(poster);
            topMoviePanel.add(Box.createVerticalStrut(15));
            
            // 3. 메타 정보 (장르, 러닝타임)
            JLabel meta = new JLabel(genre + " / " + time + "분");
            meta.setForeground(Color.GRAY);
            meta.setAlignmentX(Component.CENTER_ALIGNMENT);
            topMoviePanel.add(meta);
            
            // 4. 예매율 (임시)
            int topCount = Integer.parseInt(adminDAO.getMovieReservationCounts().get(0)[1]);
            double rate = (double) topCount / totalReservations * 100.0;
            JLabel rateLbl = new JLabel(String.format("예매율: %.1f%%", rate));
            rateLbl.setFont(UIUtils.FONT_SUBTITLE);
            rateLbl.setForeground(UIUtils.COLOR_ACCENT);
            rateLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            topMoviePanel.add(Box.createVerticalStrut(15));
            topMoviePanel.add(rateLbl);
        }
        topMoviePanel.revalidate();
        topMoviePanel.repaint();
    }


    public void refreshData() {
        // 1. 통계 갱신
        int totalReservations = adminDAO.getTotalReservations();
        long snackSales = adminDAO.getTotalSnackSales();
        long ticketSales = totalReservations * 12000L; // 임시 티켓 가격 12,000원 가정
        long totalSales = ticketSales + snackSales;
        
        // 상영 영화 수는 임시로 5편 설정
        int runningMovies = 5; 
        
        DecimalFormat df = new DecimalFormat("#,###");

        todaySalesValue.setText(df.format(totalSales) + "원");
        visitorCountValue.setText(df.format(totalReservations) + "명"); // 예매 건수를 방문자 수로 가정
        reservationRateValue.setText("68.4%"); // 임시값 (정확한 계산을 위한 데이터 부재)
        movieCountValue.setText(runningMovies + "편");

        // 2. 순위 차트 & 1위 영화 카드 업데이트
        updateRankChart(totalReservations);
        if (totalReservations > 0 && adminDAO.getMovieReservationCounts().size() > 0) {
             updateTopMovieCard(totalReservations);
        } else {
            topMoviePanel.removeAll();
            topMoviePanel.add(new JLabel("데이터 없음", SwingConstants.CENTER));
            topMoviePanel.revalidate();
            topMoviePanel.repaint();
        }
    }
}