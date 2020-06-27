package septogeddon.pluginquery.library.remote;

/***
 * Context for remote library
 * @author Thito Yalasatria Sunarya
 *
 */
public interface RemoteContext {

	public static final Byte COMMAND_FETCH_OBJECT = 0;
	public static final Byte COMMAND_STORE_OBJECT = 1;
	public static final Byte COMMAND_INVOKE_METHOD = 2;
	public static final Byte COMMAND_RESPONSE_RESULT = 3;
	public static final Byte COMMAND_DELIVERED_EXCEPTION = 4;
	public static final Byte COMMAND_CLOSE_REFERENCE = 5;
	public static final Byte COMMAND_PING = 6;
	public static final Byte COMMAND_PONG = 7;
	
}
