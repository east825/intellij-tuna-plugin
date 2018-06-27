package com.jetbrains.tuna.oauth

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import java.util.concurrent.SynchronousQueue

class ClientHandler : SimpleChannelInboundHandler<FullHttpResponse>() {
  private val queue = SynchronousQueue<FullHttpResponse>()

  public override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) {
    queue.put(msg.copy())
  }

  fun getResponse(): FullHttpResponse = queue.take()
}