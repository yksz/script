globalenv = [
    "exit": { System.exit(0) },
    "+": { x, y -> x + y },
    "-": { x, y -> x - y },
    "*": { x, y -> x * y },
    "/": { x, y -> x / y },
    "<": { x, y -> x < y },
    ">": { x, y -> x > y },
    "<=": { x, y -> x <= y },
    ">=": { x, y -> x >= y },
    "eq": { x, y -> x == y },
    "atom": { x -> !(x instanceof List) },
    "car": { x -> x[0] },
    "cdr": { x -> x[1..-1] },
    "cons": { x, y -> [x, y] }
]

def eval(x, env = globalenv) {
    if (x instanceof String) { // symbol
        return env[x]
    } else if (x instanceof List) {
        if (x[0] == "if") {
            def (_, when, then, els) = x
            return eval((eval(when, env) ? then : els), env)
        } else if (x[0] == "quote") {
            def (_, expr) = x
            return expr
        } else if (x[0] == "lambda") {
            def (_, vars, expr) = x
            return { ... args ->
                def list = [vars, args].transpose()
                def map = list.collectEntries{ it }
                env.putAll(map)
                eval(expr, env)
            }
        } else if (x[0] == "define") {
            def (_, var, expr) = x
            env[var] = eval(expr, env)
        } else {
            def exprs = x.collect { eval(it, env) }
            def proc = exprs.remove(0)
            return proc.call(*exprs)
        }
    } else { // literal
        return x
    }
}

def parse(tokens) {
    if (tokens.isEmpty())
        throw new Exception("parse error: unexpected EOF")
    def token = tokens.remove(0)
    if (token == "(") {
        return parseList(tokens)
    } else if (token == ")") {
        throw new Exception("parse error: unexpected ')'")
    } else {
        return parseAtom(token)
    }
}

def parseList(tokens) {
    def list = []
    while (tokens[0] != ")") {
        list.add(parse(tokens))
        if (tokens.isEmpty())
            throw new Exception("parse error: ')' is not found")
    }
    tokens.remove(0) // pop off ')'
    return list
}

def parseAtom(token) {
    try {
        return Integer.parseInt(token)
    } catch (NumberFormatException e) {
        return token // symbol
    }
}

def tokenize(str) {
    return str.replace("(", " ( ").replace(")", " ) ").trim().split("\\s+").toList()
}

def repl() {
    def reader = new BufferedReader(new InputStreamReader(System.in))
    while (true) {
        print("lisp> ")
        def line = reader.readLine();
        try {
            println(eval(parse(tokenize(line))))
        } catch (Throwable e) {
            println(e)
        }
    }
}


repl()
