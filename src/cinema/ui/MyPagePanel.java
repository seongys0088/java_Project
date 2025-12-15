package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.ReservationDAO;
import cinema.util.UIUtils;
import cinema.domain.User;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

public class MyPagePanel extends JPanel {
    private CinemaMain mainFrame;
    private ReservationDAO resDAO;
    private DefaultTableModel tableModel;
    private JTable table;

    public MyPagePanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.resDAO = new ReservationDAO();

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // 1. 타이틀
        JLabel title = new JLabel("👤 마이 페이지 (예매 내역)");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // 2. 테이블 (상태 컬럼 추가)
        String[] columns = {"예약번호", "영화 제목", "상영 시간", "좌석", "상태"}; // ★ 상태 추가
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(35); // 행 높이 여유 있게
        table.setFont(UIUtils.FONT_MAIN);
        table.getTableHeader().setFont(UIUtils.FONT_BTN);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        
        add(scrollPane, BorderLayout.CENTER);

        // 3. 버튼들
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIUtils.BG_MAIN);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton cancelBtn = UIUtils.createStyledButton("예매 취소");
        JButton backBtn = UIUtils.createOutlineButton("뒤로가기");

        btnPanel.add(cancelBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> mainFrame.showCard("USER_MAIN"));

        // [취소 버튼 로직 수정]
        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "취소할 내역을 선택하세요."); return; }
            
            // ★ 상태 확인: '관람 완료'면 취소 불가
            String status = (String) tableModel.getValueAt(row, 4);
            if ("관람 완료".equals(status)) {
                JOptionPane.showMessageDialog(this, "이미 상영이 종료된 영화는 취소할 수 없습니다.", "취소 불가", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (UIUtils.showConfirm(this, "정말 예매를 취소하시겠습니까?") == JOptionPane.YES_OPTION) {
                String resId = (String) tableModel.getValueAt(row, 0);
                if (resDAO.cancelReservation(Integer.parseInt(resId))) {
                    JOptionPane.showMessageDialog(this, "취소되었습니다.");
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "취소 실패");
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { refreshData(); }
        });
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        User user = mainFrame.getCurrentUser();
        if (user == null) return;

        Vector<Vector<String>> data = resDAO.getMyReservations(user.getId());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Vector<String> row : data) {
            // row: [0]ID, [1]제목, [2]시간(문자열), [3]좌석
            String timeStr = row.get(2); // "2024-06-15 14:00" 형식 가정
            String status = "예매 완료";

            try {
                // 시간 비교 로직
                LocalDateTime showTime = LocalDateTime.parse(timeStr, formatter);
                if (showTime.isBefore(now)) {
                    status = "관람 완료";
                }
            } catch (Exception e) {
                // 포맷이 안 맞을 경우 예외 처리 (그냥 둠)
            }
            
            row.add(status); // 상태 컬럼 추가
            tableModel.addRow(row);
        }
    }
}