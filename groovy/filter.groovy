#!/usr/bin/env groovy
/**
 * Lines Filter
 */

import java.util.regex.Pattern

def cli = new CliBuilder(usage: './filter.groovy <word> <file>')
cli.with {
    n longOpt:'not', 'select non-matching lines'
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

def file = new File(opt.arguments()[1])
def word = opt.arguments()[0]
def matched = !opt.n
System.out << file.filterLine { line ->
    line.contains(word) == matched
}
