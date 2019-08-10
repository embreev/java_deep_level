import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private final String filesCommonPath = "server/server_storage/";
    private String filesUserPath = filesCommonPath.concat("");
    private static Set<String> usersList = new HashSet<String>();
    private ConnectDB cdb = new ConnectDB();
    private String userLogin;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof AuthRequest) {
                AuthRequest ar = (AuthRequest) msg;
                userLogin = ar.getUserLogin();
                if (!usersList.contains(userLogin)) {
                    if (isAuth(userLogin, ar.getUserPassword())) {
                        filesUserPath = getUserDir(userLogin);
                        System.out.println(filesUserPath);
                        usersList.add(userLogin);
                        System.out.println(usersList);
                        ctx.writeAndFlush(new Command("auth_ok"));
                        sendFilesList(ctx);
                    } else {
                        ctx.writeAndFlush(new Command("auth_err"));
                    }
                } else {
                    ctx.writeAndFlush(new Command("auth_duplicate"));
                }
            }
            if (msg instanceof FileData) {
                FileData fd = (FileData) msg;
                Files.write(Paths.get(filesUserPath + fd.getFileName()), fd.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof Command) {
                Command cmd = (Command) msg;
                String userFile = filesUserPath + cmd.getItemName();
                if (cmd.getCommand().equals("copy")) {
                    if (Files.exists(Paths.get(userFile))) {
                        FileData fd = new FileData(Paths.get(userFile));
                        ctx.writeAndFlush(fd);
                    }
                }
                if (cmd.getCommand().equals("del")) {
                    if (Files.exists(Paths.get(userFile))) {
                        Files.delete(Paths.get(userFile));
                        sendFilesList(ctx);
                    }
                }
                if (cmd.getCommand().equals("move")) {
                    if (Files.exists(Paths.get(userFile))) {
                        FileData fd = new FileData(Paths.get(userFile));
                        ctx.writeAndFlush(fd);
                        Files.delete(Paths.get(userFile));
                        sendFilesList(ctx);
                    }
                }
                if (cmd.getCommand().equals("list")) {
                    sendFilesList(ctx);
                }
                if (cmd.getCommand().equals("disconnect")) {
                    usersList.remove(userLogin);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private String getUserDir(String userLogin) {
        String dir = filesCommonPath.concat(userLogin.concat("/"));
            if(!Files.exists(Paths.get(dir))) {
                try {
                    Files.createDirectory(Paths.get(dir));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return dir;
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
