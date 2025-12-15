package cinema.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cinema.dao.ReservationDAO;
import cinema.dao.ScreenDAO;
import cinema.domain.Movie;
import cinema.domain.Schedule;
import cinema.domain.User;
import cinema.util.UIUtils;
import cinema.domain.Screen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionDialog extends JDialog {
    private ReservationDAO resDAO;
    private User currentUser;
    private Movie currentMovie;
    private Schedule currentSchedule;
    
    // ★ 다중 선택을 위한 리스트
    private List<Point> selectedSeats = new ArrayList<>(); 
    private JButton[][] seatButtons;
    
    // UI 컴포넌트
    private JLabel selectedSeatLabel;
    private JLabel totalPriceLabel;
    private JLabel personCountLabel;
    
    // 인원수 변수 (기본 1명)
    private int personCount = 1;
    private final int TICKET_PRICE = 12000;

    public SeatSelectionDialog(JFrame parent, User user, Movie movie, Schedule schedule) {
        super(parent, "좌석 선택", true);
        this.currentUser = user;
        this.currentMovie = movie;
        this.currentSchedule = schedule;
        this.resDAO = new ReservationDAO();
        
        setSize(1000, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.BG_MAIN);

        // --- [좌측] 스크린 + 좌석 배치도 ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UIUtils.BG_MAIN);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel screenLabel = new JLabel("SCREEN", SwingConstants.CENTER);
        screenLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(new Color(200, 200, 200));
        screenLabel.setPreferredSize(new Dimension(0, 30));
        
        // 상영관 정보로 좌석 크기 결정
        ScreenDAO screenDAO = new ScreenDAO();
        Screen screenInfo = screenDAO.getScreenByName(schedule.getScreenName());
        int maxRow = (screenInfo != null) ? screenInfo.getTotalRow() : 5;
        int maxCol = (screenInfo != null) ? screenInfo.getTotalCol() : 5;

        JPanel seatGrid = new JPanel(new GridLayout(maxRow, maxCol, 6, 6));
        seatGrid.setBackground(UIUtils.BG_MAIN);
        seatGrid.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        seatButtons = new JButton[maxRow + 1][maxCol + 1];
        List<Point> takenSeats = resDAO.getReservedSeats(schedule.getId());

        for (int r = 1; r <= maxRow; r++) {
            for (int c = 1; c <= maxCol; c++) {
                JButton seat = new JButton(r + "-" + c);
                seat.setFont(new Font("SansSerif", Font.PLAIN, 10));
                seat.setFocusPainted(false);
                seat.setBorder(new UIUtils.RoundedBorder(Color.LIGHT_GRAY, 5));
                
                // ★ 이미 예약된 좌석 처리 (X 표시, 진한 색, 비활성화) ★
                boolean isTaken = false;
                for(Point p : takenSeats) {
                    if(p.x == r && p.y == c) {
                        isTaken = true;
                        break;
                    }
                }

                if (isTaken) {
                    seat.setBackground(new Color(80, 80, 80)); // 진한 회색 (예약됨)
                    seat.setForeground(Color.WHITE);
                    seat.setText("X");
                    seat.setEnabled(false); // 클릭 불가
                } else {
                    seat.setBackground(Color.WHITE); // 선택 가능 (흰색)
                    int finalR = r; int finalC = c;
                    seat.addActionListener(e -> toggleSeat(finalR, finalC));
                }
                
                seatButtons[r][c] = seat;
                seatGrid.add(seat);
            }
        }

        leftPanel.add(screenLabel, BorderLayout.NORTH);
        leftPanel.add(seatGrid, BorderLayout.CENTER);

        // --- [우측] 정보 및 결제창 ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(300, 0));
        rightPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel infoTitle = new JLabel("예매 정보");
        infoTitle.setFont(UIUtils.FONT_SUBTITLE);
        
        rightPanel.add(infoTitle);
        rightPanel.add(Box.createVerticalStrut(20));
        
        // 1. 영화 정보
        rightPanel.add(createDetailLabel("영화", movie.getTitle()));
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(createDetailLabel("일시", schedule.getStartTime())); // 날짜는 공간상 생략하거나 추가 가능
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(createDetailLabel("상영관", schedule.getScreenName()));
        
        // 구분선
        JSeparator sep1 = new JSeparator(); sep1.setForeground(UIUtils.COLOR_BORDER);
        rightPanel.add(Box.createVerticalStrut(15)); rightPanel.add(sep1); rightPanel.add(Box.createVerticalStrut(15));

        // 2. ★ 인원 수 선택 ( - 1 + ) ★
        JLabel personTitle = new JLabel("인원 선택");
        personTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        personTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel counterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        counterPanel.setBackground(Color.WHITE);
        
        JButton minusBtn = createCountBtn("-");
        JButton plusBtn = createCountBtn("+");
        personCountLabel = new JLabel("1", SwingConstants.CENTER);
        personCountLabel.setPreferredSize(new Dimension(40, 30));
        personCountLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        // 인원수 조절 이벤트
        minusBtn.addActionListener(e -> changePersonCount(-1));
        plusBtn.addActionListener(e -> changePersonCount(1));
        
        counterPanel.add(minusBtn);
        counterPanel.add(personCountLabel);
        counterPanel.add(plusBtn);
        
        rightPanel.add(personTitle);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(counterPanel);
        
        rightPanel.add(Box.createVerticalStrut(20));

        // 3. 선택 좌석 정보
        selectedSeatLabel = new JLabel("-");
        selectedSeatLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        selectedSeatLabel.setForeground(UIUtils.COLOR_ACCENT);
        
        JPanel seatInfoBox = new JPanel(new BorderLayout());
        seatInfoBox.setBackground(Color.WHITE);
        JLabel seatTitle = new JLabel("선택 좌석");
        seatTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        seatInfoBox.add(seatTitle, BorderLayout.NORTH);
        seatInfoBox.add(selectedSeatLabel, BorderLayout.CENTER);
        
        rightPanel.add(seatInfoBox);
        rightPanel.add(Box.createVerticalGlue()); // 아래로 밀기

        // 4. 총 금액 및 결제 버튼
        totalPriceLabel = new JLabel("12,000원"); // 기본 1명 가격
        totalPriceLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        totalPriceLabel.setForeground(UIUtils.COLOR_ACCENT);
        totalPriceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        JPanel priceBox = new JPanel(new BorderLayout());
        priceBox.setBackground(Color.WHITE);
        priceBox.add(new JLabel("총 결제금액"), BorderLayout.WEST);
        priceBox.add(totalPriceLabel, BorderLayout.EAST);
        
        JButton payBtn = UIUtils.createStyledButton("결제하기");
        payBtn.setPreferredSize(new Dimension(250, 50));
        payBtn.setMaximumSize(new Dimension(250, 50));
        payBtn.addActionListener(e -> processReservation());
        
        rightPanel.add(priceBox);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(payBtn);

        // 전체 조립
        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    // 인원수 변경 로직
    private void changePersonCount(int delta) {
        int newCount = personCount + delta;
        if (newCount < 1 || newCount > 8) return; // 1~8명 제한
        
        // 인원수가 줄어들 때, 이미 선택된 좌석이 인원수보다 많으면 초기화
        if (newCount < selectedSeats.size()) {
            JOptionPane.showMessageDialog(this, "선택된 좌석이 인원수보다 많습니다.\n좌석을 다시 선택해주세요.");
            resetSelection();
        }
        
        personCount = newCount;
        personCountLabel.setText(String.valueOf(personCount));
        updatePrice();
    }

    // 좌석 클릭(토글) 로직
    private void toggleSeat(int r, int c) {
        Point p = new Point(r, c);

        if (selectedSeats.contains(p)) {
            // 이미 선택된거면 취소 (삭제)
            selectedSeats.remove(p);
            seatButtons[r][c].setBackground(Color.WHITE);
        } else {
            // 새로 선택
            if (selectedSeats.size() >= personCount) {
                JOptionPane.showMessageDialog(this, "인원수(" + personCount + "명)만큼만 선택할 수 있습니다.");
                return;
            }
            selectedSeats.add(p);
            seatButtons[r][c].setBackground(new Color(245, 166, 35)); // 오렌지색 (선택됨)
        }
        updateSelectedSeatsLabel();
    }

    // 선택된 좌석 라벨 업데이트
    private void updateSelectedSeatsLabel() {
        if (selectedSeats.isEmpty()) {
            selectedSeatLabel.setText("-");
        } else {
            StringBuilder sb = new StringBuilder("<html>");
            for (Point p : selectedSeats) {
                sb.append("[").append(p.x).append("-").append(p.y).append("] ");
            }
            sb.append("</html>");
            selectedSeatLabel.setText(sb.toString());
        }
    }

    // 가격 업데이트
    private void updatePrice() {
        // 인원수 * 티켓가격
        int total = personCount * TICKET_PRICE;
        totalPriceLabel.setText(String.format("%,d원", total));
    }
    
    // 선택 초기화
    private void resetSelection() {
        for (Point p : selectedSeats) {
            seatButtons[p.x][p.y].setBackground(Color.WHITE);
        }
        selectedSeats.clear();
        updateSelectedSeatsLabel();
    }

    // 결제 프로세스 (다중 예매)
    private void processReservation() {
        if (selectedSeats.size() != personCount) {
            JOptionPane.showMessageDialog(this, "인원수에 맞춰 좌석을 모두 선택해주세요.\n(현재 " + selectedSeats.size() + "/" + personCount + ")");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "총 " + personCount + "명, " + totalPriceLabel.getText() + "\n결제하시겠습니까?", 
                "예매 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean allSuccess = true;
            // 선택된 모든 좌석에 대해 예매 진행
            for (Point p : selectedSeats) {
                boolean success = resDAO.addReservation(
                    currentUser.getId(), 
                    currentMovie.getId(), 
                    currentSchedule.getId(), 
                    p.x, p.y
                );
                if (!success) allSuccess = false;
            }
            
            if (allSuccess) {
                JOptionPane.showMessageDialog(this, "예매가 완료되었습니다!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "일부 좌석 예매 중 오류가 발생했습니다.\n(이미 선택된 좌석일 수 있습니다)");
            }
        }
    }

    // 정보 표시용 라벨 패널 생성기
    private JPanel createDetailLabel(String title, String content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel t = new JLabel(title); t.setForeground(Color.GRAY);
        JLabel c = new JLabel(content); c.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(t, BorderLayout.WEST);
        p.add(c, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return p;
    }
    
    // +/- 버튼 생성기
    private JButton createCountBtn(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }
}