package io.elastest.etm.beats;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the connection state to the beats client.
 */
public class ConnectionHandler extends ChannelDuplexHandler {
    public final Logger logger = getLogger(lookup().lookupClass());

    public static AttributeKey<AtomicBoolean> CHANNEL_SEND_KEEP_ALIVE = AttributeKey
            .valueOf("channel-send-keep-alive");

    @Override
    public void channelActive(final ChannelHandlerContext ctx)
            throws Exception {
        ctx.channel().attr(CHANNEL_SEND_KEEP_ALIVE)
                .set(new AtomicBoolean(false));
        if (logger.isTraceEnabled()) {
            logger.trace("{}: channel activated",
                    ctx.channel().id().asShortText());
        }
        super.channelActive(ctx);
    }

    /**
     * {@inheritDoc} Sets the flag that the keep alive should be sent.
     * {@link BeatsHandler} will un-set it. It is important that this handler
     * comes before the {@link BeatsHandler} in the channel pipeline. Note - For
     * large payloads, this method may be called many times more often then the
     * BeatsHandler#channelRead due to decoder aggregating the payload.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        ctx.channel().attr(CHANNEL_SEND_KEEP_ALIVE).get().set(true);
        if (logger.isDebugEnabled()) {
            logger.debug("{}: batches pending: {}",
                    ctx.channel().id().asShortText(),
                    ctx.channel().attr(CHANNEL_SEND_KEEP_ALIVE).get().get());
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        IdleStateEvent e;
        if (evt instanceof IdleStateEvent) {
            e = (IdleStateEvent) evt;
            if (e.state() == IdleState.WRITER_IDLE) {
                if (sendKeepAlive(ctx)) {
                    ChannelFuture f = ctx
                            .writeAndFlush(new Ack(Protocol.VERSION_2, 0));
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}: sending keep alive ack to libbeat",
                                ctx.channel().id().asShortText());
                        f.addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                logger.trace("{}: acking was successful",
                                        ctx.channel().id().asShortText());
                            } else {
                                logger.trace("{}: acking failed",
                                        ctx.channel().id().asShortText());
                            }
                        });
                    }
                }
            } else if (e.state() == IdleState.ALL_IDLE) {
                logger.debug(
                        "{}: reader and writer are idle, closing remote connection",
                        ctx.channel().id().asShortText());
                ctx.flush();
                ChannelFuture f = ctx.close();
                if (logger.isTraceEnabled()) {
                    f.addListener((future) -> {
                        if (future.isSuccess()) {
                            logger.trace("closed ctx successfully");
                        } else {
                            logger.trace("could not close ctx");
                        }
                    });
                }
            }
        }
    }

    /**
     * Determine if this channel has finished processing it's payload. If it has
     * not, send a TCP keep alive. Note - for this to work, the following must
     * be true:
     * <ul>
     * <li>This Handler comes before the {@link BeatsHandler} in the channel's
     * pipeline</li>
     * <li>This Handler is associated to an
     * {@link io.netty.channel.EventLoopGroup} that has guarantees that the
     * associated {@link io.netty.channel.EventLoop} will never block.</li>
     * <li>The {@link BeatsHandler} un-sets only after it has processed this
     * channel's payload.</li>
     * </ul>
     * 
     * @param ctx
     *            the {@link ChannelHandlerContext} used to curry the flag.
     * @return Returns true if this channel/connection has NOT finished
     *         processing it's payload. False otherwise.
     */
    public boolean sendKeepAlive(ChannelHandlerContext ctx) {
        return ctx.channel().hasAttr(CHANNEL_SEND_KEEP_ALIVE)
                && ctx.channel().attr(CHANNEL_SEND_KEEP_ALIVE).get().get();
    }
}