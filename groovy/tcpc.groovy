#!/usr/bin/env groovy
/**
 * TCP Client
 */

import java.nio.ByteBuffer

def sendText(String message) {
    if (message.endsWith('\\n')) {
        message = replaceLast(message, '\\n', '\n')
    }
    def data = message.getBytes()
    socket.outputStream.write(data);
}

def replaceLast(String str, String target, String replacement) {
    def sb = new StringBuilder(str)
    def lastIndex = str.lastIndexOf(target)
    if (lastIndex == -1)
        return str
    sb.replace(lastIndex, lastIndex + target.size(), replacement)
    return sb.toString()
}

def sendBinary(String message) {
    def data = Integer.decode(message) as byte
    socket.outputStream.write(data);
}

def repl() {
    def reader = System.in.newReader()
    while (true) {
        print "> "
        def line = reader.readLine()
        try {
            eval(line)
        } catch (Throwable e) {
            println e
        }
    }
}

def eval(String line) {
    if (line == null || line.trim().isEmpty())
        return
    isBinaryMode ? sendBinary(line) : sendText(line)
}


def cli = new CliBuilder(usage: './tcpc.groovy [options] <host> <port>')
cli.with {
    b 'binary mode'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}

isBinaryMode = opt.b
def host = opt.arguments()[0]
def port = opt.arguments()[1] as int
socket = new Socket(host, port)
repl()
socket.close()
