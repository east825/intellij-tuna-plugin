package com.jetbrains.tuna.oauth

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.net.URI
import java.nio.charset.StandardCharsets


class ClientRequest {

  fun post(url: String, payload: String? = null): FullHttpResponse {
    val uri = URI(url)
    val scheme = if (uri.scheme == null) "http" else uri.scheme
    val host = if (uri.host == null) "127.0.0.1" else uri.host
    var port = uri.port
    if (port == -1) {
      if ("http".equals(scheme, ignoreCase = true)) {
        port = 80
      }
      else if ("https".equals(scheme, ignoreCase = true)) {
        port = 443
      }
    }

    if (!"http".equals(scheme, ignoreCase = true) && !"https".equals(scheme, ignoreCase = true)) {
      throw IllegalArgumentException("Only HTTP(S) is supported.")
    }

    // Configure SSL context if necessary.
    val ssl = "https".equals(scheme, ignoreCase = true)
    val sslCtx: SslContext?
    if (ssl) {
      sslCtx = SslContextBuilder.forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE).build()
    }
    else {
      sslCtx = null
    }

    // Configure the client.
    val group = NioEventLoopGroup()
    try {
      val clientInitializer = ClientInitializer(sslCtx)

      val b = Bootstrap()
      b.group(group)
        .channel(NioSocketChannel::class.java)
        .handler(clientInitializer)

      // Make the connection attempt.
      val ch = b.connect(host, port).sync().channel()

      // Prepare the HTTP request.
      val request = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.rawPath)
      request.headers().set(HttpHeaderNames.HOST, host)
      request.headers().add(HttpHeaderNames.CONTENT_TYPE, "application/x-www-form-urlencoded");
      val payloadBuffer = Unpooled.copiedBuffer(payload, StandardCharsets.UTF_8)
      request.headers().set(HttpHeaderNames.CONTENT_LENGTH, payloadBuffer.readableBytes())
      request.content().clear().writeBytes(payloadBuffer)

      // Send the HTTP request.
      ch.writeAndFlush(request)

      return clientInitializer.getResponse()
    }
    finally {
      // Shut down executor threads to exit.
      group.shutdownGracefully()
    }
  }
}