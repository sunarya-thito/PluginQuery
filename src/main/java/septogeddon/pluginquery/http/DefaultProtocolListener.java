package septogeddon.pluginquery.http;

public class DefaultProtocolListener implements ProtocolListener {
    private String html =
            "<html>\n" +
            "    <head>\n" +
            "        <title>Plugin Query</title>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <h1>Plugin Query</h1>\n" +
            "        <h2>HTTP Protocol</h2>\n" +
            "        <p>This is the default plugin query HTTP protocol static page.</p>\n" +
            "    </body>\n" +
            "</html>";

    @Override
    public void onRequest(ProtocolClient client, ProtocolRequest request) {
        if (request.getPath().length() == 1) {
            client.setHeader("Content-Type", "text/html");
            client.setHeader("Content-Length", html.length());
            client.write(html.getBytes());
            client.close();
        }
    }
}
