public class AuthRequest extends AbstractMessage {
    private String username;
    private String password;
    private boolean auth;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAuth() {
        return auth;
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
