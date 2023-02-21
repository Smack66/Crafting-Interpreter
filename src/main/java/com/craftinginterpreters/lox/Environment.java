package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;
    Environment(){
        this.enclosing = null;
    }
    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }
    //define method will always work on its current environment object
    void define(String name , Object value){
        values.put(name, value);
    }
    // get and assign method will need to walk on the chain
    public Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        // recursively call get method to walk on the chain
        if(enclosing!=null) return enclosing.get(name);
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");

    }

    void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return ;
        }
        // current environment doesn't have corresponding variable
        // go to the enclosing one to look for it
        if(enclosing!=null){
            enclosing.assign(name, value);
            return ;
        }
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }


}
