package cinema.domain;

public class User {
    private String id;
    private String name;
    private String role; // "ADMIN", "USER", or "BANNED"

    public User(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
    
    // ★★★ [신규] 정지 상태 확인 메서드 ★★★
    public boolean isBanned() { return "BANNED".equals(role); }
}