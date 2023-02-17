package com.craftinginterpreters.lox;

public class Interpreter implements Expr.Visitor<Object> {
    void interpret (Expr expr) {
        try {
            Object value = evaluate (expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error){
            Lox.runtimeError(error);
        }

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

}
