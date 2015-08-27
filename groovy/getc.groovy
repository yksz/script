#!/usr/bin/env groovy
/**
 * HTTP GET Client
 */

def get(url, redirects) {
    def conn = new URL(url).openConnection()
    conn.setRequestMethod("GET")
    conn.setInstanceFollowRedirects(redirects)
    conn.connect()
    println "[ HTTP Headers ]"
    conn.getHeaderFields().each { key, value ->
        print key ? "$key: " : ""
        println value.join("; ")
    }
    println ""
    println "[ HTTP Body ]"
    def charset = getCharset(conn.getContentType())
    conn.getInputStream().newReader(charset).eachLine {
        println it
    }
    conn.disconnect()
}

def getCharset(contentType, defaultValue="utf-8") {
    for (s in contentType?.tokenize(" ;"))
        if (s.startsWith("charset="))
            return s.substring("charset=".length())
    return defaultValue
}


def cli = new CliBuilder(usage: './getc.groovy [options] <url>')
cli.with {
    r 'auto redirect'
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
get(opt.arguments()[0], opt.r)
