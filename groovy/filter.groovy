#!/usr/bin/env groovy
/**
 * Lines Filter
 */

import java.util.regex.Pattern

def cli = new CliBuilder(usage: './filter.groovy <word> <file>')
cli.with {
    d args:1, argName:'delimiter', 'default delimiter is ","'
    n 'select non-matching lines'
    h longOpt:'help', 'print this message'
}
def opt = cli.parse(args)
if (opt.h) {
    cli.usage()
    System.exit(0)
}
if (opt.arguments().size() < 2) {
    cli.usage()
    System.exit(1)
}

def delimiter = opt.d ?: ','
def match = !opt.n
def words = opt.arguments()[0].split(delimiter)
def file = new File(opt.arguments()[1])
System.out << file.filterLine { line ->
    def contained = false
    for (word in words) {
        contained |= line.contains(word)
    }
    return contained == match
}
