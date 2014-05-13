#!/usr/bin/env groovy
// GET request for http

def getRequest(url) {
    def conn = new URL(url).openConnection()
    conn.setRequestMethod('GET')
    conn.setInstanceFollowRedirects(false);
    conn.connect()
    println '[ HTTP Headers ]'
    conn.getHeaderFields().each { e ->
        print e.key ? "${e.key}: " : ''
        println e.value.join('; ')
    }
    println ''
    println '[ HTTP Body ]'
    def body = new BufferedReader(new InputStreamReader(conn.getInputStream()))
    for (def line = null; (line = body.readLine()) != null;)
        println line
    body.close()
    conn.disconnect()
}


if (args.length < 1) {
    println 'usage: get.groovy URL'
    System.exit(1)
}
getRequest(args[0])
