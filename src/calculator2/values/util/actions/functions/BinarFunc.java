package calculator2.values.util.actions.functions;

import calculator2.calculator.executors.Expression;

public interface BinarFunc<T> {
    T execute(Expression<T> a, Expression<T> b);
}
