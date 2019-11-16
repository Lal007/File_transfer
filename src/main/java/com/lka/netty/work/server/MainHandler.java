package com.lka.netty.work.server;

import com.lka.netty.work.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainHandler extends ChannelInboundHandlerAdapter {
    private byte[] buffer = new byte[FileMessage.MSG_BYTE_BUFFER];

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
                    try (BufferedInputStream in = new BufferedInputStream(new FileInputStream("server_storage/" + fr.getFilename()))){
                        int len = 0;
                        while ((len = in.read(buffer)) != -1){
                            FileMessage fm = new FileMessage(fr.getFilename(), buffer, len);
                            ctx.writeAndFlush(fm);
                            System.out.println("Send file " + len);
                        }
                    }
                }
            }
            else if (msg instanceof FileMessage){
                FileMessage fm = (FileMessage) msg;
                System.out.println("receive " + fm.getLen());
                if (fm.getLen() == FileMessage.MSG_BYTE_BUFFER){
                    Files.write(Paths.get("server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.SYNC);
                }else {
                    byte[] tmpBuf = new byte[fm.getLen()];
                    System.arraycopy(fm.getData(), 0, tmpBuf, 0, fm.getLen());
                    Files.write(Paths.get("server_storage/" + fm.getFilename()), tmpBuf,StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }
            }
            else if (msg instanceof ListOfFilesRequest){
                List<String> listOfFiles = Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).collect(Collectors.toCollection(ArrayList::new));
                ctx.writeAndFlush(new ListOfFiles(listOfFiles));
            }
            else if (msg instanceof DeleteFileReq){
                DeleteFileReq dfr = (DeleteFileReq) msg;
                if (Files.exists(Paths.get(dfr.getFileToDelete()))){
                    Files.delete(Paths.get(dfr.getFileToDelete()));
                }
            }
            else System.out.println(msg.toString());
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
