import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            while (true) {
                if (msg instanceof FileMessage) {
                    FileMessage fm = (FileMessage) msg;
                    System.out.println("Получил файл - " + fm.getFilename());
                    Files.write(Paths.get("server/server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                }
                if (msg instanceof Command) {
                    Command cmd = (Command) msg;
                    if (cmd.getCommand().equals("copy")) {
                        if (Files.exists(Paths.get("server/server_storage/" + cmd.getItemName()))) {
                            FileMessage fm = new FileMessage(Paths.get("server/server_storage/" + cmd.getItemName()));
                            ctx.writeAndFlush(fm);
                        }
                    }
                    if (cmd.getCommand().equals("del")) {
                        if (Files.exists(Paths.get("server/server_storage/" + cmd.getItemName()))) {
                            Files.delete(Paths.get("server/server_storage/" + cmd.getItemName()));
                        }
                    }
                    if (cmd.getCommand().equals("move")) {
                        if (Files.exists(Paths.get("server/server_storage/" + cmd.getItemName()))) {
                            FileMessage fm = new FileMessage(Paths.get("server/server_storage/" + cmd.getItemName()));
                            ctx.writeAndFlush(fm);
                            Files.delete(Paths.get("server/server_storage/" + cmd.getItemName()));
                        }
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
