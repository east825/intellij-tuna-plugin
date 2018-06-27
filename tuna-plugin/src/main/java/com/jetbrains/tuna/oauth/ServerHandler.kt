package com.jetbrains.tuna.oauth

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import org.apache.http.client.utils.URLEncodedUtils
import java.util.concurrent.SynchronousQueue

class ServerHandler : SimpleChannelInboundHandler<FullHttpMessage>() {
  private val codes = SynchronousQueue<String>()

  fun getCode(): String = codes.take()

  override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage) {
    when {
      msg is FullHttpRequest -> {
        val parameters = URLEncodedUtils.parse(msg.uri(), Charsets.UTF_8)
        parameters.find { it.name.endsWith("code") }?.let {
          codes.put(it.value)
        }

        ctx.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
      }
    }
  }

  override fun isSharable(): Boolean = true
}