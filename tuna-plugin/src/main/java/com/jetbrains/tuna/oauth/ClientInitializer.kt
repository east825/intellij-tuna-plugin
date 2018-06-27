package com.jetbrains.tuna.oauth

import com.jetbrains.tuna.oauth.ServerInitializer.Companion.MAX_HTTP_CONTENT_SIZE
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.ssl.SslContext

class ClientInitializer(private val sslCtx: SslContext?) : ChannelInitializer<SocketChannel>() {
  private val clientHandler = ClientHandler()

  public override fun initChannel(ch: SocketChannel) {
    val p = ch.pipeline()

    // Enable HTTPS if necessary.
    if (sslCtx != null) {
      p.addLast(sslCtx.newHandler(ch.alloc()))
    }

    p.addLast(HttpClientCodec())

    p.addLast(HttpObjectAggregator(MAX_HTTP_CONTENT_SIZE))

    p.addLast(clientHandler)
  }

  fun getResponse(): FullHttpResponse = clientHandler.getResponse()
}