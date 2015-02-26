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
import groovy.text.SimpleTemplateEngine

def newSql(db) {
    println "# connect to ${db.url}"
    return Sql.newInstance(db.url, db.user, db.password, db.driver)
}

def runSqlScript(sql, scriptPath, cnt) {
    executeSqlStatements(sql, parseSqlScript(scriptPath), cnt)
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

def executeSqlStatements(sql, statements, cnt) {
    def templates = convertTemplates(statements, '#', '$')
    for (i in 1..cnt) {
        withClock('# transaction:') {
            println "# begin transaction"
            sql.withTransaction {
                executeSqlTemplates(sql, templates, i)
            }
            println "# end transaction"
        }
    }
}

def executeSqlTemplates(sql, templates, i) {
    def binding = [i:i]
    templates.each { tmplt ->
        def stmt = tmplt.make(binding).toString()
        println "# execute sql: $stmt"
        withClock('#') {
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

def convertTemplates(strs, oldChar, newChar) {
    def engine = new SimpleTemplateEngine()
    def templates = []
    strs.each {
        def reader = new StringReader(it.replace(oldChar, newChar))
        templates << engine.createTemplate(reader)
    }
    return templates
}

def withClock(message, closure) {
    def start = System.currentTimeMillis()
    closure()
    def stop = System.currentTimeMillis()
    println "${message} ${stop - start} [ms]"
}


def cli = new CliBuilder(usage: './sql.groovy [options] <files> | <sql statements>\n'
        + 'e.g. ./sql.groovy -c 10 -s\n'
        + '"SELECT * FROM foo WHERE bar = \'bar#i\'"')
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
withClock('# total:') {
    if (opt.s) {
        executeSqlStatements(sql, opt.arguments(), cnt)
    } else {
        opt.arguments().each { arg ->
            runSqlScript(sql, arg, cnt)
        }
    }
}

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
