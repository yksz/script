#!/usr/bin/env groovy
// POST request for http

def postRequest(url, params) {
    def conn = new URL(url).openConnection()
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setUseCaches(false);
    conn.setInstanceFollowRedirects(false)
    conn.connect()
    def writer = new PrintWriter(conn.getOutputStream())
    writer.print params
    writer.flush()
    writer.close()
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


def cli = new CliBuilder(usage: 'post.groovy [-options] URL')
cli.with {
    p args:1, 'post parameters'
    f args:1, 'a file on post parameters'
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
