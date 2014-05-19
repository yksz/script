/**
 * Simple web server
 *
 * invoke using
 *    groovy -l 80 web.groovy
 *       (where 80 is the port to listen for requests upon)
 */

if (init) {
    headers = [:]
    binaryTypes = ["gif", "jpg", "png"]
    mimeTypes = [
        "css" : "text/css",
        "gif" : "image/gif",
        "htm" : "text/html",
        "html": "text/html",
        "jpg" : "image/jpeg",
        "png" : "image/png"
    ]
}

if (line.isEmpty()) {
    processRequest()
    return "success"
} else if (line.startsWith("GET") || line.startsWith("POST")) {
    path = line.tokenize()[1]
} else {
    h = line.tokenize(" :")
    headers[h[0]] = h[1]
}

def processRequest() {
    if (path.indexOf("..") != -1) //simplistic security
        return
    path = URLDecoder.decode(path, "utf-8")
    file = new File("." + path)
    if (!file.exists()) {
        printHeaders("404 Not Found", "text/html")
        println "<html><head><h1>404 Not Found</h1></head><body>"
    } else if (file.isDirectory()) {
        printDirectoryListing(file)
    } else {
        extension = path.substring(path.lastIndexOf(".") + 1)
        printHeaders("200 OK", mimeTypes.get(extension, "text/plain"))
        if (binaryTypes.contains(extension)) {
            socket.getOutputStream().write(file.readBytes())
        } else {
            println(file.text)
        }
    }
}

def printDirectoryListing(dir) {
    printHeaders("200 OK", "text/html")
    println "<html><head></head><body>"
    for (filename in dir.list().toList().sort()) {
        if (path == "/")
            path = ""
        path = URLEncoder.encode(path, "utf-8")
        println "<a href='${path}/${filename}'>${filename}</a><br>"
    }
    println "</body></html>"
}

def printHeaders(status, mimeType) {
    println "HTTP/1.0 ${status}"
    println "Content-Type: ${mimeType}"
    println ""
    out.flush()
}
