package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import cinema.CinemaMain;
import cinema.dao.AdminDAO;
import cinema.util.UIUtils;

import java.awt.*;
import java.io.File;
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RankingPanel extends JPanel {
    private CinemaMain mainFrame;
    private AdminDAO adminDAO;
    private JPanel rankListPanel;
    
    private JLabel cartCountLabel;
    
    // ★★★ [추가] 검색 필드 ★★★
    private JTextField searchField; 

    public RankingPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.adminDAO = new AdminDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN); 

        // 1. 헤더 (기능 복원)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER));
        header.setPreferredSize(new Dimension(0, 70));
        
        // 왼쪽: 로고 및 메뉴
        JPanel leftMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        leftMenu.setOpaque(false);
        
        JLabel logo = new JLabel("CINEPRIME"); 
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(UIUtils.COLOR_ACCENT);
        logo.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        leftMenu.add(logo);
        leftMenu.add(createNavLabel("영화 예매", "USER_MAIN"));
        leftMenu.add(createNavLabel("예매 순위", "RANKINGS")); 
        leftMenu.add(createNavLabel("스낵바", "SNACK_SHOP"));

        header.add(leftMenu, BorderLayout.WEST);
        
        // 오른쪽: 관리자, 검색, 마이페이지, 장바구니, 로그아웃 
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        rightBox.setOpaque(false);

        // '관리자' 탭
        JLabel adminLabel = new JLabel("관리자");
        adminLabel.setFont(UIUtils.FONT_MAIN);
        adminLabel.setForeground(UIUtils.COLOR_TEXT);
        adminLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminLabel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 if (mainFrame.getCurrentUser() != null && mainFrame.getCurrentUser().isAdmin()) {
                     mainFrame.showCard("ADMIN_MAIN"); 
                 } else { JOptionPane.showMessageDialog(mainFrame, "관리자만 접근 가능합니다."); }
             }
             @Override public void mouseEntered(MouseEvent e) { adminLabel.setForeground(UIUtils.COLOR_ACCENT); }
             @Override public void mouseExited(MouseEvent e) { adminLabel.setForeground(UIUtils.COLOR_TEXT); }
        });
        rightBox.add(adminLabel);
        
        // ★★★ [추가] 검색 필드 및 버튼 ★★★
        searchField = UIUtils.createTextField(15);
        searchField.setText("영화 검색");
        
        // 검색창 클릭 시 초기화 이벤트
        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.getText().equals("영화 검색")) {
                    searchField.setText("");
                }
            }
        });
        searchField.addActionListener(e -> refreshData()); // Enter 키로 검색
        
        JButton searchBtn = UIUtils.createOutlineButton("🔍");
        searchBtn.addActionListener(e -> refreshData());
        
        // 초기화 버튼
        JButton resetBtn = UIUtils.createOutlineButton("초기화");
        resetBtn.addActionListener(e -> resetSearch());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn); // 초기화 버튼 추가
        
        rightBox.add(searchPanel); // 검색 컴포넌트 추가
        
        // 마이페이지 아이콘 (👤)
        JLabel myPageIcon = new JLabel("👤"); 
        myPageIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        myPageIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        myPageIcon.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 MyPagePanel panel = (MyPagePanel) mainFrame.getPanel("MY_PAGE");
                 if (panel != null) panel.refreshData();
                 mainFrame.showCard("MY_PAGE"); 
             }
        });
        rightBox.add(myPageIcon);
        
        // 장바구니 아이콘 (🛒) + 알림 배지
        JPanel cartIconPanel = new JPanel(null); 
        cartIconPanel.setOpaque(false);
        cartIconPanel.setPreferredSize(new Dimension(30, 30)); 
        
        JLabel cartIcon = new JLabel("🛒");
        cartIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        cartIcon.setBounds(0, 0, 25, 25);
        
        cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setBackground(UIUtils.COLOR_ACCENT);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cartCountLabel.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_ACCENT, 7)); 
        cartCountLabel.setBounds(15, 0, 15, 15); 
        cartCountLabel.setVisible(false); 
        
        cartIconPanel.add(cartIcon);
        cartIconPanel.add(cartCountLabel);
        
        cartIconPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cartIconPanel.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 mainFrame.showCard("SNACK_SHOP"); 
             }
        });
        
        rightBox.add(cartIconPanel);
        
        // 로그아웃 아이콘 (🚪)
        JLabel logoutIcon = new JLabel("🚪");
        logoutIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        logoutIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutIcon.addMouseListener(new MouseAdapter() {
             @Override public void mouseClicked(MouseEvent e) {
                 mainFrame.showCard("LOGIN"); 
             }
        });
        rightBox.add(logoutIcon);

        header.add(rightBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);


        // 2. 메인 콘텐츠 (기존과 동일)
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(UIUtils.BG_MAIN);
        mainContent.setBorder(new EmptyBorder(30, 100, 30, 100)); 

        // 타이틀
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIUtils.BG_MAIN);
        
        JLabel mainTitle = new JLabel("📈 전체 예매 순위");
        mainTitle.setFont(UIUtils.FONT_TITLE);
        mainTitle.setForeground(UIUtils.COLOR_TEXT);
        
        JLabel subTitle = new JLabel("실시간으로 집계된 영화 예매율 순위입니다.");
        subTitle.setFont(UIUtils.FONT_MAIN);
        subTitle.setForeground(Color.GRAY);
        
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 5));
        titleBox.setBackground(UIUtils.BG_MAIN);
        titleBox.add(mainTitle);
        titleBox.add(subTitle);
        
        titlePanel.add(titleBox, BorderLayout.WEST);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        mainContent.add(titlePanel, BorderLayout.NORTH);

        // 순위 리스트 패널
        rankListPanel = new JPanel();
        rankListPanel.setLayout(new BoxLayout(rankListPanel, BoxLayout.Y_AXIS));
        rankListPanel.setBackground(UIUtils.BG_MAIN);

        JScrollPane scrollPane = new JScrollPane(rankListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        
        mainContent.add(scrollPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { refreshData(); }
        });
        
        refreshData();
    }
    
    private JLabel createNavLabel(String text, String cardName) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_MAIN);
        label.setForeground(UIUtils.COLOR_TEXT);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { label.setForeground(UIUtils.COLOR_ACCENT); }
            @Override public void mouseExited(MouseEvent e) { label.setForeground(UIUtils.COLOR_TEXT); }
            @Override public void mouseClicked(MouseEvent e) { 
                mainFrame.showCard(cardName); 
            }
        });
        return label;
    }
    
    public void updateCartCount(int count) {
        if (cartCountLabel == null) return; 
        if (count > 0) {
            cartCountLabel.setText(String.valueOf(count));
            cartCountLabel.setVisible(true);
        } else {
            cartCountLabel.setVisible(false);
        }
    }

    // ★★★ [신규] 검색 초기화 기능 ★★★
    private void resetSearch() {
        searchField.setText("영화 검색");
        refreshData();
    }

    public void refreshData() {
        rankListPanel.removeAll();
        
        // ★★★ [수정] 검색어 반영 ★★★
        String searchQuery = searchField.getText().trim();
        if (searchQuery.equals("영화 검색") || searchQuery.isEmpty()) {
            searchQuery = null; 
        }
        
        // 총 예매 건수 (예매율 계산용)
        int totalReservations = adminDAO.getTotalReservations();
        
        // DAO에서 [순위, 제목, 예매수, 장르, 시간, 포스터] 가져옴 (검색어 전달)
        Vector<String[]> rankings = adminDAO.getMovieRankingsDetailed(searchQuery);
        
        if (rankings.isEmpty() || totalReservations == 0) {
            JLabel empty = new JLabel("예매 데이터가 없거나 검색 결과가 없습니다.");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(50,0,0,0));
            rankListPanel.add(empty);
            rankListPanel.add(Box.createVerticalGlue());
        } else {
            for (String[] row : rankings) {
                // row: [0]순위, [1]제목, [2]예매수, [3]장르, [4]시간, [5]포스터
                int rank = Integer.parseInt(row[0]);
                
                rankListPanel.add(createRankCard(
                    String.valueOf(rank), 
                    row[1], // 제목
                    row[3], // 장르
                    row[4], // 시간
                    row[5], // 포스터 경로
                    Integer.parseInt(row[2]), // 예매수
                    totalReservations
                ));
            }
        }
        
        rankListPanel.revalidate();
        rankListPanel.repaint();
    }
    
    // 순위 카드 생성 (기존과 동일)
    private JPanel createRankCard(String rank, String title, String genre, String time, String posterPath, int count, int totalReservations) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180)); 
        card.setMinimumSize(new Dimension(10, 180));
        card.setPreferredSize(new Dimension(10, 180));

        // 1. 순위 (왼쪽, 크고 붉은 폰트)
        JLabel rankLbl = new JLabel(rank);
        rankLbl.setFont(new Font("SansSerif", Font.BOLD, 48));
        rankLbl.setForeground(UIUtils.COLOR_ACCENT);
        rankLbl.setPreferredSize(new Dimension(70, 0));
        rankLbl.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 2. 포스터 (중앙 왼쪽)
        JLabel posterLbl = new JLabel();
        posterLbl.setPreferredSize(new Dimension(100, 140));
        posterLbl.setOpaque(true);
        posterLbl.setBackground(new Color(240, 240, 240));
        posterLbl.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (posterPath != null && !posterPath.isEmpty()) {
            File f = new File(posterPath);
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(posterPath);
                posterLbl.setIcon(new ImageIcon(ic.getImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH)));
            } else posterLbl.setText("No Img");
        } else posterLbl.setText("No Img");

        // 3. 영화 정보 (중앙)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // 제목
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        // 장르, 시간
        JLabel metaLbl = new JLabel(genre + " | " + time + "분");
        metaLbl.setFont(UIUtils.FONT_MAIN);
        metaLbl.setForeground(Color.GRAY);

        // 예매율 계산
        double rate = 0;
        if (totalReservations > 0) {
            rate = (double) count / totalReservations * 100.0;
        }
        
        // 예매율
        JLabel rateValue = new JLabel("예매율 " + String.format("%.1f%%", rate));
        rateValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        rateValue.setForeground(UIUtils.COLOR_ACCENT); 

        // 평점
        JLabel scoreValue = new JLabel("⭐ 4.8"); // 임시 평점
        scoreValue.setFont(UIUtils.FONT_MAIN);
        scoreValue.setForeground(new Color(245, 166, 35)); // 오렌지색
        
        // 메타 정보 박스 (예매율 + 평점)
        JPanel statsBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statsBox.setBackground(Color.WHITE);
        statsBox.add(rateValue);
        statsBox.add(scoreValue);

        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(titleLbl);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(metaLbl);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(statsBox);


        // 4. 예매하기 버튼 (오른쪽)
        JButton reserveBtn = UIUtils.createOutlineButton("예매하기"); 
        reserveBtn.setPreferredSize(new Dimension(100, 40));
        reserveBtn.setMaximumSize(new Dimension(100, 40));
        reserveBtn.setForeground(UIUtils.COLOR_ACCENT);
        reserveBtn.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_ACCENT));
        
        reserveBtn.addActionListener(e -> {
             JOptionPane.showMessageDialog(mainFrame, "[" + title + "] 영화 예매 페이지로 이동");
             mainFrame.showCard("USER_MAIN");
        });
        
        JPanel btnWrapper = new JPanel(new GridBagLayout()); // 버튼 세로 중앙 정렬
        btnWrapper.setBackground(Color.WHITE);
        btnWrapper.add(reserveBtn);

        // 레이아웃 조립
        JPanel centerWrapper = new JPanel(new BorderLayout(20, 0));
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(posterLbl, BorderLayout.WEST);
        centerWrapper.add(infoPanel, BorderLayout.CENTER);

        card.add(rankLbl, BorderLayout.WEST);
        card.add(centerWrapper, BorderLayout.CENTER);
        card.add(btnWrapper, BorderLayout.EAST);

        // 카드 간 간격
        JPanel cardContainer = new JPanel(new BorderLayout());
        cardContainer.setBackground(UIUtils.BG_MAIN);
        cardContainer.setBorder(new EmptyBorder(0, 0, 15, 0)); // 아래쪽 간격
        cardContainer.add(card, BorderLayout.CENTER);

        return cardContainer;
    }
}