package cinema.domain;

public class Screen {
    private int id;
    private String name;
    private int totalRow;
    private int totalCol;

    public Screen(int id, String name, int totalRow, int totalCol) {
        this.id = id;
        this.name = name;
        this.totalRow = totalRow;
        this.totalCol = totalCol;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public int getTotalRow() { return totalRow; }
    public int getTotalCol() { return totalCol; }
    
    @Override
    public String toString() { return name; } // 콤보박스용
}