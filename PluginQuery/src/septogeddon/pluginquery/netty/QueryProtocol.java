package septogeddon.pluginquery.netty;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryMessenger;

public class QueryProtocol {

	private QueryHandshaker handshaker;
	private QueryManager manager;
	private QueryEncoder encoder;
	private QueryDecoder decoder;
	private QueryPipelineInbound pipein;
	private QueryPipelineOutbound pipeout;
	private QueryAppender appender;
	private QuerySplitter splitter;
	private QueryMessenger messenger;
	private QueryConnection connection;
	public QueryProtocol(QueryMessenger messenger, QueryConnection connection) {
		this.messenger = messenger;
		this.connection = connection;
	}
	
	public QueryMessenger getMessenger() {
		return messenger;
	}
	
	public QueryConnection getConnection() {
		return connection;
	}
	
	public QueryAppender getAppender() {
		return appender == null ? appender = new QueryAppender() : appender;
	}
	
	public QuerySplitter getSplitter() {
		return splitter == null ? splitter = new QuerySplitter() : splitter;
	}
	
	public QueryPipelineOutbound getPipelineOutbound() {
		return pipeout == null ? pipeout = new QueryPipelineOutbound(getMessenger().getPipeline(), getConnection()) : pipeout;
	}
	
	public QueryPipelineInbound getPipelineInbound() {
		return pipein == null ? pipein = new QueryPipelineInbound(getMessenger().getPipeline(), getConnection()) : pipein;
	}
	
	public QueryManager getManager() {
		return manager == null ? manager = new QueryManager(this) : manager;
	}
	
	public QueryHandshaker getHandshaker() {
		return handshaker == null ? handshaker = new QueryHandshaker(this) : handshaker;
	}

	public QueryEncoder getEncoder() {
		return encoder == null ? encoder = new QueryEncoder() : encoder;
	}
	
	public QueryDecoder getDecoder() {
		return decoder == null ? decoder = new QueryDecoder() : decoder;
	}
	
}
