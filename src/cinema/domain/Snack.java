package cinema.domain;

public class Snack {
    private int id;
    private String name;
    private int price;
    private String category;
    private String imagePath; // 추가됨
    private boolean isSoldOut; // 추가됨

    public Snack(int id, String name, int price, String category, String imagePath, String soldOutYn) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.isSoldOut = "Y".equals(soldOutYn); // Y면 true, N이면 false
    }
    // Getters...
    public int getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; } // 추가됨
    public boolean isSoldOut() { return isSoldOut; } // 추가됨
}