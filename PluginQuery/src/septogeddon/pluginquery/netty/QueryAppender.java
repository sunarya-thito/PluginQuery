package septogeddon.pluginquery.netty;

import java.io.IOException;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class QueryAppender extends ByteToMessageDecoder {
	@Override
	protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
		in.markReaderIndex();
		final byte[] buf = new byte[3];
		int i = 0;
		while (i < buf.length) {
			if (!in.isReadable()) {
				in.resetReaderIndex();
				return;
			}
			buf[i] = in.readByte();
			if (buf[i] >= 0) {
				final int length = readUnsignedInteger(Unpooled.wrappedBuffer(buf));
				if (length == 0) {
					throw new IOException("invalid packet");
				}
				if (in.readableBytes() < length) {
					in.resetReaderIndex();
					return;
				}
				if (in.hasMemoryAddress()) {
					out.add(in.slice(in.readerIndex(), length).retain());
					in.skipBytes(length);
				} else {
					final ByteBuf dst = ctx.alloc().directBuffer(length);
					in.readBytes(dst);
					out.add(dst);
				}
				return;
			} else {
				++i;
			}
		}
		throw new IOException("packet too large");
	}

	public int readUnsignedInteger(final ByteBuf input) throws IOException {
		int out = 0;
		int bytes = 0;
		byte in;
		do {
			in = input.readByte();
			out |= (in & 0x7F) << bytes++ * 7;
			if (bytes > 5) {
				throw new IOException("invalid unsigned int");
			}
		} while ((in & 0x80) == 0x80);
		return out;
	}
}
