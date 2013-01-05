EnvLambda {
    var <env;
    var <func;
    *new {|env, func|
        ^super.new.init(env, func)
    }
    init {|at_callable, f|
        env = at_callable;
        func = f;
    }   
    at {|x|
        ^func.(this.env.at(x));
    }
    + {|other|
        ^EnvLambda(this, (_ + other));
    }
}

+ Env {
    + {|other|
        ^EnvLambda(this, (_ + other));
    }
}

