package com.jetbrains.tuna.oauth

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec

class ServerInitializer : ChannelInitializer<SocketChannel>() {
  private val serverHandler = ServerHandler()

  override fun initChannel(ch: SocketChannel) {
    ch.pipeline().apply {
      addLast(HttpServerCodec())
      addLast(HttpObjectAggregator(MAX_HTTP_CONTENT_SIZE))
      addLast(serverHandler)
    }
  }

  fun getCode(): String = serverHandler.getCode()

  companion object {
    const val MAX_HTTP_CONTENT_SIZE = 1 * 1024 * 1024
  }
}