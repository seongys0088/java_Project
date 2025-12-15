package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.ScreenDAO;
import cinema.domain.Screen;
import cinema.util.UIUtils;

import java.awt.*;
import java.util.List;

public class ScreenAdminPanel extends JPanel {
    private ScreenDAO screenDAO;
    private JTextField nameField, rowField, colField;
    private DefaultTableModel model;
    private JTable table;

    public ScreenAdminPanel(CinemaMain mainFrame) {
        screenDAO = new ScreenDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN); // 배경 밝게

        JLabel title = new JLabel("🏢 상영관 관리");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT);
        title.setBorder(new EmptyBorder(10, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        // 입력 폼
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        inputPanel.setBackground(UIUtils.BG_MAIN);
        inputPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        nameField = UIUtils.createTextField(8);
        rowField = UIUtils.createTextField(4);
        colField = UIUtils.createTextField(4);
        
        inputPanel.add(new JLabel("상영관명:") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(nameField);
        inputPanel.add(new JLabel("행(Row):") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(rowField);
        inputPanel.add(new JLabel("열(Col):") {{setForeground(UIUtils.COLOR_TEXT);}}); inputPanel.add(colField);

        // 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIUtils.BG_MAIN);
        
        JButton addBtn = UIUtils.createStyledButton("등록");
        JButton editBtn = UIUtils.createStyledButton("수정");
        JButton delBtn = UIUtils.createStyledButton("삭제");
        JButton cancelBtn = UIUtils.createOutlineButton("입력 취소"); // 아웃라인 버튼
        
        btnPanel.add(addBtn); 
        btnPanel.add(editBtn); 
        btnPanel.add(delBtn);
        btnPanel.add(cancelBtn);

        // 테이블
        String[] cols = {"ID", "상영관명", "좌석수", "행", "열"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(UIUtils.FONT_MAIN);
        table.getTableHeader().setFont(UIUtils.FONT_BTN);
        
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE); // 테이블 흰색
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // 이벤트
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                nameField.setText(model.getValueAt(row, 1).toString());
                rowField.setText(model.getValueAt(row, 3).toString());
                colField.setText(model.getValueAt(row, 4).toString());
            }
        });

        addBtn.addActionListener(e -> {
            String name = nameField.getText();
            if(name.isEmpty()) { JOptionPane.showMessageDialog(this, "상영관명을 입력하세요."); return; }
            if(UIUtils.showConfirm(this, "[" + name + "] 상영관을 등록하시겠습니까?") == JOptionPane.YES_OPTION) processSave(false);
        });

        editBtn.addActionListener(e -> {
            if(table.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "선택하세요."); return; }
            String oldName = model.getValueAt(table.getSelectedRow(), 1).toString();
            if(UIUtils.showConfirm(this, "[" + oldName + "] 정보를 수정하시겠습니까?") == JOptionPane.YES_OPTION) processSave(true);
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "선택하세요."); return; }
            String name = model.getValueAt(row, 1).toString();
            if(UIUtils.showConfirm(this, "[" + name + "]을(를) 정말 삭제하시겠습니까?") == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                if(screenDAO.deleteScreen(id)) {
                    JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                    loadData();
                    clearFields();
                }
            }
        });
        
        cancelBtn.addActionListener(e -> clearFields());
        loadData();
    }

    private void processSave(boolean isEdit) {
        try {
            String name = nameField.getText();
            int r = Integer.parseInt(rowField.getText());
            int c = Integer.parseInt(colField.getText());
            boolean success;
            if(isEdit) {
                int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
                success = screenDAO.updateScreen(id, name, r, c);
            } else success = screenDAO.addScreen(name, r, c);

            if(success) { JOptionPane.showMessageDialog(this, "완료"); loadData(); clearFields(); } 
            else JOptionPane.showMessageDialog(this, "실패");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "행/열은 숫자여야 합니다."); }
    }

    private void clearFields() {
        nameField.setText(""); rowField.setText(""); colField.setText("");
        table.clearSelection();
    }

    public void loadData() {
        model.setRowCount(0);
        List<Screen> list = screenDAO.getAllScreens();
        for(Screen s : list) {
            model.addRow(new Object[]{s.getId(), s.getName(), (s.getTotalRow()*s.getTotalCol())+"석", s.getTotalRow(), s.getTotalCol()});
        }
    }
}