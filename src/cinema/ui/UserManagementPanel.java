package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import cinema.CinemaMain;
import cinema.dao.UserDAO;
import cinema.util.UIUtils;

import java.awt.*;
import java.util.Vector;

public class UserManagementPanel extends JPanel {
    private UserDAO userDAO;
    private DefaultTableModel tableModel;
    private JTable userTable;

    public UserManagementPanel(CinemaMain mainFrame) {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);
        setBorder(new EmptyBorder(30, 40, 30, 40)); 

        JLabel title = new JLabel("👥 회원 관리");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // 1. 검색 및 필터 영역
        JPanel topControls = new JPanel(new BorderLayout(10, 0));
        topControls.setBackground(UIUtils.BG_MAIN);
        
        JTextField searchField = UIUtils.createTextField(25);
        searchField.setText("이름 또는 아이디 검색...");
        searchField.setForeground(Color.GRAY);
        
        JButton searchBtn = UIUtils.createOutlineButton("검색");
        JButton filterBtn = UIUtils.createOutlineButton("정지 회원 보기");
        
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBox.setBackground(UIUtils.BG_MAIN);
        searchBox.add(searchField);
        searchBox.add(searchBtn);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBox.setBackground(UIUtils.BG_MAIN);
        btnBox.add(filterBtn);
        
        topControls.add(searchBox, BorderLayout.WEST);
        topControls.add(btnBox, BorderLayout.EAST);
        topControls.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // 2. 테이블
        String[] columns = {"ID", "이름", "나이", "권한", "가입일", "상태", "관리"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { 
                return column == 6; 
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? JButton.class : String.class;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setRowHeight(35); 
        userTable.setFont(UIUtils.FONT_MAIN);
        userTable.getTableHeader().setFont(UIUtils.FONT_BTN);
        
        // 버튼 컬럼 렌더러/에디터 설정
        userTable.getColumn("관리").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("관리").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIUtils.BG_MAIN);
        centerPanel.add(topControls, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        Vector<Vector<String>> userData = userDAO.getAllUsers();
        
        for (Vector<String> row : userData) {
            Vector<Object> rowData = new Vector<>(row);
            rowData.add("상태 변경");
            tableModel.addRow(rowData);
        }
    }
    
    // 버튼 렌더러 클래스
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(UIUtils.FONT_MAIN);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getValueAt(row, 5);
            if (status.equals("활성")) {
                setText("정지");
                setBackground(new Color(255, 100, 100)); // 빨간색 계열
                setForeground(Color.WHITE);
            } else {
                setText("활성 해제");
                setBackground(new Color(100, 255, 100)); // 초록색 계열
                setForeground(UIUtils.COLOR_TEXT);
            }
            return this;
        }
    }

    // 버튼 에디터 클래스 (클릭 이벤트 처리)
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) { 
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(UIUtils.FONT_MAIN);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "상태 변경" : value.toString();
            String status = (String) table.getValueAt(row, 5);
            
            if (status.equals("활성")) {
                button.setText("정지");
                button.setBackground(new Color(255, 100, 100));
            } else {
                button.setText("활성 해제");
                button.setBackground(new Color(100, 255, 100));
            }
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = userTable.getSelectedRow();
                String userId = (String) userTable.getValueAt(row, 0);
                String currentStatus = (String) userTable.getValueAt(row, 5);
                String newStatus = currentStatus.equals("활성") ? "정지" : "활성";
                
                if (UIUtils.showConfirm(UserManagementPanel.this, "사용자 [" + userId + "]의 상태를 [" + newStatus + "]로 변경하시겠습니까?") == JOptionPane.YES_OPTION) {
                    
                    // ★★★ [수정] 정지 상태를 BANNED 역할로 DB에 반영 ★★★
                    String newRole = newStatus.equals("정지") ? "BANNED" : "USER"; 
                    
                    if (userDAO.updateUserRole(userId, newRole)) {
                        userTable.setValueAt(newStatus, row, 5); // UI 업데이트
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "상태가 " + newStatus + "로 변경되었습니다.");
                        loadData(); // 데이터 새로고침
                    } else {
                        JOptionPane.showMessageDialog(UserManagementPanel.this, "상태 변경 실패 (DB 오류)", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            isPushed = false;
            return label;
        }
    }
}