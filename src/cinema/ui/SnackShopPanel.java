package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder; 
import javax.swing.table.DefaultTableModel;

import cinema.CinemaMain;
import cinema.dao.SnackDAO;
import cinema.domain.User;
import cinema.domain.Snack;
import cinema.util.UIUtils;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SnackShopPanel extends JPanel {
    private CinemaMain mainFrame;
    private SnackDAO snackDAO;
    private JPanel menuGrid;
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private JLabel totalLabel;
    
    private ArrayList<Integer> cartPrices = new ArrayList<>();
    private int totalPrice = 0;

    public SnackShopPanel(CinemaMain mainFrame) {
        this.mainFrame = mainFrame;
        this.snackDAO = new SnackDAO();
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_MAIN);

        // 1. 헤더 (UserMainPanel 디자인과 유사하게 통일)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(15, 40, 15, 40)
        ));
        
        JLabel title = new JLabel("🍿 스낵바"); 
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(UIUtils.COLOR_ACCENT);
        
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBox.setOpaque(false);
        
        JButton historyBtn = createHeaderBtn("📜 주문 내역");
        JButton backBtn = createHeaderBtn("메인으로");
        
        rightBox.add(historyBtn);
        rightBox.add(backBtn);
        
        header.add(title, BorderLayout.WEST);
        header.add(rightBox, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. 메뉴 그리드 (기존과 동일)
        menuGrid = new JPanel(new GridLayout(0, 3, 20, 20));
        menuGrid.setBackground(UIUtils.BG_MAIN);
        menuGrid.setBorder(new EmptyBorder(20, 30, 20, 10)); 
        
        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UIUtils.BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        // 3. 장바구니 (새 디자인 반영)
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setPreferredSize(new Dimension(320, 0));
        cartPanel.setBackground(Color.WHITE); 
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel cartTitle = new JLabel("장바구니"); 
        cartTitle.setForeground(UIUtils.COLOR_TEXT);
        cartTitle.setFont(UIUtils.FONT_SUBTITLE);
        cartTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        cartList.setBackground(new Color(245, 245, 245));
        cartList.setForeground(UIUtils.COLOR_TEXT);
        cartList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        cartList.setBorder(new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 5));
        
        // 장바구니 비어있을 때 표시 로직
        JLabel emptyCartLabel = new JLabel("담긴 상품이 없습니다.", SwingConstants.CENTER);
        emptyCartLabel.setForeground(UIUtils.COLOR_TEXT_GRAY);
        emptyCartLabel.setFont(UIUtils.FONT_MAIN);
        
        JScrollPane cartScrollPane = new JScrollPane(cartList);
        cartScrollPane.getViewport().setBackground(Color.WHITE);
        cartScrollPane.setViewportView(cartList);
        
        // 모델 리스너 추가: 장바구니가 비어있을 때 안내 메시지 표시
        cartModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) { checkEmptyCart(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { checkEmptyCart(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { checkEmptyCart(); }
            private void checkEmptyCart() {
                if (cartModel.isEmpty()) {
                    cartScrollPane.setViewportView(emptyCartLabel);
                } else {
                    cartScrollPane.setViewportView(cartList);
                }
            }
        });
        if (cartModel.isEmpty()) cartScrollPane.setViewportView(emptyCartLabel);
        
        
        // ★★★ [신규] 메뉴 선택 삭제 버튼 ★★★
        JButton removeBtn = UIUtils.createOutlineButton("선택 삭제"); 
        removeBtn.setForeground(new Color(200, 50, 50));
        
        totalLabel = new JLabel("총 결제금액: 0원", SwingConstants.RIGHT); 
        totalLabel.setForeground(UIUtils.COLOR_ACCENT);
        totalLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        
        JButton buyBtn = UIUtils.createStyledButton("주문하기"); 
        buyBtn.setPreferredSize(new Dimension(100, 50));
        buyBtn.setBackground(new Color(230, 230, 230)); 
        buyBtn.setForeground(UIUtils.COLOR_TEXT);
        
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(Color.WHITE);
        JLabel totalTitle = new JLabel("총 결제금액");
        totalTitle.setForeground(UIUtils.COLOR_TEXT_GRAY);
        totalBox.add(totalTitle, BorderLayout.WEST);
        totalBox.add(totalLabel, BorderLayout.EAST);
        totalBox.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // ★ 삭제 버튼을 포함한 상단 컨트롤 패널
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topControls.setBackground(Color.WHITE);
        topControls.add(removeBtn);
        topControls.setBorder(new EmptyBorder(0, 0, 10, 0));


        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setBackground(Color.WHITE);
        
        // 삭제 버튼, 금액 정보, 주문 버튼을 순서대로 배치
        bottomBox.add(topControls, BorderLayout.NORTH);
        bottomBox.add(totalBox, BorderLayout.CENTER);
        bottomBox.add(buyBtn, BorderLayout.SOUTH);
        bottomBox.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        cartPanel.add(cartTitle, BorderLayout.NORTH);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER); 
        cartPanel.add(bottomBox, BorderLayout.SOUTH);
        add(cartPanel, BorderLayout.EAST);

        // 이벤트 연결 (기존 로직 유지 및 카운트 업데이트 추가)
        backBtn.addActionListener(e -> mainFrame.showCard("USER_MAIN"));
        historyBtn.addActionListener(e -> showOrderHistory());

        // ★★★ [핵심] 선택 삭제 버튼 액션 리스너 ★★★
        removeBtn.addActionListener(e -> {
            int idx = cartList.getSelectedIndex();
            if (idx != -1) {
                totalPrice -= cartPrices.get(idx);
                totalLabel.setText("총 결제금액: " + totalPrice + "원");
                cartModel.remove(idx);
                cartPrices.remove(idx);
                mainFrame.updateGlobalCartCount(cartModel.size()); // 전역 카운트 업데이트
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 항목을 선택해주세요.");
            }
        });

        buyBtn.addActionListener(e -> {
            if(totalPrice == 0) return;
            User user = mainFrame.getCurrentUser();
            if(user == null) { JOptionPane.showMessageDialog(this, "로그인 필요"); return; }

            if(UIUtils.showConfirm(this, "총 " + totalPrice + "원을 결제하시겠습니까?") == JOptionPane.YES_OPTION) {
                StringBuilder details = new StringBuilder();
                for(int i=0; i<cartModel.size(); i++) details.append(cartModel.get(i)).append(", ");
                
                int orderId = snackDAO.addOrder(user.getId(), details.toString(), totalPrice);
                
                if (orderId != -1) {
                    JOptionPane.showMessageDialog(this, "결제 완료!\n주문번호: [" + orderId + "] 번\n카운터에서 번호를 불러드립니다.");
                    cartModel.clear(); cartPrices.clear(); totalPrice = 0;
                    totalLabel.setText("총 결제금액: 0원");
                    mainFrame.updateGlobalCartCount(0); 
                } else {
                    JOptionPane.showMessageDialog(this, "주문 저장 실패");
                }
            }
        });

        loadMenu();
        
        // 패널이 보여질 때 카운트 업데이트
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                mainFrame.updateGlobalCartCount(cartModel.size());
            }
        });
        
        mainFrame.updateGlobalCartCount(cartModel.size()); 
    }

    private JButton createHeaderBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_MAIN);
        btn.setForeground(Color.GRAY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showOrderHistory() {
        User user = mainFrame.getCurrentUser();
        if(user == null) return;

        JDialog dialog = new JDialog((Frame)null, "내 주문 내역", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(UIUtils.BG_MAIN);
        
        String[] cols = {"주문번호", "날짜", "주문 상세", "금액"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(UIUtils.FONT_MAIN);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        Vector<Vector<String>> data = snackDAO.getOrderHistory(user.getId());
        for(Vector<String> row : data) model.addRow(row);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public void loadMenu() {
        menuGrid.removeAll();
        List<Snack> snacks = snackDAO.getAllSnacks();
        
        for (Snack s : snacks) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    new UIUtils.RoundedBorder(UIUtils.COLOR_BORDER, 15),
                    new EmptyBorder(15, 15, 15, 15)
            ));

            JLayeredPane layeredPane = new JLayeredPane();
            layeredPane.setPreferredSize(new Dimension(120, 120)); 

            JLabel imgLabel = new JLabel();
            imgLabel.setBounds(0, 0, 120, 120);
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imgLabel.setOpaque(true);
            imgLabel.setBackground(new Color(240, 240, 240));

            String path = s.getImagePath();
            boolean imgLoaded = false;
            if (path != null && !path.isEmpty()) {
                File imgFile = new File(path);
                if (imgFile.exists()) {
                    ImageIcon icon = new ImageIcon(path);
                    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(img));
                        imgLabel.setText(""); 
                        imgLoaded = true;
                    }
                }
            }
            if (!imgLoaded) { imgLabel.setText("NO IMAGE"); imgLabel.setForeground(Color.GRAY); }
            
            layeredPane.add(imgLabel, JLayeredPane.DEFAULT_LAYER);

            if(s.isSoldOut()) {
                JLabel soldOutLabel = new JLabel("SOLD OUT") {
                    @Override protected void paintComponent(Graphics g) {
                        g.setColor(getBackground()); g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                soldOutLabel.setBounds(0, 0, 120, 120);
                soldOutLabel.setHorizontalAlignment(SwingConstants.CENTER);
                soldOutLabel.setFont(new Font("Arial Black", Font.BOLD, 16));
                soldOutLabel.setForeground(Color.WHITE);
                soldOutLabel.setBackground(new Color(0, 0, 0, 150)); 
                soldOutLabel.setOpaque(false); 
                layeredPane.add(soldOutLabel, JLayeredPane.PALETTE_LAYER);
            }

            JPanel imageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            imageWrapper.setBackground(Color.WHITE);
            imageWrapper.add(layeredPane);

            JLabel name = new JLabel(s.getName() + " (" + s.getCategory() + ")");
            name.setForeground(UIUtils.COLOR_TEXT);
            name.setFont(new Font("맑은 고딕", Font.BOLD, 15));
            name.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel price = new JLabel(s.getPrice() + "원");
            price.setForeground(UIUtils.COLOR_TEXT_GRAY);
            price.setHorizontalAlignment(SwingConstants.CENTER);

            JButton addBtn = UIUtils.createOutlineButton("+ 담기"); 
            if(s.isSoldOut()) {
                addBtn.setText("품절");
                addBtn.setBackground(Color.GRAY);
                addBtn.setEnabled(false);
            } else {
                addBtn.addActionListener(e -> {
                    cartModel.addElement(s.getName());
                    cartPrices.add(s.getPrice());
                    totalPrice += s.getPrice();
                    totalLabel.setText("총 결제금액: " + totalPrice + "원");
                    mainFrame.updateGlobalCartCount(cartModel.size()); 
                });
            }

            JPanel centerInfo = new JPanel(new GridLayout(2, 1));
            centerInfo.setBackground(Color.WHITE);
            centerInfo.add(name);
            centerInfo.add(price);

            card.add(imageWrapper, BorderLayout.NORTH);
            card.add(centerInfo, BorderLayout.CENTER);
            card.add(addBtn, BorderLayout.SOUTH);
            
            menuGrid.add(card);
        }
        menuGrid.revalidate();
        menuGrid.repaint();
    }
}