#!/usr/bin/env groovy
// HTTP GET request

def getRequest(url) {
    def conn = new URL(url).openConnection()
    conn.setRequestMethod('GET')
    conn.setInstanceFollowRedirects(false);
    conn.connect()
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


if (args.length < 1) {
    println 'usage: get.groovy <url>'
    System.exit(1)
}
getRequest(args[0])
