import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private String username;
    private String password;
    private ConnectDB db = new ConnectDB();

    AuthHandler() {
        db.connect();
    }

    private boolean isAuth (String username, String password) {
        boolean result = false;
        String sql = String.format("SELECT * FROM users WHERE username = '%s' AND password = '%s'", username, password);
        try {
            ResultSet resultSet = db.stmt.executeQuery(sql);
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        if (isAuth("usr1", "pswd")) {
            ctx.fireChannelRead(msg);
        } else {
            ctx.writeAndFlush("Unauthorized!");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
