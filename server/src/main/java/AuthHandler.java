import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private ConnectDB db = new ConnectDB();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        db.connect();

        while (true) {
            if (msg instanceof AuthRequest) {
                AuthRequest ar = (AuthRequest) msg;
                System.out.println(ar.getUsername());
                if (isAuth(ar.getUsername(), ar.getPassword())) {
                    ctx.write(new Command("authorized"));
                    ctx.fireChannelRead(msg);
                    ctx.flush();
                    break;
                } else {
                    ctx.writeAndFlush(new Command("unauthorized"));
                }
            }
        }
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
