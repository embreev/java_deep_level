import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private final String filesPath = "server/server_storage/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileData) {
                FileData fd = (FileData) msg;
                Files.write(Paths.get(filesPath + fd.getFileName()), fd.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof Command) {
                Command cmd = (Command) msg;
                if (cmd.getCommand().equals("copy")) {
                    if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                        FileData fd = new FileData(Paths.get(filesPath + cmd.getItemName()));
                        ctx.writeAndFlush(fd);
                    }
                }
                if (cmd.getCommand().equals("del")) {
                    if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                        Files.delete(Paths.get(filesPath + cmd.getItemName()));
                        sendFilesList(ctx);
                    }
                }
                if (cmd.getCommand().equals("move")) {
                    if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                        FileData fd = new FileData(Paths.get("server/server_storage/" + cmd.getItemName()));
                        ctx.writeAndFlush(fd);
                        Files.delete(Paths.get(filesPath + cmd.getItemName()));
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

    private void sendFilesList(ChannelHandlerContext ctx) throws IOException {
        Set<String> listFilesServer = Files.list(Paths.get(filesPath)).map(p -> p.getFileName().toString()).collect(Collectors.toSet());
        FilesList fl = new FilesList(listFilesServer);
        ctx.writeAndFlush(fl);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
