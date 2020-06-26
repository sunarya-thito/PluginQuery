package septogeddon.pluginquery.library.remote;

public interface RemoteContext {

	public static final byte COMMAND_FETCH_OBJECT = 0;
	public static final byte COMMAND_STORE_OBJECT = 1;
	public static final byte COMMAND_INVOKE_METHOD = 2;
	public static final byte COMMAND_RETURN_METHOD = 3;
	public static final byte COMMAND_DELIVERED_EXCEPTION = 4;
	public static final byte COMMAND_CLOSE_REFERENCE = 5;
	
}
