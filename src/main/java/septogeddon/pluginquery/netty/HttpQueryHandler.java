package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import septogeddon.pluginquery.http.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpQueryHandler extends ChannelInboundHandlerAdapter {
    /*
    SAMPLE HTTP REQUEST: (line break using \r\n)
    GET /simple_get HTTP/1.1
    Host: 127.0.0.1:5000
    User-Agent: Mozilla/5.0
    Accept: text/html
    Accept-Language: en-GB, en;q=0.8
    Accept-Encoding: gzip, deflate
    Connection: keep-alive
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ChannelPipeline pipe = ctx.channel().pipeline();
        if (msg instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byteBuf.markReaderIndex();
            try {
                String headLine = readLine(byteBuf);
                String splitHeadLine[] = headLine.split(" ", 3);
                if (splitHeadLine.length == 3) {
                    String method = splitHeadLine[0];
                    ProtocolMethod protocolMethod = HTTPContext.getContext().getMethod(method);
                    if (protocolMethod != null && protocolMethod.getContext() != null) {
                        HTTPContext context = protocolMethod.getContext();
                        if (context != null) {
                            String path = splitHeadLine[1];
                            ProtocolPath protocolPath = new ProtocolPath(path.isEmpty() ? path : path.substring(1));
                            Map<String, HTTPHeader> headers = new LinkedHashMap<>();
                            String line;
                            while (!(line = readLine(byteBuf)).isEmpty()) {
                                String splitLine[] = line.split(": ", 2);
                                if (splitLine.length == 2) {
                                    // header key must be lowercase in general
                                    String name = splitLine[0].toLowerCase();
                                    headers.put(name, HTTPHandler.parseHeader(name, splitLine[1]));
                                }
                            }
                            ProtocolRequest request = new ProtocolRequest(context, protocolMethod, protocolPath, splitHeadLine[2], headers, byteBuf);
                            ProtocolClient client = new ProtocolClient(request.getVersion(), context, ctx);
                            try {
                                context.dispatchRequest(request, client);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                            client.close();
                            ctx.disconnect();
                        }
                        return;
                    }
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            byteBuf.resetReaderIndex();
        }
        try {
            pipe.remove(this);
        } catch (Throwable t) {
        }
        ctx.fireChannelRead(msg);
    }

    private StringBuilder lineBuf = new StringBuilder();
    private String readLine(ByteBuf buffer) {
        this.lineBuf.setLength(0);

        while(buffer.isReadable()) {
            int c = buffer.readUnsignedByte();
            switch(c) {
                case 13:
                    if (buffer.isReadable() && buffer.getUnsignedByte(buffer.readerIndex()) == 10) {
                        buffer.skipBytes(1);
                    }
                case 10:
                    return this.lineBuf.toString();
                default:
                    this.lineBuf.append((char)c);
            }
        }

        return this.lineBuf.toString();
    }

}
