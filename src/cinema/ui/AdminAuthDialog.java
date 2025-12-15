package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.dao.UserDAO;
import cinema.domain.User;
import cinema.util.UIUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminAuthDialog extends JDialog {
    private JPasswordField pwField;
    private UserDAO userDAO;
    private User currentUser;
    private boolean authenticated = false;

    public AdminAuthDialog(JFrame parent, User user, UserDAO dao) {
        super(parent, "관리자 모드 재인증", true); // Modal
        this.currentUser = user;
        this.userDAO = dao;
        
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.BG_MAIN);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIUtils.BG_MAIN);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel message = new JLabel("보안 강화를 위해 비밀번호를 다시 입력하세요.");
        message.setFont(UIUtils.FONT_MAIN);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(message, gbc);

        JLabel pwLabel = new JLabel("비밀번호:");
        pwField = new JPasswordField(15);
        pwField.setFont(UIUtils.FONT_MAIN);
        pwField.setBorder(UIUtils.createTextField(15).getBorder()); 
        
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 1;
        panel.add(pwLabel, gbc);
        gbc.gridx = 1; 
        panel.add(pwField, gbc);

        JButton confirmBtn = UIUtils.createStyledButton("인증");
        confirmBtn.setPreferredSize(new Dimension(0, 30));
        
        ActionListener authListener = e -> attemptAuthentication();
        confirmBtn.addActionListener(authListener);
        pwField.addActionListener(authListener); // Enter key support

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(confirmBtn, gbc);
        
        add(panel, BorderLayout.CENTER);
    }
    
    private void attemptAuthentication() {
        String enteredPw = new String(pwField.getPassword());
        if (enteredPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "비밀번호를 입력해주세요.");
            return;
        }

        // UserDAO.java에 verifyPassword 메서드가 정상적으로 존재하면 오류가 없어야 합니다.
        if (userDAO.verifyPassword(currentUser.getId(), enteredPw)) {
            authenticated = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            pwField.setText("");
            pwField.requestFocusInWindow();
        }
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
}