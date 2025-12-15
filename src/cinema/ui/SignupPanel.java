package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.CinemaMain;
import cinema.dao.UserDAO;
import cinema.util.UIUtils;

import java.awt.*;

public class SignupPanel extends JPanel {
    private CinemaMain mainFrame;
    private UserDAO userDAO;

    public SignupPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        
        setLayout(new GridBagLayout());
        setBackground(UIUtils.BG_MAIN); // ★ 수정

        JPanel signupBox = new JPanel(new GridBagLayout());
        signupBox.setBackground(UIUtils.BG_CARD); // ★ 수정
        signupBox.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 20),
                new EmptyBorder(30, 50, 30, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("회원가입");
        title.setFont(UIUtils.FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        signupBox.add(title, gbc);

        JTextField idField = UIUtils.createTextField(15);
        JTextField pwField = UIUtils.createTextField(15);
        JTextField nameField = UIUtils.createTextField(15);
        JTextField ageField = UIUtils.createTextField(5);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addFormField(signupBox, "아이디", idField, 1, gbc);
        addFormField(signupBox, "비밀번호", pwField, 2, gbc);
        addFormField(signupBox, "이름", nameField, 3, gbc);
        addFormField(signupBox, "나이", ageField, 4, gbc);

        JCheckBox agreeCheck = new JCheckBox("개인정보 수집 이용 동의");
        agreeCheck.setBackground(UIUtils.BG_CARD);
        agreeCheck.setFocusPainted(false);
        
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 15, 10);
        signupBox.add(agreeCheck, gbc);

        JButton joinBtn = UIUtils.createStyledButton("가입완료");
        JButton backBtn = UIUtils.createOutlineButton("뒤로가기"); // ★ 스타일 변경

        gbc.gridy = 6; gbc.insets = new Insets(5, 10, 5, 10);
        signupBox.add(joinBtn, gbc);
        gbc.gridy = 7;
        signupBox.add(backBtn, gbc);

        add(signupBox);
        
        backBtn.addActionListener(e -> mainFrame.showCard("LOGIN"));
        joinBtn.addActionListener(e -> {
            // (기존 가입 로직 동일)
            String id = idField.getText().trim();
            String pw = pwField.getText().trim();
            String name = nameField.getText().trim();
            String ageStr = ageField.getText().trim();
            if(id.isEmpty() || pw.isEmpty() || name.isEmpty() || ageStr.isEmpty()) return;
            if(!agreeCheck.isSelected()) return;
            try {
                if(userDAO.addUser(id, pw, name, Integer.parseInt(ageStr))) {
                    JOptionPane.showMessageDialog(this, "가입 성공!");
                    mainFrame.showCard("LOGIN");
                }
            } catch(Exception ex) {}
        });
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, int y, GridBagConstraints gbc) {
        gbc.gridy = y;
        gbc.gridx = 0; gbc.weightx = 0.3;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
}