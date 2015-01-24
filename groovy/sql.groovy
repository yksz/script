#!/usr/bin/env groovy
/**
 * SQL Scripts Runner
 */

@GrabConfig(systemClassLoader=true)
@Grapes([
    @Grab(group='com.h2database', module='h2', version='1.4.185'),
    @Grab(group='org.apache.derby', module='derby', version='10.11.1.1')
])
import groovy.sql.Sql

def newSql(dbPropPath) {
    def db = new Properties()
    if (dbPropPath) {
        new File(dbPropPath).withInputStream { db.load(it) }
    } else {
        db.driver = 'org.h2.Driver'
        db.url = 'jdbc:h2:mem:'
        db.user = 'sa'
        db.password = ''
    }
    return Sql.newInstance(db.url, db.user, db.password, db.driver)
}

def runSqlScript(sql, scriptPath) {
    executeSql(sql, parseSqlScript(scriptPath))
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

def executeSql(sql, statements) {
    println '# execute sql with batch:'
    sql.withBatch(statements.size()) { stmt ->
        statements.each {
            println "$it\n"
            stmt.addBatch(it)
        }
    }
}


def cli = new CliBuilder(usage: './sql.groovy [options] <files>')
cli.with {
    _ longOpt:'db', args:1, argName:'file', 'a properties file of database'
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
def sql = newSql(opt.db)
opt.arguments().each { arg ->
    runSqlScript(sql, arg)
}
