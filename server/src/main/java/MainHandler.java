import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import sun.nio.cs.ext.JISAutoDetect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private final String filesCommonPath = "server/server_storage/";
    String filesUserPath = filesCommonPath.concat("");

    ConnectDB cdb = new ConnectDB();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof AuthRequest) {
                AuthRequest ar = (AuthRequest) msg;
                if (isAuth(ar.getUserLogin(), ar.getUserPassword())) {
//                    filesUserPath = filesCommonPath.concat(ar.getUserLogin()).concat("/");
                    System.out.println(filesUserPath);
                    ctx.writeAndFlush(new Command("auth_ok"));
                } else {
                    ctx.writeAndFlush(new Command("auth_err"));
                }
            }
            if (msg instanceof FileData) {
                FileData fd = (FileData) msg;
                Files.write(Paths.get(filesUserPath + fd.getFileName()), fd.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof Command) {
                Command cmd = (Command) msg;
                if (cmd.getCommand().equals("copy")) {
                    if (Files.exists(Paths.get(filesUserPath + cmd.getItemName()))) {
                        FileData fd = new FileData(Paths.get(filesUserPath + cmd.getItemName()));
                        ctx.writeAndFlush(fd);
                    }
                }
                if (cmd.getCommand().equals("del")) {
                    if (Files.exists(Paths.get(filesUserPath + cmd.getItemName()))) {
                        Files.delete(Paths.get(filesUserPath + cmd.getItemName()));
                        sendFilesList(ctx);
                    }
                }
                if (cmd.getCommand().equals("move")) {
                    if (Files.exists(Paths.get(filesUserPath + cmd.getItemName()))) {
                        FileData fd = new FileData(Paths.get("server/server_storage/" + cmd.getItemName()));
                        ctx.writeAndFlush(fd);
                        Files.delete(Paths.get(filesUserPath + cmd.getItemName()));
                        sendFilesList(ctx);
                    }
                }
                if (cmd.getCommand().equals("list")) {
                    sendFilesList(ctx);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private boolean isAuth(String userLogin, String userPassword) {
//        return userLogin.equals("user1") && userPassword.equals("pass1");
        boolean result = false;
        cdb.connect();
        String sql = String.format("SELECT * FROM users WHERE username = '%s' AND password = '%s'", userLogin, userPassword);
        try {
            ResultSet resultSet = cdb.stmt.executeQuery(sql);
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void sendFilesList(ChannelHandlerContext ctx) throws IOException {
        Set<String> listFilesServer = Files.list(Paths.get(filesUserPath)).map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        FilesList fl = new FilesList(listFilesServer);
        ctx.writeAndFlush(fl);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
