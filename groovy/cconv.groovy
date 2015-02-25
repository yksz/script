#!/usr/bin/env groovy
/**
 * Charset Converter
 */

import java.nio.charset.Charset

def convertCharset(from, to, srcPath) {
    def srcFile = new File(srcPath)
    if (!srcFile.exists()) {
        println "no such file: $srcPath"
        return
    }
    if (srcFile.isDirectory() || isBinaryFile(srcFile))
        return
    println srcPath
    def dstFile = new File("${srcPath}.tmp")
    convertCharset(from, to, srcFile, dstFile)
    moveFile(dstFile, srcFile)
}

def convertCharset(from, to, srcFile, dstFile) {
    srcFile.withReader(from) { reader ->
        dstFile.withPrintWriter(to) { writer ->
            for (line in reader)
                writer.println line
        }
    }
}

def isBinaryFile(file) {
    file.withInputStream { input ->
        for (b in input)
            if (b == 0)
                return true
        return false
    }
}

def moveFile(srcFile, dstFile) {
    def ok = srcFile.renameTo(dstFile)
    if (!ok) {
        copyFile(srcFile, dstFile)
        srcFile.delete()
    }
}

def copyFile(srcFile, dstFile) {
    def input = srcFile.newInputStream()
    def output = dstFile.newOutputStream()
    output << input
    input.close()
    output.close()
}

def printAvailableCharsets() {
    Charset.availableCharsets().each {
        println it.key
    }
}


def cli = new CliBuilder(usage: './cconv.groovy [options] <file> | <regexp>\n'
        + 'e.g. ./cconv.groovy -f euc-jp -t utf-8 "/.*\\.txt/"')
cli.with {
    f args:1, argName:'charset', 'from encording'
    t args:1, argName:'charset', 'to encording'
    l 'print lists of available charsets'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
if (opt.l) {
    printAvailableCharsets()
    System.exit(0)
}
if (!opt.f || !opt.t) {
    println 'error: Missing required options: f, t'
    cli.usage()
    System.exit(1)
}
def from = opt.f
def to = opt.t
opt.arguments().each { arg ->
    if (arg ==~ "^/.*/\$") {
        def regexp = arg[1..<-1]
        new File(".").eachFileRecurse { file ->
            if (file.name =~ regexp)
                convertCharset(from, to, file.getCanonicalPath())
        }
    } else {
        convertCharset(from, to, arg)
    }
}
