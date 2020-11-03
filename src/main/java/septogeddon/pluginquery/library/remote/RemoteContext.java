package septogeddon.pluginquery.library.remote;

/**
 * Context for remote library
 * @author Thito Yalasatria Sunarya
 *
 */
public interface RemoteContext {

    Byte COMMAND_FETCH_OBJECT = 0;
    Byte COMMAND_STORE_OBJECT = 1;
    Byte COMMAND_INVOKE_METHOD = 2;
    Byte COMMAND_RESPONSE_RESULT = 3;
    Byte COMMAND_DELIVERED_EXCEPTION = 4;
    Byte COMMAND_CLOSE_REFERENCE = 5;
    Byte COMMAND_PING = 6;
    Byte COMMAND_PONG = 7;

}
