#!/usr/bin/env groovy
/**
 * UDP Client
 */

def send(String message) {
    def data = message.getBytes()
    def packet = new DatagramPacket(data, data.length, address)
    def socket = new DatagramSocket()
    socket.send(packet)
    socket.close()
}

def repl() {
    def reader = System.in.newReader()
    while (true) {
        print "> "
        def line = reader.readLine()
        try {
            eval(line)
        } catch (Throwable e) {
            System.err.println e
        }
    }
}

def eval(String line) {
    if (line == null || line.trim().isEmpty())
        return
    send(line)
}


if (args.length < 2) {
    println 'usage: ./udpc.groovy <host> <port>'
    System.exit(1)
}
def host = args[0]
def port = args[1] as int
address = new InetSocketAddress(host, port)
repl()
