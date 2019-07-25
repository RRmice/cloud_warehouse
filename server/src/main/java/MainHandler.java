import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainHandler extends ChannelInboundHandlerAdapter {

    ServersMessage svm = new ServersMessage();
    AuthService authService = new AuthService();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (!authService.isAuthorized(ctx.channel())){

            if (msg instanceof CommandRequest
                    && (((CommandRequest) msg).getCt() == CommandType.Authorization)){

                String[] arg = ((CommandRequest) msg).getArguments();
                if (arg.length >= 2) {
                    boolean result = authService.loginUser(ctx.channel(), arg[0], arg[1]);
                    if (result) {
                        ctx.writeAndFlush(new CommandRequest(CommandType.Authorization, "OK"));
                    }
                }
            }

        } else {
            check(ctx, msg);
        }

    }

    private void check(ChannelHandlerContext ctx, Object msg) {

        try {
            if (msg instanceof FileRequest) {
                ctx.write(svm.fileRequest((FileRequest) msg));
            }

            if (msg instanceof FileMessage) {
                File file = svm.fileMessage((FileMessage) msg).toFile();
                authService.addFile(ctx.channel(), file.getName(), file.length());
            }

            if(msg instanceof CommandRequest){

                CommandRequest cr = (CommandRequest) msg;
                if (cr.getCt() == CommandType.GetFileLIST){

                    ArrayList<String> files = authService.getUserFiles(ctx.channel());
                    String[] filesArr = new String[files.size()];

                    for (int i = 0; i < files.size(); i++) {
                        filesArr[i] = files.get(i);
                    }

                    ctx.writeAndFlush(new CommandRequest(CommandType.GetFileLIST, filesArr));
                }
                if (cr.getCt() == CommandType.DeleteFile){

                    File file  = Paths.get(getDefaultCatalog() + "/" + cr.getArguments()[0]).toFile();
                    authService.deleteFile(ctx.channel(), cr.getArguments()[0]);
                    if (file.exists()){
                        file.delete();

                        File folder = new File(getDefaultCatalog());
                        String[] filesInCatalog = folder.list();

                        ctx.writeAndFlush(new CommandRequest(CommandType.GetFileLIST, filesInCatalog));
                    }

                }
                if (cr.getCt() == CommandType.Disconnect){
                    ctx.disconnect();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private String getDefaultCatalog(){
        return "server_storage";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
