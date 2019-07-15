import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private final String filesPath = "server/server_storage/";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            while (true) {
                if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    System.out.println("Получил файл - " + fm.getFilename());
                    Files.write(Paths.get(filesPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }
                if (msg instanceof Command) {
                    Command cmd = (Command) msg;
                    if (cmd.getCommand().equals("copy")) {
                        if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                            FileMessage fm = new FileMessage(Paths.get(filesPath + cmd.getItemName()));
                            ctx.writeAndFlush(fm);
                        }
                    }
                    if (cmd.getCommand().equals("del")) {
                        if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                            Files.delete(Paths.get(filesPath + cmd.getItemName()));
                        }
                    }
                    if (cmd.getCommand().equals("move")) {
                        if (Files.exists(Paths.get(filesPath + cmd.getItemName()))) {
                            FileMessage fm = new FileMessage(Paths.get("server/server_storage/" + cmd.getItemName()));
                            ctx.writeAndFlush(fm);
                            Files.delete(Paths.get(filesPath + cmd.getItemName()));
                        }
                    }
                    if (cmd.getCommand().equals("list")) {
                        Set<String> listClient = new HashSet();
                        Files.list(Paths.get(filesPath)).map(p -> p.getFileName().toString()).forEach(o -> listClient.add(o));
                        FilesListRequest flr = new FilesListRequest(listClient);
                        ctx.writeAndFlush(flr);
                    }

                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
