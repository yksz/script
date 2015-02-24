#!/usr/bin/env groovy
/**
 * SQL Scripts Runner
 */

@GrabConfig(systemClassLoader=true)
@Grapes([
    @Grab(group='com.h2database', module='h2', version='1.4.185'),
    @Grab(group='org.apache.derby', module='derby', version='10.11.1.1'),
    @Grab(group='org.apache.derby', module='derbyclient', version='10.11.1.1')
])
import groovy.sql.Sql

def newSql(db) {
    println "# connect to ${db.url}"
    return Sql.newInstance(db.url, db.user, db.password, db.driver)
}

def runSqlScript(sql, scriptPath, cnt) {
    executeSql(sql, parseSqlScript(scriptPath), cnt)
}

def parseSqlScript(scriptPath) {
    def script = new File(scriptPath).text
    script = script.replaceAll(/--.*/, '') // remove comment
    script = removeBlankLine(script)
    def statements = []
    script.split(/;\s*/).each {
        if (!it.matches(/\s+/))
            statements << it
    }
    return statements
}

def removeBlankLine(text) {
    def sb = new StringBuilder()
    text.eachLine {
        if (it != '')
            sb.append("$it\n")
    }
    return sb.toString()
}

def executeSql(sql, statements, cnt) {
    for (i in 0..<cnt) {
        if (i == 1) println "# ...and repeat ${cnt - 1} times"
        statements.each { stmt ->
            if (i == 0) println "# execute sql: $stmt"
            if (stmt.trim().toLowerCase().startsWith('select ')) {
                sql.eachRow(stmt) { row ->
                    println row
                }
            } else {
                sql.execute(stmt)
            }
        }
    }
}


def cli = new CliBuilder(usage: './sql.groovy [options] <files> | <sql statements>')
cli.with {
    _ longOpt:'prop', args:1, argName:'file', 'jdbc properties file'
    _ longOpt:'driver', args:1, 'jdbc driver'
    _ longOpt:'url', args:1, 'jdbc url'
    _ longOpt:'user', args:1, 'jdbc user'
    _ longOpt:'password', args:1, 'jdbc password'
    c args:1, argName:'count', 'a repeat count'
    s 'execute sql statements'
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
def cnt = (opt.c ?: 1) as int
def sql = newSql(loadDbConfig(opt))
def start = System.currentTimeMillis()
if (opt.s) {
    executeSql(sql, opt.arguments(), cnt)
} else {
    opt.arguments().each { arg ->
        runSqlScript(sql, arg, cnt)
    }
}
def stop = System.currentTimeMillis()
println "# ${(stop - start) / 1000} [sec]"

def loadDbConfig(opt) {
    def db = new Properties()
    // default
    db.driver = 'org.h2.Driver'
    db.url = 'jdbc:h2:mem:'
    db.user = 'sa'
    db.password = ''
    // file
    if (opt.prop) {
        new File(opt.prop).withInputStream { db.load(it) }
    }
    // command option
    db.driver = opt.driver ?: db.driver
    db.url = opt.url ?: db.url
    db.user = opt.user ?: db.user
    db.password = opt.password ?: db.password
    return db
}
