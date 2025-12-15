package cinema.domain;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private int runningTime;
    private String posterPath; // 추가됨

    public Movie(int id, String title, String genre, int runningTime, String posterPath) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.runningTime = runningTime;
        this.posterPath = posterPath;
    }
    // Getters...
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getRunningTime() { return runningTime; }
    public String getPosterPath() { return posterPath; } // 추가됨
    
    @Override public String toString() { return title; } // 콤보박스용
}