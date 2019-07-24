public class AuthRequest extends AbstractMessage {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
