#!/usr/bin/env groovy
/**
 * TCP Client
 */

def send(String message) {
    def data = message.getBytes()
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
    send(line)
}


if (args.length < 2) {
    println 'usage: ./tcpc.groovy <host> <port>'
    System.exit(1)
}
def host = args[0]
def port = args[1] as int
socket = new Socket(host, port)
repl()
socket.close()
