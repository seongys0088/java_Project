package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.CinemaMain;
import cinema.dao.UserDAO;
import cinema.domain.User;
import cinema.util.UIUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginPanel extends JPanel {
    private CinemaMain mainFrame;
    private UserDAO userDAO;

    private JTextField idField;
    private JPasswordField pwField;
    private JButton loginBtn;

    public LoginPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.userDAO = new UserDAO();
        
        setLayout(new GridBagLayout());
        setBackground(UIUtils.BG_MAIN); 

        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 20),
            new EmptyBorder(30, 40, 40, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel title = new JLabel("CINEPRIME");
        title.setFont(UIUtils.FONT_TITLE);
        title.setForeground(UIUtils.COLOR_ACCENT);
        title.setHorizontalAlignment(SwingConstants.CENTER); // ★★★ [추가] 타이틀 가운데 정렬 ★★★
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginBox.add(title, gbc);

        JLabel idLabel = new JLabel("아이디:");
        idField = UIUtils.createTextField(20);
        
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 1;
        loginBox.add(idLabel, gbc);
        gbc.gridx = 1; 
        loginBox.add(idField, gbc);

        JLabel pwLabel = new JLabel("비밀번호:");
        pwField = new JPasswordField(20);
        pwField.setBorder(idField.getBorder());
        
        // FocusListener (클릭/포커스 시 초기화)
        pwField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                 pwField.setText("");
            }
        });
        
        gbc.gridy = 2; gbc.gridx = 0;
        loginBox.add(pwLabel, gbc);
        gbc.gridx = 1; 
        loginBox.add(pwField, gbc);
        
        JButton loginBtn = UIUtils.createStyledButton("로그인");
        loginBtn.setPreferredSize(new Dimension(0, 40));
        
        ActionListener loginAction = e -> performLogin();
        loginBtn.addActionListener(loginAction);
        pwField.addActionListener(loginAction);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 5, 10, 5);
        loginBox.add(loginBtn, gbc);
        
        // ★★★ [수정] 회원가입 권유 문구 스타일 ★★★
        JLabel signupLabel = new JLabel("<html><u>계정이 없으신가요? 회원가입</u></html>", SwingConstants.CENTER);
        signupLabel.setFont(UIUtils.FONT_MAIN);
        signupLabel.setForeground(UIUtils.COLOR_TEXT_GRAY); // ★ 회색 글씨로 변경
        signupLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { mainFrame.showCard("SIGNUP"); }
        });

        gbc.gridy = 4; gbc.insets = new Insets(0, 5, 0, 5);
        loginBox.add(signupLabel, gbc);
        
        add(loginBox);
    }

    private void performLogin() {
        String id = idField.getText();
        String pw = new String(pwField.getPassword());
        
        User user = userDAO.login(id, pw);

        if (user != null) {
            // 정지된 사용자 체크
            if (user.isBanned()) {
                JOptionPane.showMessageDialog(this, "정지된 계정입니다. 관리자에게 문의하세요.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                pwField.setText(""); // 실패 시 초기화
                pwField.requestFocusInWindow();
                return;
            }
            
            mainFrame.handleLoginSuccess(user);
        } else {
            // 실패 시 비밀번호 초기화
            JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 올바르지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            pwField.setText("");
            pwField.requestFocusInWindow();
        }
    }
}