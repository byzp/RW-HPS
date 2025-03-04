/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.net.core.AbstractNet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.error
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import net.udp.ReliableServerSocket
import net.udp.ReliableSocket
import java.net.BindException
import java.net.ServerSocket

/**
 * @author Dr
 * 遵守NetGameServer服务对外最少开放接口,尽量内部整合
 */
class StartNet {
    private val connectChannel = Seq<Channel>(4)
    private var serverSocket: ServerSocket? = null
    private var startGameNetUdp: StartGameNetUdp? = null
    private val start: AbstractNet

    @JvmField
    val OVER_MAP = OrderedMap<String, TypeConnect>(16)

    internal val ioGroup: EventExecutorGroup = DefaultEventExecutorGroup(32)

    constructor() {
        start = StartGameNetTcp(this)
    }

    constructor(abstractNet: AbstractNet) {
        start = abstractNet
    }

    /**
     * 在指定端口启动Game Server
     * @param port 端口
     */
    fun openPort(port: Int) {
        Data.config.RunPid = Data.core.pid
        Data.config.save()

        openPort(port,1,0)
    }

    /**
     * 在指定端口范围启动Game Server
     * @param port 主端口
     * @param startPort Start Port
     * @param endPort End Port
     */
    fun openPort(port: Int,startPort:Int,endPort:Int) {
        clog(Data.localeUtil.getinput("server.start.open"))
        val bossGroup: EventLoopGroup = getEventLoopGroup(4)
        val workerGroup: EventLoopGroup = getEventLoopGroup()
        val runClass: Class<out ServerChannel>

        if (Epoll.isAvailable()) {
            runClass = EpollServerSocketChannel::class.java
        } else {
            runClass = NioServerSocketChannel::class.java
            clog("无法使用Epool 效率可能略低")
        }
        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup)
                .channel(runClass)
                //.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(start)
            clog(Data.localeUtil.getinput("server.start.openPort"))
            val channelFutureTcp = serverBootstrapTcp.bind(port)

            for (i in startPort..endPort) {
                serverBootstrapTcp.bind(i)
            }

            val start = channelFutureTcp.channel()
            connectChannel.add(start)
            clog(Data.localeUtil.getinput("server.start.end"))
            start.closeFuture().sync()
        } catch (e: InterruptedException) {
            error("[TCP Start Error]", e)
        } catch (bindError: BindException) {
            error("[Port Bind Error]", bindError)
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    fun startUdp(port: Int) {
        startGameNetUdp = StartGameNetUdp(this)
        try {
            ReliableServerSocket(port).use { serverSocket ->
                this.serverSocket = serverSocket
                do {
                    val socket = serverSocket.accept() as ReliableSocket
                    startGameNetUdp!!.run(socket as ReliableServerSocket.ReliableClientSocket)
                } while (true)
            }
        } catch (ignored: Exception) {
            error("UDP Start Error", ignored)
        }
    }

    fun getConnectSize(): Int {
        return start.getConnectSize()
    }

    fun stop() {
        connectChannel.each { obj: Channel -> obj.close() }
    }

    fun clear(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        try {
            val attr = channel.attr(NewServerHandler.NETTY_CHANNEL_KEY)
            val con = attr.get()
            if (con != null) {
                con.abstractNetConnect.disconnect()
            } else {
                channel.close()
                ctx.close()
            }
        } finally {
            OVER_MAP.remove(channel.id().asLongText())
        }
    }

    private fun getEventLoopGroup(size: Int = 0): EventLoopGroup {
        return if (Epoll.isAvailable()) {
            EpollEventLoopGroup(size)
        } else {
            NioEventLoopGroup(size)
        }
    }
}