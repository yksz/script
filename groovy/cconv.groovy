import java.nio.charset.Charset

def convertCharset(from, to, src) {
    def dst = src + ".tmp"
    convertCharset(from, to, src, dst)
    moveFile(dst, src)
}

def convertCharset(from, to, src, dst) {
    def input  = new FileInputStream(src)
    def output = new FileOutputStream(dst)
    def reader = new InputStreamReader(input, from)
    def writer = new OutputStreamWriter(output, to)
    try {
        for (def c; (c = reader.read()) != -1;)
            writer.write(c)
        writer.flush()
    } finally {
        reader.close()
        writer.close()
    }
}

def moveFile(src, dst) {
    def srcFile = new File(src)
    def ok = srcFile.renameTo(dst)
    if (!ok) {
        copyFile(src, dst)
        srcFile.delete()
    }
}

def copyFile(src, dst) {
    def input  = new FileInputStream(src)
    def output = new FileOutputStream(dst)
    copy(input, output)
}

def copy(input, output) {
    def buf = new byte[1024]
    try {
        for (def len; (len = input.read(buf)) != -1;)
            output.write(buf, 0, len)
        output.flush();
    } finally {
        input.close()
        output.close()
    }
}

def isBinaryFile(path) {
    def input = new FileInputStream(path)
    try {
        for (def b; (b = input.read()) != -1;)
            if (b == 0)
                return true
        return false
    } finally {
        input.close()
    }
}

def printAvailableCharsets() {
    def map = Charset.availableCharsets()
    for (name in map.keySet())
        println name
}


def cli = new CliBuilder(usage: 'cconv - charset converter')
cli.f(args:1, 'from encording')
cli.t(args:1, 'to encording')
cli.s(args:1, 'search for files by path or regexp(e.g. "/.*\\.txt/")')
cli.l('print lists of available charsets')
cli.h(longOpt:'help', 'print this message')
def opt = cli.parse(args)
if (!opt) {
    System.exit(1)
}
if (opt.h) {
    cli.usage()
    System.exit(0)
}
if (opt.l) {
    printAvailableCharsets()
    System.exit(0)
}
if (!opt.f || !opt.t || !opt.s) {
    println 'error: Missing required options: f, t, s'
    cli.usage()
    System.exit(1)
}
def from = opt.f
def to = opt.t
if (opt.s ==~ '^/.*/$') {
    def regexp = opt.s.substring(1, opt.s.length() - 1)
    new File('.').eachFileRecurse {
        if (it.isFile() && !isBinaryFile(it) && it.name =~ regexp) {
            def src = it.getCanonicalPath()
            println src
            convertCharset(from, to, src)
        }
    }
} else {
    def src = opt.s
    if (!new File(src).exists()) {
        System.err.println 'no such file: ' + src
        System.exit(1)
    }
    println src
    convertCharset(from, to, src)
}
