#!/usr/bin/env groovy
/**
 * HTTP Echo Server
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
            while (serve(id, input, output)) {}
        } catch (IOException e) {
            System.err.println "id=$id: $e"
        }
        input.close()
        output.close()
        println "id=$id: disconnected"
    }

    def serve(id, input, output) {}
}

class HttpServer extends Server {
    class Request {
        def line
        def header
        def input
    }

    class Response {
        def output
    }

    class DefaultHandler {
        def handle(request, response) {
            def buf = new StringBuilder()
            buf.append "HTTP/1.1 200 OK\n"
            buf.append "Content-Length: 0\n"
            buf.append "\n"
            response.output.write buf.toString().getBytes()
            response.output.flush()
        }
    }

    def handler = new DefaultHandler()

    def serve(id, input, output) {
        def line = readLine(input)
        if (line?.startsWith("GET") || line?.startsWith("POST")) {
            def protocol = line.tokenize()[2]
            def header = parseHeader(input)
            def keepAlive = getKeepAlive(protocol, header)
            def request = new Request(line: line, header: header, input: input)
            def response = new Response(output: output)
            handler.handle(id, request, response)
            return keepAlive
        }
        return false
    }

    def readLine(input) {
        def buf = new StringBuilder()
        while (true) {
            int c = input.read()
            if (c == -1)
                return buf.length() > 0 ? buf.toString() : null
            else if ((c == "\r" && input.read() == "\n") || c == "\n")
                return buf.toString()
            else
                buf.append((char) c)
        }
    }

    def parseHeader(input) {
        def header = [:]
        while (input.available() > 0) {
            def line = readLine(input)
            if (line == null || line.isEmpty())
                break
            def s = line.tokenize(" :")
            header[s[0]] = s[1]
        }
        return header
    }

    def getKeepAlive(protocol, header) {
        def v = header["Connection"]
        if (protocol == "HTTP/1.0") {
            return v ? v.equalsIgnoreCase("keep-alive") : false
        } else if (protocol == "HTTP/1.1") {
            return v ? !v.equalsIgnoreCase("close") : true
        } else {
            return false
        }
    }
}

class EchoHandler {
    def saveDir

    def handle(id, request, response) {
        def content = restoreRequest(request)
        writeResponse(response.output, content)
        if (saveDir)
            saveRequest(id, content, saveDir)
    }

    def restoreRequest(request) {
        def len = getContentLength(request.header)
        def body = readRequestBody(request.input, len)
        def buf = new ByteArrayOutputStream()
        buf.write "${request.line}\n".getBytes()
        request.header.each {
            buf.write "${it.key}: ${it.value}\n".getBytes()
        }
        buf.write "\n".getBytes()
        buf.write body
        buf.flush()
        return buf.toByteArray()
    }

    def getContentLength(header) {
        def v = header["Content-Length"]
        return (v ?: 0) as int
    }

    def readRequestBody(input, len) {
        def body = new byte[len]
        input.read body
        return body
    }

    def writeResponse(output, content) {
        writeHeader(output, content.length)
        writeBody(output, content)
    }

    def writeHeader(output, len) {
        def buf = new StringBuilder()
        buf.append "HTTP/1.1 200 OK\n"
        buf.append "Content-Length: $len\n"
        buf.append "Content-Type: text/plain; charset=utf-8\n"
        buf.append "\n"
        output.write buf.toString().getBytes()
        output.flush()
    }

    def writeBody(output, content) {
        output.write content
        output.flush()
    }

    static def saveRequest(id, content, saveDir) {
        def filepath = "${saveDir.name}/request${id}.txt"
        println "id=$id: save a request into $filepath"
        new File(filepath).append content
    }
}


def cli = new CliBuilder(usage: './echos.groovy [options]')
cli.with {
    p args:1, 'port'
    s 'save a request into a file'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
def port = (opt.p ?: 8080) as int
def saveDir
if (opt.s) {
    saveDir = new File('requests')
    saveDir.deleteDir()
    saveDir.mkdirs()
}
def server = new HttpServer(port: port)
server.handler = new EchoHandler(saveDir: saveDir)
server.run()
