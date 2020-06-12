package septogeddon.pluginquery;

import septogeddon.pluginquery.utils.QueryUtil;

public class QueryMessage {

	private String channel;
	private byte[] message;
	
	public QueryMessage(String channel, byte[] message) {
		QueryUtil.illegalArgument(channel.length() > Byte.MAX_VALUE, "channel length too long > "+Byte.MAX_VALUE);
		this.channel = channel;
		this.message = message;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public byte[] getMessage() {
		return message;
	}
	
	public void setMessage(byte[] message) {
		this.message = message;
	}

}
