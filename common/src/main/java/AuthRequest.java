public class AuthRequest extends AbstractMessage {
    private String userLogin;
    private String userPassword;

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public AuthRequest(String userLogin, String userPassword) {
        this.userLogin = userLogin;
        this.userPassword = userPassword;
    }
}
