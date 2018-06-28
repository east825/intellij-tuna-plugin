package com.jetbrains.tuna.oauth

import com.intellij.util.concurrency.FutureResult
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import org.apache.http.client.utils.URLEncodedUtils
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class ServerHandler : SimpleChannelInboundHandler<FullHttpMessage>() {
  private val codes = FutureResult<String>()

  fun getCode(): String {
    return codes.get(30, TimeUnit.SECONDS)
  }

  override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage) {
    when {
      msg is FullHttpRequest -> {
        val parameters = URLEncodedUtils.parse(msg.uri(), Charsets.UTF_8)
        parameters.find { it.name.endsWith("code") }?.let {
          codes.set(it.value)
        }

        val message = Unpooled.copiedBuffer("Code accepted. Go back to the IDE.", StandardCharsets.UTF_8)
        ctx.writeAndFlush(DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, message))
      }
    }
  }

  override fun isSharable(): Boolean = true
}