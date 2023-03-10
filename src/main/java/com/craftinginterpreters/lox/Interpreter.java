package com.craftinginterpreters.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>,
                                    Stmt.Visitor<Void>{
    private Environment environment = new Environment();
    void interpret (List<Stmt> statements) {
//        try {
//            Object value = evaluate (expr);
//            System.out.println(stringify(value));
//        } catch (RuntimeError error){
//            Lox.runtimeError(error);
//        }
        try{
            for(Stmt statement : statements) {
                execute(statement);
            }

        }catch(RuntimeError error){
            Lox.runtimeError(error);
        }

    }


    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left  = evaluate(expr.left);
        Object right  = evaluate(expr.right);
        switch(expr.operator.type){
            // Comparison
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
                // Term
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                // to support both add and concatenation of strings
                // to make LOX more approachable

                if(left instanceof  Double && right instanceof Double){
                    return (double) left + (double) right;
                }
                if (left instanceof String  && right instanceof String){
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;

                //equality
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

        }

        // Unreachable ?????
        return null;

    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // because this Expr Node has a Expr-type child node.
        Object right = evaluate(expr.right);
        switch(expr.operator.type ){
            case BANG:
                return !isTruthy(right);
            case MINUS :
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // The method used to evaluate the expression
    // this method is the start of the interpreter !!
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    private boolean isTruthy(Object object){
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean) object;
        return true;
    }
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        // can number use equals to compare with each other ?
        return a.equals(b);
    }

    // Run time error
    private void checkNumberOperand(Token operator , Object right){
        if(right instanceof  Double ) return ;
        throw new RuntimeError(operator, "Operand must be a number");
    }
    private void checkNumberOperands(Token operator , Object left, Object right) {
        if(left instanceof  Double && right instanceof  Double ) return ;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    private String stringify (Object object){
        if(object == null) return "nil";
        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0")) return text.substring(0, text.length() -2 );
            return text;
        }
        return object.toString();

    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
         evaluate(stmt.expression);
//        System.out.println(stringify(value));
        // a statement will not produce value so it returns null
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        // a statement will not produce value so it returns null
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer!=null ){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    private void execute(Stmt statement){
        statement.accept(this);
    }

    void executeBlock(List<Stmt> statements , Environment environment){
        // record the current environment that the interpreter exist
        // in order to recover later after finishing the interpret of
        // the block statement
        // the reason why to write like this is:
        // previous code gets the code in the current environment
        Environment previous = this.environment;
        try{
            this.environment = environment;
            for(Stmt statement : statements){
                execute(statement);
            }
        } finally {
             this.environment = previous;
        }

    }

}
