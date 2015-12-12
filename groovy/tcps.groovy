#!/usr/bin/env groovy
/**
 * TCP Server
 */

import java.util.concurrent.atomic.AtomicInteger

abstract class Server {
    def port = 8080

    def run() {
        def serverSocket = new ServerSocket(port)
        println "this server is listening on port $port"
        def id = new AtomicInteger(0)
        while (true) {
            serverSocket.accept { socket ->
                processConnection(id.getAndIncrement(), socket)
            }
        }
    }

    def processConnection(id, socket) {
        println "id=$id: connected [ip=${socket.getInetAddress()}]"
        def input = new BufferedInputStream(socket.getInputStream())
        def output = new BufferedOutputStream(socket.getOutputStream())
        try {
            while (serve(input, output)) {}
        } catch (IOException e) {
            println "id=$id: $e"
        }
        input.close()
        output.close()
        println "id=$id: disconnected"
    }

    def serve(input, output) {}
}

class TCPServer extends Server {
    def serve(input, output) {
        def line = readLine(input)
        if (line == null) {
            return false
        }
        println line
        return true
    }

    def readLine(input) {
        def buf = new StringBuilder()
        while (true) {
            int c = input.read()
            if (c == -1)
                return buf.length() > 0 ? buf.toString() : null
            else if (c == "\n")
                return buf.toString()
            else
                buf.append((char) c)
        }
    }
}


def cli = new CliBuilder(usage: './tcps.groovy [options]')
cli.with {
    p args:1, 'port'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
def port = (opt.p ?: 8080) as int
def server = new TCPServer(port: port)
server.run()
