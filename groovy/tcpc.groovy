#!/usr/bin/env groovy
/**
 * TCP Client
 */

def sendText(String message) {
    if (message.endsWith('\\n')) { // new line
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

def repl(BufferedReader reader) {
    while (true) {
        print "> "
        def line = reader.readLine()
        if (line == null)
            break
        try {
            eval(line)
        } catch (Throwable e) {
            println e
        }
    }
}

def eval(String line) {
    if (line.trim().isEmpty() || line[0] == '#') // comment
        return
    binaryMode ? sendBinary(line) : sendText(line)
}


def cli = new CliBuilder(usage: './tcpc.groovy [options] <host> <port>')
cli.with {
    b 'binary mode'
    f args:1, argName:'file', 'a send message file'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.arguments().size() < 2) {
    cli.usage()
    System.exit(1)
}
if (opt.h) {
    cli.usage()
    System.exit(0)
}
binaryMode = opt.b
def reader = opt.f ? new File(opt.f).newReader() : System.in.newReader()
def host = opt.arguments()[0]
def port = opt.arguments()[1] as int
socket = new Socket(host, port)
repl(reader)
socket.close()
