package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import cinema.CinemaMain;
import cinema.dao.MovieDAO;
import cinema.dao.ScheduleDAO;
import cinema.dao.UserDAO;
import cinema.domain.User;
import cinema.domain.Movie;
import cinema.domain.Schedule;
import cinema.util.UIUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class UserMainPanel extends JPanel {
    private CinemaMain mainFrame;
    private JPanel movieGrid;
    private MovieDAO movieDAO;
    private LocalDate currentSelectedDate; 
    
    private JLabel cartCountLabel;
    private JLabel dateLabel;
    
    // [신규 필드] 검색어 입력 필드
    private JTextField searchField; 

    public UserMainPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.movieDAO = new MovieDAO();
        this.currentSelectedDate = LocalDate.now();

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);

        // 1. 헤더 (기존 로직 유지)
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

        // '관리자' 탭 (재인증 로직 추가)
        JLabel adminLabel = new JLabel("관리자");
        adminLabel.setFont(UIUtils.FONT_MAIN);
        adminLabel.setForeground(UIUtils.COLOR_TEXT);
        adminLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                 User currentUser = mainFrame.getCurrentUser();
                 
                 if (currentUser != null && currentUser.isAdmin()) {
                     // 보안 강화: 비밀번호 재인증 다이얼로그 호출
                     UserDAO userDAO = new UserDAO();
                     AdminAuthDialog authDialog = new AdminAuthDialog(mainFrame, currentUser, userDAO);
                     authDialog.setVisible(true);

                     if (authDialog.isAuthenticated()) {
                         mainFrame.showCard("ADMIN_MAIN");
                     }
                 } else { JOptionPane.showMessageDialog(mainFrame, "관리자만 접근 가능합니다."); }
             }
             @Override public void mouseEntered(java.awt.event.MouseEvent e) { adminLabel.setForeground(UIUtils.COLOR_ACCENT); }
             @Override public void mouseExited(java.awt.event.MouseEvent e) { adminLabel.setForeground(UIUtils.COLOR_TEXT); }
        });
        rightBox.add(adminLabel);
        
        // 검색 필드 및 버튼 추가
        searchField = UIUtils.createTextField(15);
        searchField.setText("영화 검색");
        
        // ★★★ [수정] 검색창 클릭 이벤트 (내용 초기화) ★★★
        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.getText().equals("영화 검색")) {
                    searchField.setText("");
                }
            }
        });
        searchField.addActionListener(e -> loadMovieData()); 
        
        JButton searchBtn = UIUtils.createOutlineButton("🔍");
        searchBtn.addActionListener(e -> loadMovieData());

        // 초기화 버튼
        JButton resetBtn = UIUtils.createOutlineButton("초기화");
        resetBtn.addActionListener(e -> resetSearch());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn); 
        
        rightBox.add(searchPanel);
        
        // 마이페이지 아이콘 (👤)
        JLabel myPageIcon = new JLabel("👤"); 
        myPageIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        myPageIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        myPageIcon.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override public void mouseClicked(java.awt.event.MouseEvent e) {
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
        cartIconPanel.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                 mainFrame.showCard("SNACK_SHOP"); 
             }
        });
        
        rightBox.add(cartIconPanel);
        
        // 로그아웃 아이콘 (🚪)
        JLabel logoutIcon = new JLabel("🚪");
        logoutIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        logoutIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutIcon.addMouseListener(new java.awt.event.MouseAdapter() {
             @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                 mainFrame.showCard("LOGIN"); 
             }
        });
        rightBox.add(logoutIcon);

        header.add(leftMenu, BorderLayout.WEST);
        header.add(rightBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. 날짜 선택바 (기존 로직 유지)
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        datePanel.setBackground(UIUtils.BG_MAIN);
        
        JButton prevBtn = createArrowBtn("<");
        JButton nextBtn = createArrowBtn(">");
        JButton calBtn = createArrowBtn("📅");
        
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        dateLabel.setForeground(UIUtils.COLOR_TEXT);
        dateLabel.setPreferredSize(new Dimension(240, 40));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);

        prevBtn.addActionListener(e -> changeDate(-1));
        nextBtn.addActionListener(e -> changeDate(1));
        calBtn.addActionListener(e -> openCalendar());

        datePanel.add(prevBtn);
        datePanel.add(dateLabel);
        datePanel.add(nextBtn);
        datePanel.add(calBtn);
        
        // 3. 영화 리스트 (기존 로직 유지)
        movieGrid = new JPanel(new GridLayout(0, 1, 0, 20)); 
        movieGrid.setBackground(UIUtils.BG_MAIN);
        movieGrid.setBorder(new EmptyBorder(10, 100, 40, 100)); 

        JScrollPane scrollPane = new JScrollPane(movieGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UIUtils.BG_MAIN);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(datePanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        
        updateDateLabel();
        loadMovieData();
    }

    private JLabel createNavLabel(String text, String cardName) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_MAIN);
        label.setForeground(UIUtils.COLOR_TEXT);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { label.setForeground(UIUtils.COLOR_ACCENT); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { label.setForeground(UIUtils.COLOR_TEXT); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { 
                if (cardName.equals("USER_MAIN")) loadMovieData(); 
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

    private JButton createArrowBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(Color.WHITE);
        btn.setForeground(UIUtils.COLOR_TEXT);
        btn.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 10));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(50, 40));
        return btn;
    }

    private void changeDate(int days) {
        LocalDate newDate = currentSelectedDate.plusDays(days);
        
        if (days < 0 && newDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "지난 날짜의 영화는 조회할 수 없습니다.");
            return;
        }
        
        currentSelectedDate = newDate;
        updateDateLabel();
        loadMovieData();
    }

    private void updateDateLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
        String dayOfWeek = currentSelectedDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
        dateLabel.setText(currentSelectedDate.format(formatter) + " (" + dayOfWeek + ")");
    }

    private void openCalendar() {
        CalendarDialog dialog = new CalendarDialog(mainFrame);
        dialog.setVisible(true);
        String picked = dialog.getSelectedDate();
        if (picked != null) {
            LocalDate selected = LocalDate.parse(picked);
            
            if (selected.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "지난 날짜의 영화는 조회할 수 없습니다.", "선택 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            currentSelectedDate = selected;
            updateDateLabel();
            loadMovieData();
        }
    }
    
    private void resetSearch() {
        searchField.setText("영화 검색");
        loadMovieData();
    }

    public void loadMovieData() {
        movieGrid.removeAll();
        
        String dateStr = currentSelectedDate.toString();
        
        String searchQuery = searchField.getText().trim();
        if (searchQuery.equals("영화 검색") || searchQuery.isEmpty()) {
            searchQuery = null; 
        }

        List<Movie> movies = movieDAO.getMoviesForUser(currentSelectedDate, searchQuery);

        if (movies.isEmpty()) {
            JLabel empty = new JLabel("상영 중인 영화가 없거나 검색 결과가 없습니다.");
            empty.setFont(UIUtils.FONT_SUBTITLE);
            empty.setForeground(Color.GRAY);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            movieGrid.add(empty);
        } else {
            for (Movie m : movies) {
                movieGrid.add(createModernMovieCard(m, dateStr));
            }
        }
        movieGrid.revalidate();
        movieGrid.repaint();
    }
    
    private JPanel createModernMovieCard(Movie movie, String dateStr) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15), 
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(0, 220));

        // 포스터
        JLabel poster = new JLabel();
        poster.setPreferredSize(new Dimension(120, 180));
        poster.setOpaque(true); 
        poster.setBackground(new Color(240, 240, 240));
        poster.setHorizontalAlignment(SwingConstants.CENTER);
        
        if(movie.getPosterPath() != null) {
            ImageIcon ic = new ImageIcon(movie.getPosterPath());
            if (ic.getImageLoadStatus() == MediaTracker.COMPLETE) {
                poster.setIcon(new ImageIcon(ic.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH)));
            } else poster.setText("No Image");
        } else poster.setText("No Image");

        // 우측 정보
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        JLabel titleLbl = new JLabel(movie.getTitle());
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel metaLbl = new JLabel(movie.getGenre() + "  |  " + movie.getRunningTime() + "분");
        metaLbl.setFont(UIUtils.FONT_MAIN);
        metaLbl.setForeground(UIUtils.COLOR_TEXT_GRAY);
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 시간표 영역
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        timePanel.setBackground(Color.WHITE);
        timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        List<Schedule> schedules = scheduleDAO.getSchedulesByMovieAndDate(movie.getId(), dateStr);

        // 현재 시간과 비교하여 지난 시간 비활성화
        LocalDateTime now = LocalDateTime.now();

        if (schedules.isEmpty()) {
            JLabel noShow = new JLabel("상영 일정이 없습니다.");
            noShow.setForeground(Color.GRAY);
            timePanel.add(noShow);
        } else {
            for (Schedule s : schedules) {
                // 스케줄 날짜+시간 파싱
                LocalDate sDate = LocalDate.parse(s.getShowDate());
                LocalTime sTime = LocalTime.parse(s.getStartTime());
                LocalDateTime scheduleDateTime = LocalDateTime.of(sDate, sTime);

                JButton timeBtn = UIUtils.createTimeButton(s.getStartTime() + " " + s.getScreenName());
                
                // 시간이 지났으면 비활성화
                if (scheduleDateTime.isBefore(now)) {
                    timeBtn.setEnabled(false);
                    timeBtn.setForeground(Color.LIGHT_GRAY); 
                    timeBtn.setToolTipText("상영 시간이 지났습니다.");
                } else {
                    timeBtn.addActionListener(e -> {
                        User user = mainFrame.getCurrentUser();
                        if (user == null) { JOptionPane.showMessageDialog(this, "로그인 필요"); return; }
                        new SeatSelectionDialog(mainFrame, user, movie, s).setVisible(true);
                    });
                }
                timePanel.add(timeBtn);
            }
        }

        rightPanel.add(titleLbl);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(metaLbl);
        rightPanel.add(Box.createVerticalStrut(20));
        
        JLabel timeLabel = new JLabel("상영 시간");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        rightPanel.add(timeLabel);
        rightPanel.add(timePanel);

        card.add(poster, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);
        
        return card;
    }
}