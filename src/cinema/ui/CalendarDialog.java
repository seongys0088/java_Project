package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.util.UIUtils;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CalendarDialog extends JDialog {
    private LocalDate selectedDate;
    private LocalDate currentDate;
    private JLabel monthLabel;
    private JPanel dayPanel;
    private boolean isConfirmed = false;

    public CalendarDialog(JFrame parent) {
        super(parent, "날짜 선택", true); // 모달 창
        currentDate = LocalDate.now();
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // ★ 배경색을 밝은 테마로 변경
        getContentPane().setBackground(UIUtils.BG_MAIN); 

        // 1. 상단 (이전/다음 달 이동)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BG_MAIN);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton prevBtn = createNavButton("<");
        JButton nextBtn = createNavButton(">");

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        monthLabel.setForeground(UIUtils.COLOR_TEXT); // 검정 텍스트

        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));

        header.add(prevBtn, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 2. 중앙 (요일 + 날짜)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIUtils.BG_MAIN);
        centerPanel.setBorder(new EmptyBorder(0, 15, 15, 15));

        // 요일 헤더
        JPanel weekPanel = new JPanel(new GridLayout(1, 7));
        weekPanel.setBackground(UIUtils.BG_MAIN);
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 14));
            // 요일 색상 구분
            if ("일".equals(d)) l.setForeground(new Color(220, 50, 50));
            else if ("토".equals(d)) l.setForeground(new Color(50, 100, 220));
            else l.setForeground(UIUtils.COLOR_TEXT_GRAY);
            weekPanel.add(l);
        }
        centerPanel.add(weekPanel, BorderLayout.NORTH);

        // 날짜 그리드
        dayPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        dayPanel.setBackground(UIUtils.BG_MAIN);
        dayPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        centerPanel.add(dayPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        updateCalendar();
    }
    
    // 네비게이션 버튼 스타일 (화살표)
    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(45, 35));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(UIUtils.COLOR_TEXT);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void changeMonth(int amount) {
        currentDate = currentDate.plusMonths(amount);
        updateCalendar();
    }

    private void updateCalendar() {
        dayPanel.removeAll();
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월")));

        YearMonth yearMonth = YearMonth.from(currentDate);
        int dayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue() % 7; 
        int lengthOfMonth = yearMonth.lengthOfMonth();

        // 앞쪽 빈칸 채우기
        for (int i = 0; i < dayOfWeek; i++) {
            JLabel empty = new JLabel("");
            dayPanel.add(empty);
        }

        // 날짜 버튼 생성
        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            
            dayBtn.setFocusPainted(false);
            dayBtn.setBackground(Color.WHITE);
            dayBtn.setForeground(UIUtils.COLOR_TEXT);
            // ★ 중요: 폰트 크기 13 + 여백 0으로 설정하여 글자 잘림(...) 방지
            dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 13)); 
            dayBtn.setMargin(new Insets(0, 0, 0, 0)); 
            
            // 오늘 날짜 강조 (테두리 + 굵게)
            if (date.equals(LocalDate.now())) {
                dayBtn.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_ACCENT, 2));
                dayBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            } else {
                dayBtn.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
            }

            dayBtn.addActionListener(e -> {
                selectedDate = date;
                isConfirmed = true;
                dispose();
            });
            
            dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dayPanel.add(dayBtn);
        }
        
        dayPanel.revalidate();
        dayPanel.repaint();
    }

    public String getSelectedDate() {
        return isConfirmed && selectedDate != null ? selectedDate.toString() : null;
    }
}