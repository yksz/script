#!/usr/bin/env groovy
/**
 * HTTP stub server
 */

abstract class Server {
    def port = 8080

    def run() {
        def serverSocket = new ServerSocket(port)
        println "this server is listening on port $port"
        def id = 0
        while (true) {
            def socket = serverSocket.accept()
            Thread.start {
                processConnection(id++, socket)
            }
        }
    }

    def processConnection(id, socket) {
        println "connected: id=$id, ip=${socket.getInetAddress()}"
        def input = new BufferedInputStream(socket.getInputStream())
        def output = new BufferedOutputStream(socket.getOutputStream())
        while (serve(input, output)) {}
        input.close()
        output.close()
        socket.close()
        println "disconnected: id=$id"
    }

    def serve(input, output) {}
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

    def serve(input, output) {
        def line = readLine(input)
        if (line?.startsWith("GET") || line?.startsWith("POST")) {
            def header = parseHeader(input)
            def keepAlive = getKeepAlive(header)
            def request = new Request(line: line, header: header, input: input)
            def response = new Response(output: output)
            handler.handle(request, response)
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

    def getKeepAlive(header) {
        def v = header["Connection"]
        return !v.equalsIgnoreCase("close")
    }
}

class StubHandler {
    static def mimeTypes = [
        "html" : "text/html",
        "xml"  : "application/xml",
        "json" : "application/json",
    ]

    def filename = ""

    def handle(request, response) {
        def extension = filename.substring(filename.lastIndexOf(".") + 1)
        def mimeType = mimeTypes.get(extension, "text/plain")
        def content = new File(filename).getText().getBytes()
        writeResponse(response.output, mimeType, content)
    }

    def writeResponse(output, mimeType, content) {
        writeHeader(output, content.length, mimeType)
        writeBody(output, content)
    }

    def writeHeader(output, len, mimeType) {
        def buf = new StringBuilder()
        buf.append "HTTP/1.1 200 OK\n"
        buf.append "Content-Length: $len\n"
        buf.append "Connection: close\n"
        buf.append "Content-Type: $mimeType; charset=utf-8\n"
        buf.append "\n"
        output.write buf.toString().getBytes()
        output.flush()
    }

    def writeBody(output, content) {
        output.write content
        output.flush()
    }
}


def cli = new CliBuilder(usage: 'stub.groovy [options] <response-file>')
cli.with {
    p args:1, 'port'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
if (opt.arguments().size() < 1) {
    cli.usage()
    System.exit(1)
}
def port = (opt.p ?: 8080) as int
def server = new HttpServer(port: port)
server.handler = new StubHandler(filename: opt.arguments()[0])
server.run()