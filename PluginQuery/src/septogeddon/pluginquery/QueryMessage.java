package septogeddon.pluginquery;

public class QueryMessage {

	private String channel;
	private byte[] message;
	
	public QueryMessage(String channel, byte[] message) {
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
