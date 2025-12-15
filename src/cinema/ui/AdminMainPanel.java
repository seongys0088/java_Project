package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.MovieDAO;
import cinema.domain.Movie;
import cinema.util.UIUtils;

import java.awt.*;
import java.util.List;

public class AdminMainPanel extends JPanel {
    private CinemaMain mainFrame;
    private JPanel contentPanel;
    private CardLayout contentLayout;
    
    // 헤더 컴포넌트 추가
    private JPanel headerPanel; 
    private JLabel currentPanelTitle; 

    private JTable movieTable;
    private DefaultTableModel tableModel;
    private MovieDAO movieDAO;

    public AdminMainPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.movieDAO = new MovieDAO();
        
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN); 

        // 0. 헤더 패널 (기존 코드 유지)
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER));
        headerPanel.setPreferredSize(new Dimension(0, 70));

        // 왼쪽: 현재 패널 타이틀
        currentPanelTitle = new JLabel("대시보드"); 
        currentPanelTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        currentPanelTitle.setForeground(UIUtils.COLOR_TEXT);
        
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        leftHeader.setBackground(Color.WHITE);
        leftHeader.add(currentPanelTitle);
        
        // 오른쪽: 관리자 정보 및 사용자 모드 전환 버튼
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightHeader.setBackground(Color.WHITE);
        
        // 사용자 모드 전환 버튼
        JButton userModeBtn = UIUtils.createOutlineButton("→ 사용자 모드");
        userModeBtn.addActionListener(e -> mainFrame.showCard("USER_MAIN"));
        
        JLabel adminInfo = new JLabel("관리자님 (admin)");
        adminInfo.setFont(UIUtils.FONT_MAIN);
        adminInfo.setForeground(UIUtils.COLOR_TEXT_GRAY);
        
        rightHeader.add(adminInfo);
        rightHeader.add(new JLabel("👤") {{ setFont(new Font("SansSerif", Font.PLAIN, 20)); }});
        rightHeader.add(userModeBtn);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);


        // 1. 사이드바 (비율 수정)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(38, 43, 64)); // 다크 블루 계열
        sidebar.setPreferredSize(new Dimension(200, 0)); 
        sidebar.setBorder(null);

        // 로고 영역 (상단)
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(38, 43, 64));
        logoPanel.setBorder(new EmptyBorder(30, 20, 20, 20));
        JLabel logo = new JLabel("CineManager");
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logoPanel.add(logo);
        sidebar.add(logoPanel);
        
        // 메뉴 버튼들
        sidebar.add(createMenuButton("대시보드", "HOME", "📊"));
        sidebar.add(createMenuButton("영화 관리", "MOVIES", "🎬"));
        sidebar.add(createMenuButton("상영 일정", "SCHEDULES", "📅"));
        sidebar.add(createMenuButton("매점 관리", "SNACKS", "🍿")); 
        sidebar.add(createMenuButton("매출 분석", "SALES_ANALYTICS", "📈")); 
        sidebar.add(createMenuButton("회원 관리", "USER_MANAGEMENT", "👥")); 
        sidebar.add(createMenuButton("상영관 관리", "SCREENS", "🏢")); 
        
        sidebar.add(Box.createVerticalGlue());
        
        // 로그아웃 버튼
        JButton logoutBtn = UIUtils.createStyledButton("로그아웃");
        logoutBtn.setBackground(new Color(50, 50, 50));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(200, 40));
        logoutBtn.addActionListener(e -> mainFrame.showCard("LOGIN"));
        
        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(new Color(38, 43, 64));
        bottomBar.setBorder(new EmptyBorder(20, 10, 20, 10));
        bottomBar.add(logoutBtn);
        sidebar.add(bottomBar);

        add(sidebar, BorderLayout.WEST);

        // 2. 콘텐츠 영역
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(UIUtils.BG_MAIN);
        contentPanel.setBorder(null);

        // 패널 등록
        AdminHomePanel homePanel = new AdminHomePanel();
        SalesAnalysisPanel salesPanel = new SalesAnalysisPanel(mainFrame);
        UserManagementPanel userPanel = new UserManagementPanel(mainFrame); 
        
        contentPanel.add(homePanel, "HOME");
        contentPanel.add(createMovieMgmtPanel(), "MOVIES");
        contentPanel.add(new ScheduleAdminPanel(mainFrame), "SCHEDULES");
        contentPanel.add(new SnackAdminPanel(mainFrame), "SNACKS"); 
        contentPanel.add(salesPanel, "SALES_ANALYTICS"); 
        contentPanel.add(userPanel, "USER_MANAGEMENT"); 
        contentPanel.add(new ScreenAdminPanel(mainFrame), "SCREENS");


        add(contentPanel, BorderLayout.CENTER);
        contentLayout.show(contentPanel, "HOME");
    }

    // 새 메뉴 버튼 스타일 (다크 테마)
    private JButton createMenuButton(String text, String cardName, String icon) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setFont(UIUtils.FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(38, 43, 64)); 
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // ★★★ 수정: 버튼 내부 패딩 조정 및 정렬 설정으로 전체 폭 차지 ★★★
        btn.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); // BoxLayout.Y_AXIS에서 최대 폭을 차지하도록 설정
        
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // 호버 및 클릭 시 배경색 변경
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(55, 62, 88));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getClientProperty("selected").equals(true)) {
                    btn.setBackground(new Color(38, 43, 64));
                }
            }
        });
        
        btn.putClientProperty("selected", false); 

        btn.addActionListener(e -> {
            // 모든 버튼 선택 해제
            for (Component comp : btn.getParent().getComponents()) {
                if (comp instanceof JButton) {
                    ((JButton)comp).putClientProperty("selected", false);
                    ((JButton)comp).setBackground(new Color(38, 43, 64));
                }
            }
            // 현재 버튼 선택
            btn.putClientProperty("selected", true);
            btn.setBackground(UIUtils.COLOR_ACCENT); 
            
            // 콘텐츠 전환
            contentLayout.show(contentPanel, cardName);
            currentPanelTitle.setText(text);
            
            // 탭 이동 시 데이터 새로고침
            for (Component comp : contentPanel.getComponents()) {
                if (comp.isVisible()) {
                    if (cardName.equals("HOME") && comp instanceof AdminHomePanel) ((AdminHomePanel) comp).refreshData();
                    else if (cardName.equals("MOVIES")) refreshMovieTable();
                    else if (cardName.equals("SALES_ANALYTICS") && comp instanceof SalesAnalysisPanel) ((SalesAnalysisPanel) comp).refreshData(); 
                    else if (cardName.equals("USER_MANAGEMENT") && comp instanceof UserManagementPanel) ((UserManagementPanel) comp).loadData(); 
                    else if (cardName.equals("SCREENS") && comp instanceof ScreenAdminPanel) ((ScreenAdminPanel) comp).loadData();
                    else if (cardName.equals("SCHEDULES") && comp instanceof ScheduleAdminPanel) {
                        ScheduleAdminPanel p = (ScheduleAdminPanel) comp;
                        p.loadMoviesToCombo(); p.loadSchedules(null); p.loadScreens();
                    } else if (cardName.equals("SNACKS") && comp instanceof SnackAdminPanel) ((SnackAdminPanel) comp).loadData();
                }
            }
        });
        
        // 초기 대시보드를 선택된 상태로 설정
        if(cardName.equals("HOME")) {
            btn.putClientProperty("selected", true);
            btn.setBackground(UIUtils.COLOR_ACCENT); 
        }

        return btn;
    }
    
    // 임시 플레이스 홀더 패널 (기존과 동일)
    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BG_MAIN);
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_TITLE);
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }
    
    // 영화 관리 패널 (기존과 동일)
    private JPanel createMovieMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.BG_MAIN);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 상단: 검색 및 등록 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIUtils.BG_MAIN);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JTextField searchField = UIUtils.createTextField(20);
        searchField.setText("영화 제목 검색...");
        
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBox.setBackground(UIUtils.BG_MAIN);
        searchBox.add(searchField);
        
        JButton filterBtn = UIUtils.createOutlineButton("필터");
        JButton addMovieBtn = UIUtils.createStyledButton("+ 새 영화 등록");
        
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBox.setBackground(UIUtils.BG_MAIN);
        btnBox.add(filterBtn);
        btnBox.add(addMovieBtn);
        
        topPanel.add(searchBox, BorderLayout.WEST);
        topPanel.add(btnBox, BorderLayout.EAST);
        
        addMovieBtn.addActionListener(e -> showMovieDialog(null));


        String[] columns = {"ID", "제목", "장르", "러닝타임", "포스터경로"}; // ID, 경로 숨김 처리됨
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        movieTable = new JTable(tableModel);
        movieTable.setRowHeight(40);
        movieTable.setFont(UIUtils.FONT_MAIN);
        movieTable.getTableHeader().setFont(UIUtils.FONT_BTN);
        
        movieTable.getColumnModel().getColumn(0).setMinWidth(0); movieTable.getColumnModel().getColumn(0).setMaxWidth(0);
        movieTable.getColumnModel().getColumn(4).setMinWidth(0); movieTable.getColumnModel().getColumn(4).setMaxWidth(0);
        
        // UI 디자인에 맞게 테이블 컬럼 추가/삭제
        String[] displayColumns = {"포스터 / 제목", "장르", "등급", "러닝타임", "예매율", "상태", "관리"};
        DefaultTableModel visualModel = new DefaultTableModel(displayColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable visualTable = new JTable(visualModel); 

        JScrollPane scrollPane = new JScrollPane(movieTable);
        scrollPane.getViewport().setBackground(Color.WHITE); 
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));

        panel.add(topPanel, BorderLayout.NORTH); 
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshMovieTable();
        return panel;
    }

    private void showMovieDialog(Movie movieToEdit) {
        // 기존 showMovieDialog 로직 유지
        boolean isEdit = (movieToEdit != null);
        JTextField titleField = UIUtils.createTextField(15);
        JTextField genreField = UIUtils.createTextField(15);
        JTextField timeField = UIUtils.createTextField(5);
        JTextField pathField = UIUtils.createTextField(15); pathField.setEditable(false);
        JButton fileBtn = new JButton("파일..");

        fileBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("./images"); 
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathField.setText(chooser.getSelectedFile().getPath());
            }
        });

        if (isEdit) {
            titleField.setText(movieToEdit.getTitle());
            genreField.setText(movieToEdit.getGenre());
            timeField.setText(String.valueOf(movieToEdit.getRunningTime()));
            pathField.setText(movieToEdit.getPosterPath());
        }

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.add(new JLabel("제목:")); inputPanel.add(titleField);
        inputPanel.add(new JLabel("장르:")); inputPanel.add(genreField);
        inputPanel.add(new JLabel("시간(분):")); inputPanel.add(timeField);
        inputPanel.add(new JLabel("포스터:")); 
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(pathField, BorderLayout.CENTER); filePanel.add(fileBtn, BorderLayout.EAST);
        inputPanel.add(filePanel);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                isEdit ? "영화 정보 수정" : "새 영화 등록", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (UIUtils.showConfirm(this, "저장하시겠습니까?") != JOptionPane.YES_OPTION) return;
            try {
                String title = titleField.getText();
                String genre = genreField.getText();
                int time = Integer.parseInt(timeField.getText());
                String path = pathField.getText();
                boolean success;
                if (isEdit) success = movieDAO.updateMovie(movieToEdit.getId(), title, genre, time, path);
                else success = movieDAO.addMovie(title, genre, time, path);
                if (success) { refreshMovieTable(); JOptionPane.showMessageDialog(this, "저장되었습니다."); }
                else { JOptionPane.showMessageDialog(this, "실패!"); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "입력 오류"); }
        }
    }
    
    // 테이블 새로고침 로직 유지
    private void refreshMovieTable() {
        tableModel.setRowCount(0);
        List<Movie> movies = movieDAO.getAllMovies();
        for (Movie m : movies) {
            tableModel.addRow(new Object[]{m.getId(), m.getTitle(), m.getGenre(), m.getRunningTime(), m.getPosterPath()});
        }
    }
}