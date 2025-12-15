package cinema.util;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {
    // 🎨 화이트 & 라이트 그레이 테마 적용
    public static final Color BG_MAIN = new Color(245, 247, 250); // 전체 배경 (연한 회색)
    public static final Color BG_CARD = Color.WHITE;              // 카드/패널 배경 (흰색)
    public static final Color COLOR_ACCENT = new Color(229, 9, 20); // 포인트 레드 (예매 버튼 등)
    public static final Color COLOR_TEXT = new Color(30, 30, 30);   // 진한 검정 텍스트
    public static final Color COLOR_TEXT_GRAY = new Color(120, 120, 120); // 연한 텍스트
    public static final Color COLOR_BORDER = new Color(220, 220, 220); // 연한 테두리
    // ★ 추가된 색상
    public static final Color BG_DARK_SIDEBAR = new Color(38, 43, 64); // 관리자 사이드바 배경

    // 폰트 설정
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BTN = new Font("SansSerif", Font.BOLD, 13);

    public static void initCustomUI() {
        try {
            // 전체 배경 통일
            UIManager.put("Panel.background", BG_MAIN);
            UIManager.put("OptionPane.background", BG_MAIN);
            UIManager.put("Dialog.background", BG_MAIN);
            UIManager.put("Viewport.background", BG_MAIN);
            
            // 텍스트 색상
            UIManager.put("Label.foreground", COLOR_TEXT);
            UIManager.put("Button.foreground", COLOR_TEXT);
            UIManager.put("OptionPane.messageForeground", COLOR_TEXT);
            
            // 입력창 (흰색 배경)
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", COLOR_TEXT);
            UIManager.put("TextField.caretForeground", COLOR_TEXT);
            UIManager.put("PasswordField.background", Color.WHITE);
            UIManager.put("PasswordField.foreground", COLOR_TEXT);
            
            // 폰트
            UIManager.put("Label.font", FONT_MAIN);
            UIManager.put("Button.font", FONT_MAIN);
            UIManager.put("TextField.font", FONT_MAIN);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 둥근 테두리
    public static class RoundedBorder extends AbstractBorder {
        private final Color color; private final int radius;
        public RoundedBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(5, 5, 5, 5); }
    }

    // 포인트 버튼 (빨간 배경)
    public static JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(COLOR_ACCENT.darker());
                else g2.setColor(COLOR_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }

    // 보조 버튼 (흰 배경 + 테두리) - 이전에 누락되었을 수 있는 부분
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(FONT_MAIN);
        btn.setForeground(COLOR_TEXT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }

    // ★★★ [중요] 오류 해결을 위해 이 메서드가 꼭 있어야 합니다! ★★★
    // 시간 선택용 칩 버튼 (흰 배경 + 둥근 테두리)
    public static JButton createTimeButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); // 배경 흰색
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15); // 더 둥글게
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(COLOR_TEXT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(COLOR_BORDER, 10),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    public static int showConfirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "확인", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
    }
}