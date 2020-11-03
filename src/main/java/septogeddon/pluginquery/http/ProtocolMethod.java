package septogeddon.pluginquery.http;

/**
 * The HTTP method
 */
public interface ProtocolMethod {
    /**
     * Get the protocol name
     * @return
     */
    String name();

    /**
     * Get the protocol context
     * @return
     */
    HTTPContext getContext();

    /**
     * General methods that are often used for RestfulAPI services
     */
    enum General implements ProtocolMethod {
        GET, PUT, POST, PATCH, DELETE, TRACE, CONNECT, OPTIONS, HEAD;

        /**
         * Get the default HTTP Context
         * @return HTTPContext instance
         * @see HTTPContext#getContext()
         */
        @Override
        public HTTPContext getContext() {
            return HTTPContext.getContext();
        }
    }
}
