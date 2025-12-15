package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.SnackDAO;
import cinema.domain.Snack;
import cinema.util.UIUtils;

import java.awt.*;
import java.util.List;

public class SnackAdminPanel extends JPanel {
    private SnackDAO snackDAO;
    private JTextField nameField, priceField, pathField;
    private JComboBox<String> categoryCombo;
    private JCheckBox soldOutCheck;
    private DefaultTableModel model;
    private JTable table;

    public SnackAdminPanel(CinemaMain mainFrame) {
        snackDAO = new SnackDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN); // 배경색 통일

        JLabel title = new JLabel("🍿 매점 메뉴 관리");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_TEXT);
        title.setBorder(new EmptyBorder(10, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        // 입력 폼 디자인
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(UIUtils.BG_MAIN);
        inputContainer.setBorder(new EmptyBorder(0, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIUtils.BG_MAIN); 
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        nameField = UIUtils.createTextField(15);
        priceField = UIUtils.createTextField(10);
        pathField = UIUtils.createTextField(15); pathField.setEditable(false);
        String[] cats = {"팝콘", "음료", "스낵", "세트"};
        categoryCombo = new JComboBox<>(cats);
        categoryCombo.setBackground(Color.WHITE);
        
        soldOutCheck = new JCheckBox("품절(Sold Out)");
        soldOutCheck.setForeground(UIUtils.COLOR_TEXT);
        soldOutCheck.setBackground(UIUtils.BG_MAIN);
        soldOutCheck.setFocusPainted(false);
        
        JButton fileBtn = UIUtils.createOutlineButton("파일");
        fileBtn.addActionListener(e -> {
            JFileChooser ch = new JFileChooser("./images");
            if(ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
                pathField.setText(ch.getSelectedFile().getPath());
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; formPanel.add(createLabel("메뉴명:"), gbc);
        gbc.gridx=1; gbc.gridy=0; formPanel.add(nameField, gbc);
        gbc.gridx=2; gbc.gridy=0; formPanel.add(createLabel("가격:"), gbc);
        gbc.gridx=3; gbc.gridy=0; formPanel.add(priceField, gbc);

        gbc.gridx=0; gbc.gridy=1; formPanel.add(createLabel("분류:"), gbc);
        gbc.gridx=1; gbc.gridy=1; formPanel.add(categoryCombo, gbc);
        gbc.gridx=2; gbc.gridy=1; formPanel.add(createLabel("상태:"), gbc);
        gbc.gridx=3; gbc.gridy=1; formPanel.add(soldOutCheck, gbc);

        gbc.gridx=0; gbc.gridy=2; formPanel.add(createLabel("이미지:"), gbc);
        gbc.gridx=1; gbc.gridy=2; gbc.gridwidth=2; 
        formPanel.add(pathField, gbc);
        gbc.gridx=3; gbc.gridy=2; gbc.gridwidth=1;
        formPanel.add(fileBtn, gbc);

        // 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(UIUtils.BG_MAIN);
        
        JButton addBtn = UIUtils.createStyledButton("추가");
        JButton editBtn = UIUtils.createStyledButton("수정");
        JButton delBtn = UIUtils.createStyledButton("삭제");
        JButton cancelBtn = UIUtils.createOutlineButton("입력 취소");

        btnPanel.add(addBtn); 
        btnPanel.add(editBtn); 
        btnPanel.add(delBtn);
        btnPanel.add(cancelBtn);

        inputContainer.add(formPanel, BorderLayout.CENTER);
        inputContainer.add(btnPanel, BorderLayout.SOUTH);
        
        add(inputContainer, BorderLayout.NORTH); 

        // 테이블
        String[] cols = {"ID", "분류", "메뉴명", "가격", "품절여부", "경로"};
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
        scroll.getViewport().setBackground(Color.WHITE); // 테이블 배경 흰색
        scroll.setBorder(new EmptyBorder(0, 20, 20, 20));

        add(scroll, BorderLayout.CENTER);

        // 이벤트 연결
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) {
                categoryCombo.setSelectedItem(model.getValueAt(row, 1).toString());
                nameField.setText(model.getValueAt(row, 2).toString());
                priceField.setText(model.getValueAt(row, 3).toString());
                Object soldVal = model.getValueAt(row, 4);
                if (soldVal instanceof Boolean) soldOutCheck.setSelected((Boolean)soldVal);
                else soldOutCheck.setSelected("Y".equals(soldVal));
                
                Object pathVal = model.getValueAt(row, 5);
                pathField.setText(pathVal != null ? pathVal.toString() : "");
            }
        });

        addBtn.addActionListener(e -> {
            String name = nameField.getText();
            if(UIUtils.showConfirm(this, "[" + name + "] 메뉴를 추가하시겠습니까?") == JOptionPane.YES_OPTION) processSave(false);
        });

        editBtn.addActionListener(e -> {
            if(table.getSelectedRow() == -1) { JOptionPane.showMessageDialog(this, "선택해주세요."); return; }
            String name = model.getValueAt(table.getSelectedRow(), 2).toString();
            if(UIUtils.showConfirm(this, "[" + name + "] 정보를 수정하시겠습니까?") == JOptionPane.YES_OPTION) processSave(true);
        });

        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "삭제할 메뉴를 선택하세요."); return; }
            String name = model.getValueAt(row, 2).toString();
            if(UIUtils.showConfirm(this, "[" + name + "] 메뉴를 정말 삭제하시겠습니까?") == JOptionPane.YES_OPTION) {
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                if(snackDAO.deleteSnack(id)) {
                    JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                    loadData();
                    clearFields();
                }
            }
        });

        cancelBtn.addActionListener(e -> clearFields());
        loadData();
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UIUtils.COLOR_TEXT);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private void processSave(boolean isEdit) {
        try {
            String name = nameField.getText();
            if(name.isEmpty()) { JOptionPane.showMessageDialog(this, "이름을 입력하세요."); return; }
            int price = Integer.parseInt(priceField.getText());
            String cat = (String) categoryCombo.getSelectedItem();
            String path = pathField.getText();
            boolean isSold = soldOutCheck.isSelected();

            boolean success;
            if(isEdit) {
                int id = Integer.parseInt(model.getValueAt(table.getSelectedRow(), 0).toString());
                success = snackDAO.updateSnack(id, name, price, cat, path, isSold);
            } else {
                success = snackDAO.addSnack(name, price, cat, path, isSold);
            }

            if(success) {
                JOptionPane.showMessageDialog(this, "완료되었습니다.");
                loadData();
                clearFields();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "가격은 숫자여야 합니다.");
        }
    }

    private void clearFields() {
        nameField.setText(""); priceField.setText(""); pathField.setText("");
        soldOutCheck.setSelected(false);
        table.clearSelection();
    }

    public void loadData() {
        model.setRowCount(0);
        List<Snack> list = snackDAO.getAllSnacks();
        for(Snack s : list) {
            model.addRow(new Object[]{s.getId(), s.getCategory(), s.getName(), s.getPrice(), s.isSoldOut(), s.getImagePath()});
        }
    }
}