#!/usr/bin/env groovy
// echo server for http

class ConnThread extends Thread {

    def cid
    def socket
    def input, output

    ConnThread(id, socket) {
        this.cid = id
        this.socket = socket
    }

    void run() {
        println "connected: id=${cid}, ip=${socket.getInetAddress()}"
        input = new BufferedInputStream(socket.getInputStream())
        output = new BufferedOutputStream(socket.getOutputStream())
        while (true) {
            if (!process())
                break
        }
        input.close()
        output.close()
        socket.close()
        println "disconnected: id=${cid}"
    }

    def process() {
        def method = readLine()
        if (method?.startsWith('GET') || method?.startsWith('POST')) {
            def headers = readHeader()
            def keepAlive = getKeepAlive(headers)
            def len = getContentLength(headers)
            def body = readBody(len)
            def bytes = makeResponse(method, headers, body)
            writeResponse(bytes)
            return keepAlive
        }
        return false
    }

    def readLine() {
        def buf = new StringBuilder()
        while (true) {
            int c = input.read()
            if (c == -1 || (c == '\r' && input.read() == '\n') || c == '\n')
                return buf.length() > 0 ? buf.toString() : null
            else
                buf.append((char) c)
        }
    }

    def readHeader() {
        def headers = [:]
        while (input.available() > 0) {
            def line = readLine()
            if (line == null || line.trim().isEmpty())
                break
            def s = line.split(':')
            if (s.length > 1)
                headers[s[0]] = s[1].trim().toLowerCase()
            else
                headers[null] = s[0]
        }
        return headers
    }

    def getKeepAlive(headers) {
        def v = headers['Connection']
        return v != 'close'
    }

    def getContentLength(headers) {
        def v = headers['Content-Length']
        return v != null ? v as int : 0
    }

    def readBody(len) {
        def body = new byte[len];
        input.read body
        return body
    }

    def makeResponse(method, headers, body) {
        def buf = new ByteArrayOutputStream()
        buf.write method.getBytes()
        headers.each {
            buf.write "${it.key}: ${it.value}\n".getBytes()
        }
        buf.write '\n'.getBytes()
        buf.write body
        buf.flush()
        return buf.toByteArray()
    }

    def writeResponse(bytes) {
        writeHeader(bytes.length)
        writeBody(bytes)
    }

    def writeHeader(len) {
        def buf = new StringBuilder()
        buf.append 'HTTP/1.1 200 OK\n'
        buf.append 'Content-Type: text/plain; charset=utf-8\n'
        buf.append "Content-Length: ${len}\n"
        buf.append 'Connection: close\n'
        buf.append '\n'
        output.write buf.toString().getBytes()
        output.flush()
    }

    def writeBody(bytes) {
        output.write bytes
        output.flush()
    }

}

def runServer(port) {
    def server = new ServerSocket(port)
    println "the http server started: port=$port"
    int id = 0
    while (true) {
        def socket = server.accept()
        connThread = new ConnThread(id++, socket)
        connThread.start()
    }
}


def cli = new CliBuilder(usage: 'echo.groovy [-options]')
cli.with {
    p args:1, 'port'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
def port = 8080
if (opt.p) {
    port = opt.p as int
}
runServer(port)
