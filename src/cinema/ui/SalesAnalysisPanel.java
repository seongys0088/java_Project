package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.CinemaMain;
import cinema.dao.AdminDAO;
import cinema.util.UIUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Vector;

public class SalesAnalysisPanel extends JPanel {
    private CinemaMain mainFrame;
    private AdminDAO adminDAO;
    private LocalDate currentSelectedDate; 
    
    // UI 필드
    private JLabel totalSalesValue;
    private JLabel averageTicketValue;
    private JLabel salesRatioValue;
    private JLabel dummyStatValue; 
    private JLabel dateDisplayLabel; 
    private JPanel contributionListPanel;
    private JPanel weeklyChartPanel; 
    
    // 동적 변화율 라벨 필드 추가
    private JLabel totalSalesChangeLabel;
    private JLabel averageTicketChangeLabel;
    private JLabel salesRatioChangeLabel; 

    public SalesAnalysisPanel(CinemaMain mainFrame) {
        this.adminDAO = new AdminDAO();
        this.mainFrame = mainFrame; 
        this.currentSelectedDate = LocalDate.now(); 
        
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40)); 

        // 1. 상단 타이틀 및 버튼 (기존 로직 유지)
        JLabel title = new JLabel("매출 분석");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(UIUtils.COLOR_TEXT);
        
        JLabel subTitle = new JLabel("실시간 매출 현황과 기간별 성과를 분석합니다.");
        subTitle.setFont(UIUtils.FONT_MAIN);
        subTitle.setForeground(Color.GRAY);
        
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(UIUtils.BG_MAIN);
        headerPanel.add(title);
        headerPanel.add(subTitle);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // 우측 버튼 및 날짜 표시
        JPanel topBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topBtnPanel.setBackground(UIUtils.BG_MAIN);
        
        dateDisplayLabel = new JLabel();
        dateDisplayLabel.setFont(UIUtils.FONT_MAIN);
        dateDisplayLabel.setForeground(UIUtils.COLOR_TEXT);
        
        JButton setPeriodBtn = UIUtils.createOutlineButton("기간 설정");
        JButton downloadReportBtn = UIUtils.createStyledButton("레포트 다운로드");
        
        setPeriodBtn.addActionListener(e -> openCalendarDialog());
        downloadReportBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "매출 분석 레포트를 다운로드합니다. (로직 구현 필요)", "다운로드 안내", JOptionPane.INFORMATION_MESSAGE));

        topBtnPanel.add(dateDisplayLabel);
        topBtnPanel.add(setPeriodBtn);
        topBtnPanel.add(downloadReportBtn);
        
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(UIUtils.BG_MAIN);
        topContainer.add(headerPanel, BorderLayout.WEST);
        topContainer.add(topBtnPanel, BorderLayout.EAST);
        
        add(topContainer, BorderLayout.NORTH);

        // 2. 통계 카드 영역 (4개)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(UIUtils.BG_MAIN);
        statsPanel.setPreferredSize(new Dimension(0, 150));
        
        totalSalesValue = new JLabel("₩ 0");
        averageTicketValue = new JLabel("₩ 0");
        salesRatioValue = new JLabel("0:0");
        dummyStatValue = new JLabel("0%");

        // 변화율 라벨 초기화
        totalSalesChangeLabel = new JLabel(" - 0.0%");
        averageTicketChangeLabel = new JLabel(" - 0.0%");
        salesRatioChangeLabel = new JLabel(" - 0.0%");
        
        // createStatCardDynamic으로 카드 생성
        statsPanel.add(createStatCardDynamic("이번 달 총 매출", totalSalesValue, "$", new Color(40, 167, 69), totalSalesChangeLabel));
        statsPanel.add(createStatCardDynamic("객단가", averageTicketValue, "₩", new Color(255, 152, 0), averageTicketChangeLabel));
        statsPanel.add(createStatCardDynamic("매점/티켓 비율", salesRatioValue, "○", new Color(0, 123, 255), salesRatioChangeLabel));
        statsPanel.add(createStatCardDynamic("더미 통계", dummyStatValue, "▲", new Color(220, 53, 69), new JLabel(" - 0.0%")));
        
        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(UIUtils.BG_MAIN);
        statsWrapper.add(statsPanel, BorderLayout.CENTER);
        statsWrapper.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // 3. 주간 매출 추이 (차트 영역) + 영화 기여도
        
        // [좌측] 주간 매출 추이
        weeklyChartPanel = new JPanel(new BorderLayout()); 
        weeklyChartPanel.setBackground(Color.WHITE);
        weeklyChartPanel.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15));
        
        JLabel chartTitle = new JLabel("주간 매출 추이", SwingConstants.LEFT);
        chartTitle.setFont(UIUtils.FONT_SUBTITLE);
        chartTitle.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        weeklyChartPanel.add(chartTitle, BorderLayout.NORTH);
        
        // [우측] 영화별 매출 기여도
        JPanel contributionPanel = new JPanel(new BorderLayout());
        contributionPanel.setBackground(Color.WHITE);
        contributionPanel.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15));

        JLabel contributionTitle = new JLabel("영화별 매출 기여도", SwingConstants.LEFT);
        contributionTitle.setFont(UIUtils.FONT_SUBTITLE);
        contributionTitle.setBorder(new EmptyBorder(15, 20, 15, 20));

        contributionListPanel = new JPanel();
        contributionListPanel.setLayout(new BoxLayout(contributionListPanel, BoxLayout.Y_AXIS));
        contributionListPanel.setBackground(Color.WHITE);
        contributionListPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JScrollPane contributionScroll = new JScrollPane(contributionListPanel);
        contributionScroll.setBorder(null);
        contributionScroll.getVerticalScrollBar().setUnitIncrement(16);
        contributionScroll.getViewport().setBackground(Color.WHITE);

        contributionPanel.add(contributionTitle, BorderLayout.NORTH);
        contributionPanel.add(contributionScroll, BorderLayout.CENTER);
        
        // 전체 조립
        JPanel mainContent = new JPanel(new BorderLayout(0, 30));
        mainContent.add(statsWrapper, BorderLayout.NORTH);
        
        JPanel chartAndContribution = new JPanel(new GridLayout(1, 2, 20, 0));
        chartAndContribution.setBackground(UIUtils.BG_MAIN);
        chartAndContribution.add(weeklyChartPanel);
        chartAndContribution.add(contributionPanel);
        
        mainContent.add(chartAndContribution, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { refreshData(currentSelectedDate); }
        });
        
        refreshData(currentSelectedDate);
    }
    
    // CalendarDialog 연결 메서드
    private void openCalendarDialog() {
        CalendarDialog dialog = new CalendarDialog(mainFrame);
        dialog.setVisible(true);
        String picked = dialog.getSelectedDate();
        if (picked != null) {
            currentSelectedDate = LocalDate.parse(picked);
            refreshData(currentSelectedDate);
        }
    }
    
    // 통계 카드 생성기 (동적 변화율 라벨을 받도록 수정)
    private JPanel createStatCardDynamic(String title, JLabel valueLabel, String icon, Color iconColor, JLabel changeRateLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // 아이콘과 타이틀
        JPanel iconTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        iconTitlePanel.setBackground(Color.WHITE);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setForeground(iconColor);
        iconLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(Color.GRAY);
        titleLbl.setFont(UIUtils.FONT_MAIN);
        iconTitlePanel.add(iconLbl);
        iconTitlePanel.add(titleLbl);
        
        // 값 스타일
        valueLabel.setForeground(UIUtils.COLOR_TEXT);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // 변화율 라벨 스타일링
        updateChangeLabelColor(changeRateLabel);
        changeRateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        card.add(iconTitlePanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(changeRateLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    // 영화별 기여도 목록 항목 생성기 (기존과 동일)
    private JPanel createContributionItem(int rank, String title, String path, String contributionRate, long amount) {
        JPanel item = new JPanel(new BorderLayout(15, 0));
        item.setBackground(Color.WHITE);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        item.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        DecimalFormat df = new DecimalFormat("#,###원");
        
        // 1. 순위 및 제목
        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setBackground(Color.WHITE);
        JLabel rankLbl = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
        rankLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        rankLbl.setPreferredSize(new Dimension(20, 0));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIUtils.FONT_MAIN);
        titleLbl.setForeground(UIUtils.COLOR_TEXT);
        
        left.add(rankLbl, BorderLayout.WEST);
        left.add(titleLbl, BorderLayout.CENTER);
        
        // 2. 기여율 및 금액
        JPanel right = new JPanel(new BorderLayout(10, 0));
        right.setBackground(Color.WHITE);
        
        JLabel rateLbl = new JLabel(contributionRate, SwingConstants.RIGHT);
        rateLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        rateLbl.setForeground(Color.GRAY);
        
        JLabel amountLbl = new JLabel(df.format(amount), SwingConstants.RIGHT);
        amountLbl.setFont(UIUtils.FONT_MAIN);
        amountLbl.setForeground(UIUtils.COLOR_TEXT);
        
        right.add(rateLbl, BorderLayout.WEST);
        right.add(amountLbl, BorderLayout.EAST);

        item.add(left, BorderLayout.WEST);
        item.add(right, BorderLayout.EAST);
        
        // 구분선
        item.add(new JSeparator(), BorderLayout.SOUTH);

        return item;
    }
    
    // [신규 헬퍼] 변화율 계산 함수 (DB 연동)
    private String calculateChangeRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "↑ +100%" : "- 0.0%";
        }
        
        double change = (double)(current - previous) / previous * 100.0;
        String sign = change >= 0 ? "↑ +" : "↓ ";
        
        return sign + String.format("%.1f%%", Math.abs(change));
    }
    
    // [신규 헬퍼] 변화율 라벨 색상 업데이트
    private void updateChangeLabelColor(JLabel label) {
        String changeText = label.getText();
        if (changeText.startsWith("↑")) {
            label.setForeground(new Color(40, 167, 69)); // 초록색
        } else if (changeText.startsWith("↓")) {
            label.setForeground(new Color(220, 53, 69)); // 빨간색
        } else {
            label.setForeground(Color.GRAY);
        }
    }

    // AdminMainPanel에서 호출될 때 사용하는 오버로드 메서드
    public void refreshData() {
        refreshData(currentSelectedDate);
    }

    // 데이터 새로고침 메서드 (날짜 파라미터로 내부 로직 처리)
    public void refreshData(LocalDate date) {
        // 1. 기준 날짜 설정 및 표시 업데이트
        currentSelectedDate = date; 
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
        dateDisplayLabel.setText("기준일: " + dateStr);
        
        // 2. 통계 카드 데이터 및 변화율 계산 (MoM)
        
        LocalDate currentMonth = date.withDayOfMonth(1);
        LocalDate previousMonth = currentMonth.minusMonths(1);
        
        // 현재 월 데이터
        long currentTotalTicketSales = adminDAO.getTicketSalesByMonth(currentMonth);
        long currentTotalSnackSales = adminDAO.getSnackSalesByMonth(currentMonth);
        long currentTotalSales = currentTotalTicketSales + currentTotalSnackSales;
        long currentTotalReservations = adminDAO.getTotalReservationsByMonth(currentMonth);
        long currentAvgTicket = currentTotalReservations > 0 ? currentTotalSales / currentTotalReservations : 0;
        
        // 지난 달 데이터 (비교 대상)
        long prevTotalSales = adminDAO.getTicketSalesByMonth(previousMonth) + adminDAO.getSnackSalesByMonth(previousMonth);
        long prevTotalReservations = adminDAO.getTotalReservationsByMonth(previousMonth);
        long prevAvgTicket = prevTotalReservations > 0 ? prevTotalSales / prevTotalReservations : 0;
        
        // 변화율 계산
        String totalSalesChange = calculateChangeRate(currentTotalSales, prevTotalSales);
        String avgTicketChange = calculateChangeRate(currentAvgTicket, prevAvgTicket);

        DecimalFormat df = new DecimalFormat("#,###");

        // UI 값 업데이트
        totalSalesValue.setText("₩ " + df.format(currentTotalSales));
        averageTicketValue.setText("₩ " + df.format(currentAvgTicket));
        salesRatioValue.setText(adminDAO.getSnackTicketRatio());
        
        // 더미 통계 (전체 기준)
        long totalSalesRatio = currentTotalSales > 0 ? currentTotalSales : 1;
        long currentTotalSnackSalesForDummy = adminDAO.getTotalSnackSales(); 
        dummyStatValue.setText(new DecimalFormat("0.0%").format((double)currentTotalSnackSalesForDummy / totalSalesRatio));

        // 변화율 라벨 업데이트 및 색상 적용
        totalSalesChangeLabel.setText(totalSalesChange);
        averageTicketChangeLabel.setText(avgTicketChange);
        salesRatioChangeLabel.setText(" - 0.0%"); // ★ 더미 값으로 초기화 (비율 변화율은 복잡하여 임의 처리)
        
        updateChangeLabelColor(totalSalesChangeLabel);
        updateChangeLabelColor(averageTicketChangeLabel);
        updateChangeLabelColor(salesRatioChangeLabel);

        // 3. 영화별 기여도 목록 업데이트 (기존 로직 유지)
        contributionListPanel.removeAll();
        
        Vector<String[]> contributions = adminDAO.getMovieSalesContributionDetailed();
        
        if (contributions.isEmpty() || adminDAO.getTotalReservations() == 0) {
            JLabel empty = new JLabel("매출 데이터가 없습니다.", SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(50, 0, 0, 0));
            contributionListPanel.add(empty);
            contributionListPanel.add(Box.createVerticalGlue());
        } else {
            int rank = 1;
            for (String[] row : contributions) {
                contributionListPanel.add(createContributionItem(
                    rank++, 
                    row[0], 
                    row[3], 
                    row[2], 
                    Long.parseLong(row[1])
                ));
            }
        }
        
        // 4. 주간 매출 추이 차트 업데이트 (재생성)
        Component existingChart = ((BorderLayout)weeklyChartPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (existingChart != null) {
            weeklyChartPanel.remove(existingChart);
        }
        
        // 새로운 차트 플레이스홀더 추가
        weeklyChartPanel.add(new WeeklySalesChartPlaceholder(date), BorderLayout.CENTER);
        weeklyChartPanel.revalidate();
        weeklyChartPanel.repaint();

        contributionListPanel.revalidate();
        contributionListPanel.repaint();
    }
    
    // 주간 매출 추이 차트 플레이스홀더 클래스 (실제 DB 데이터 이용)
    private class WeeklySalesChartPlaceholder extends JPanel {
        public WeeklySalesChartPlaceholder(LocalDate startDate) {
            setLayout(new GridLayout(1, 7, 10, 0));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 20, 20, 20));
            
            // 1. 주의 시작일 (월요일) 계산
            LocalDate startOfWeek = startDate.with(DayOfWeek.MONDAY); 
            
         // 2. DAO를 통해 실제 주간 매출 데이터 가져오기
            long[] weeklySales = adminDAO.getWeeklySales(startOfWeek);
            
            long maxSale = 0;
            for (long sale : weeklySales) maxSale = Math.max(maxSale, sale);
            
            for (int i = 0; i < 7; i++) {
                LocalDate day = startOfWeek.plusDays(i);
                
                JPanel dayColumn = new JPanel(new BorderLayout(0, 5));
                dayColumn.setBackground(Color.WHITE);

                // Bar (Visual element)
                JPanel barContainer = new JPanel(new BorderLayout());
                barContainer.setBackground(Color.WHITE);
                
                // 데이터에 기반한 동적 높이 계산 (최대 150px)
                int height = (maxSale > 0) ? (int)(((double)weeklySales[i] / maxSale) * 150) : 0;
                if (height < 10 && weeklySales[i] > 0) height = 10; 
                
                JPanel bar = new JPanel();
                bar.setBackground(new Color(0, 123, 255)); // 기본 막대 색상
                bar.setPreferredSize(new Dimension(20, height));
                
                barContainer.add(Box.createVerticalGlue(), BorderLayout.NORTH); 
                barContainer.add(bar, BorderLayout.CENTER);
                
                // Day Label
                String dayName = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                JLabel dayLbl = new JLabel(dayName, SwingConstants.CENTER);
                dayLbl.setForeground(UIUtils.COLOR_TEXT_GRAY);
                
                // 선택된 날짜 강조 
                if (day.equals(startDate)) {
                    dayLbl.setForeground(UIUtils.COLOR_ACCENT);
                    bar.setBackground(UIUtils.COLOR_ACCENT); // 기준일 막대 색상 변경
                }

                dayColumn.add(barContainer, BorderLayout.CENTER);
                dayColumn.add(dayLbl, BorderLayout.SOUTH);

                add(dayColumn);
            }
        }
    }
}