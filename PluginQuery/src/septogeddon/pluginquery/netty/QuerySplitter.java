package septogeddon.pluginquery.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class QuerySplitter extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) throws Exception {
		final int bodyLen = msg.readableBytes();
		final int headerLen = byteLength(bodyLen);
		out.ensureWritable(headerLen + bodyLen);
		writeUnsignedShort(bodyLen, out);
		out.writeBytes(msg);
	}

	private int byteLength(final int paramInt) {
		if ((paramInt & 0xFFFFFF80) == 0x0) {
			return 1;
		}
		if ((paramInt & 0xFFFFC000) == 0x0) {
			return 2;
		}
		if ((paramInt & 0xFFE00000) == 0x0) {
			return 3;
		}
		if ((paramInt & 0xF0000000) == 0x0) {
			return 4;
		}
		return 5;
	}

	private void writeUnsignedShort(int value, final ByteBuf output) {
		do {
			int part = value & 0x7F;
			value >>>= 7;
			if (value != 0) {
				part |= 0x80;
			}
			output.writeByte(part);
		} while (value != 0);
	}

}
