package com.jetbrains.tuna.oauth

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LoggingHandler

class Server private constructor(port: Int) {
  private val bossGroup: NioEventLoopGroup = NioEventLoopGroup(1)
  private val workerGroup: NioEventLoopGroup = NioEventLoopGroup()

  private val serverInitializer = ServerInitializer()

  init {
    val b = ServerBootstrap();
    b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .handler(LoggingHandler())
            .childHandler(serverInitializer)

    b.bind(port).sync().channel()
  }

  fun getCode(): String = serverInitializer.getCode()

  fun shutdown() {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
  }

  companion object {
    fun start(port: Int): Server = Server(port)
  }
}