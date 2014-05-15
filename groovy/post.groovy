#!/usr/bin/env groovy
// HTTP POST request

def postRequest(url, params) {
    def conn = new URL(url).openConnection()
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setUseCaches(false);
    conn.setInstanceFollowRedirects(false)
    conn.connect()
    conn.getOutputStream().withPrintWriter {
        it.print params
    }
    println '[ HTTP Headers ]'
    conn.getHeaderFields().each { key, val ->
        print key ? "$key: " : ''
        println val.join('; ')
    }
    println ''
    println '[ HTTP Body ]'
    def charset = getCharset(conn.getContentType())
    conn.getInputStream().newReader(charset).eachLine {
        println it
    }
    conn.disconnect()
}

def getCharset(contentType, defaultValue='utf-8') {
    for (s in contentType?.split(';')) {
        s = s.trim()
        if (s.startsWith('charset='))
            return s.substring('charset='.length())
    }
    return defaultValue
}


def cli = new CliBuilder(usage: 'post.groovy [options] <url>')
cli.with {
    p args:1, argName:'params', 'post parameters'
    f args:1, argName:'filename', 'a file on post parameters'
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
def params
if (opt.p)
    params = opt.p
else if (opt.f)
    params = new File(opt.f).text
else
    params = ''
postRequest(opt.arguments()[0], params)
