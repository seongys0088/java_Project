package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.MovieDAO;
import cinema.dao.ScheduleDAO;
import cinema.dao.ScreenDAO;
import cinema.domain.Movie;
import cinema.domain.Screen;
import cinema.util.DBUtil;
import cinema.util.UIUtils;

import java.awt.*;
import java.sql.*;
import java.util.List;

public class ScheduleAdminPanel extends JPanel {
    private CinemaMain mainFrame;
    private JComboBox<String> movieCombo, screenCombo;
    private JTextField dateField, timeField;
    private JTextField searchDateField;
    private JTable table;
    private DefaultTableModel model;
    
    private List<Movie> movieList;

    public ScheduleAdminPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN); // 배경 밝게

        JLabel title = new JLabel("📅 상영 스케줄 관리");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT); // 텍스트 어둡게
        title.setBorder(new EmptyBorder(10, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        // 검색 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIUtils.BG_MAIN);
        searchPanel.setBorder(new EmptyBorder(0, 0, 10, 20));
        
        searchDateField = UIUtils.createTextField(10);
        JButton searchBtn = UIUtils.createOutlineButton("날짜 검색");
        JButton resetSearchBtn = UIUtils.createOutlineButton("전체 보기");
        
        searchPanel.add(new JLabel("날짜(YYYY-MM-DD): ") {{setForeground(UIUtils.COLOR_TEXT);}});
        searchPanel.add(searchDateField);
        searchPanel.add(searchBtn);
        searchPanel.add(resetSearchBtn);

        // 입력 폼
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        inputPanel.setBackground(UIUtils.BG_MAIN);
        
        movieCombo = new JComboBox<>();
        movieCombo.setBackground(Color.WHITE);
        screenCombo = new JComboBox<>();
        screenCombo.setBackground(Color.WHITE);
        
        dateField = UIUtils.createTextField(8); 
        timeField = UIUtils.createTextField(5); 
        
        inputPanel.add(new JLabel("영화:") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(movieCombo);
        inputPanel.add(new JLabel("상영관:") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(screenCombo);
        inputPanel.add(new JLabel("날짜:") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(dateField);
        inputPanel.add(new JLabel("시간:") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(timeField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIUtils.BG_MAIN);
        JButton addBtn = UIUtils.createStyledButton("등록");
        JButton editBtn = UIUtils.createStyledButton("수정");
        JButton delBtn = UIUtils.createStyledButton("삭제");
        JButton cancelBtn = UIUtils.createOutlineButton("취소");
        
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(delBtn); btnPanel.add(cancelBtn);
        
        JPanel centerTop = new JPanel(new BorderLayout());
        centerTop.setBackground(UIUtils.BG_MAIN);
        centerTop.add(searchPanel, BorderLayout.NORTH);
        centerTop.add(inputPanel, BorderLayout.CENTER);
        centerTop.add(btnPanel, BorderLayout.SOUTH);
        
        add(centerTop, BorderLayout.CENTER);

        // 테이블
        String[] cols = {"ID", "영화제목", "상영관", "날짜", "시간", "영화ID"}; 
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(UIUtils.FONT_MAIN);
        table.getTableHeader().setFont(UIUtils.FONT_BTN);
        
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setMinWidth(0); table.getColumnModel().getColumn(5).setMaxWidth(0);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE); // 테이블 흰색
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));
        
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(centerTop, BorderLayout.NORTH);
        mainContent.add(scroll, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // 이벤트 연결
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                movieCombo.setSelectedItem(model.getValueAt(row, 1).toString());
                screenCombo.setSelectedItem(model.getValueAt(row, 2).toString());
                dateField.setText(model.getValueAt(row, 3).toString());
                timeField.setText(model.getValueAt(row, 4).toString());
            }
        });

        searchBtn.addActionListener(e -> loadSchedules(searchDateField.getText()));
        resetSearchBtn.addActionListener(e -> { searchDateField.setText(""); loadSchedules(null); });

        addBtn.addActionListener(e -> {
            String m = (String) movieCombo.getSelectedItem();
            if(UIUtils.showConfirm(this, "[" + m + "] 스케줄을 등록하시겠습니까?") == JOptionPane.YES_OPTION) processSave(false);
        });
        
        editBtn.addActionListener(e -> {
            if(table.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "선택하세요."); return; }
            if(UIUtils.showConfirm(this, "수정하시겠습니까?") == JOptionPane.YES_OPTION) processSave(true);
        });
        
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "선택하세요."); return; }
            if(UIUtils.showConfirm(this, "정말 삭제하시겠습니까?") == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                new ScheduleDAO().deleteSchedule(id);
                loadSchedules(null);
                clearFields();
            }
        });
        
        cancelBtn.addActionListener(e -> clearFields());

        loadMoviesToCombo();
        loadScreens();
        loadSchedules(null);
    }

    private void processSave(boolean isEdit) {
        if(movieCombo.getSelectedIndex() == -1 || screenCombo.getSelectedIndex() == -1) return;
        String selectedTitle = (String) movieCombo.getSelectedItem();
        int movieId = -1;
        for(Movie m : movieList) { if(m.getTitle().equals(selectedTitle)) { movieId = m.getId(); break; } }
        
        String screen = (String) screenCombo.getSelectedItem();
        String date = dateField.getText();
        String time = timeField.getText();

        if(date.isEmpty() || time.isEmpty()) { JOptionPane.showMessageDialog(this, "정보 입력 필요"); return; }

        ScheduleDAO dao = new ScheduleDAO();
        boolean success;
        if(isEdit) {
            int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
            success = dao.updateSchedule(id, movieId, screen, date, time);
        } else success = dao.addSchedule(movieId, screen, date, time);

        if(success) { JOptionPane.showMessageDialog(this, "완료"); loadSchedules(null); clearFields(); } 
        else JOptionPane.showMessageDialog(this, "실패");
    }

    private void clearFields() {
        dateField.setText(""); timeField.setText("");
        if(movieCombo.getItemCount() > 0) movieCombo.setSelectedIndex(0);
        if(screenCombo.getItemCount() > 0) screenCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    public void loadMoviesToCombo() {
        movieCombo.removeAllItems();
        MovieDAO movieDAO = new MovieDAO();
        movieList = movieDAO.getAllMovies();
        for (Movie m : movieList) movieCombo.addItem(m.getTitle());
    }

    public void loadScreens() {
        screenCombo.removeAllItems();
        ScreenDAO sDao = new ScreenDAO();
        List<Screen> list = sDao.getAllScreens();
        for(Screen s : list) screenCombo.addItem(s.getName());
    }

    public void loadSchedules(String dateQuery) {
        model.setRowCount(0);
        String sql = "SELECT s.schedule_id, m.title, s.screen_name, s.show_date, s.start_time, m.movie_id " +
                     "FROM schedules s JOIN movies m ON s.movie_id = m.movie_id ";
        if(dateQuery != null && !dateQuery.trim().isEmpty()) sql += " WHERE s.show_date LIKE ? ";
        sql += " ORDER BY s.show_date DESC, s.start_time ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if(dateQuery != null && !dateQuery.trim().isEmpty()) pstmt.setString(1, "%" + dateQuery + "%");
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("schedule_id"), rs.getString("title"), rs.getString("screen_name"),
                    rs.getString("show_date"), rs.getString("start_time"), rs.getInt("movie_id")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}