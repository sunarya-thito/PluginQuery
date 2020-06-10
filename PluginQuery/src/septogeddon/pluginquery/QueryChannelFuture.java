package septogeddon.pluginquery;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class QueryChannelFuture<T> extends QueryFutureAdapter<T> implements ChannelFutureListener {

	private ChannelFuture future;
	private T defaultResult;
	public QueryChannelFuture(ChannelFuture future, T defaultResult) {
		this.future = future;
		this.defaultResult = defaultResult;
		future.addListener(this);
	}
	
	@Override
	public void joinThread() {
		future.awaitUninterruptibly();
	}

	@Override
	public void joinThread(long timeout) {
		future.awaitUninterruptibly(timeout);
	}

	@Override
	public void operationComplete(ChannelFuture arg0) throws Exception {
		if (arg0.isSuccess()) {
			complete(defaultResult);
		} else {
			completeExceptionally(arg0.cause());
		}
	}

}
