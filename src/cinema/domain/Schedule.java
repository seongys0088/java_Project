package cinema.domain;

public class Schedule {
    private int id;
    private int movieId;
    private String screenName;
    private String showDate; // ★ 날짜 필드 추가 (YYYY-MM-DD)
    private String startTime;

    public Schedule(int id, int movieId, String screenName, String showDate, String startTime) {
        this.id = id;
        this.movieId = movieId;
        this.screenName = screenName;
        this.showDate = showDate;
        this.startTime = startTime;
    }

    public int getId() { return id; }
    public String getScreenName() { return screenName; }
    public String getShowDate() { return showDate; }
    public String getStartTime() { return startTime; }
    
    // 콤보박스 표시용
    @Override
    public String toString() {
        return "[" + screenName + "] " + startTime;
    }
}