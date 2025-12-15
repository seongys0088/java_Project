package cinema;

import javax.swing.*;

import cinema.domain.User;
import cinema.ui.AdminMainPanel;
import cinema.ui.LoginPanel;
import cinema.ui.MyPagePanel;
import cinema.ui.RankingPanel;
import cinema.ui.ScheduleAdminPanel;
import cinema.ui.SignupPanel;
import cinema.ui.SnackShopPanel;
import cinema.ui.UserMainPanel;
import cinema.util.UIUtils;

import java.awt.*;

public class CinemaMain extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private User currentUser;
    
    // ★ 장바구니 카운트 업데이트를 위해 UserMainPanel 참조를 필드로 유지
    private UserMainPanel userMainPanel;

    public CinemaMain() {
        setTitle("시네마 관리 시스템 v1.0");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(UIUtils.BG_MAIN); 

        // 패널 등록 시 userMainPanel 참조 저장 및 추가
        userMainPanel = new UserMainPanel(this); 
        
        mainContainer.add(new LoginPanel(this), "LOGIN");
        mainContainer.add(new SignupPanel(this), "SIGNUP");
        mainContainer.add(userMainPanel, "USER_MAIN"); 
        mainContainer.add(new AdminMainPanel(this), "ADMIN_MAIN");
        mainContainer.add(new MyPagePanel(this), "MY_PAGE");
        mainContainer.add(new ScheduleAdminPanel(this), "SCHEDULES");
        mainContainer.add(new SnackShopPanel(this), "SNACK_SHOP");
        // ★★★ 수정: 예매 순위 페이지 (RankingPanel) 등록 ★★★
        mainContainer.add(new RankingPanel(this), "RANKINGS"); 

        add(mainContainer);
        showCard("LOGIN");
    }
    
    // ★ [신규] 장바구니 카운트 업데이트 메서드 (UserMainPanel과 SnackShopPanel 연결용)
    public void updateGlobalCartCount(int count) {
        if (userMainPanel != null) {
            userMainPanel.updateCartCount(count);
        }
    }
    
    // ... 나머지 메서드는 기존과 동일 (showCard, handleLoginSuccess, getPanel, main)
    public void showCard(String cardName) { cardLayout.show(mainContainer, cardName); }
    public void handleLoginSuccess(User user) {
        this.currentUser = user;
        if (user.isAdmin()) showCard("ADMIN_MAIN"); else showCard("USER_MAIN");
    }
    public User getCurrentUser() { return currentUser; }
    public Component getPanel(String name) {
        for (Component comp : mainContainer.getComponents()) {
            if (name.equals("MY_PAGE") && comp instanceof MyPagePanel) return comp;
        }
        return null;
    }
    public static void main(String[] args) {
        UIUtils.initCustomUI();
        SwingUtilities.invokeLater(() -> new CinemaMain().setVisible(true));
    }
}