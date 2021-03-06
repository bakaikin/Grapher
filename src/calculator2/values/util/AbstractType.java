package calculator2.values.util;

import calculator2.calculator.executors.Variable;
import calculator2.values.util.actions.Func;
import calculator2.values.util.actions.Sign;
import calculator2.values.util.actions.functions.BinarFunc;
import calculator2.values.util.actions.functions.MultiFunc;
import calculator2.values.util.actions.functions.UnarFunc;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractType<T> {
    public final List<Sign<T>> signs;
    public final List<Func<T>> funcs;
    public final List<Variable<T>> consts;
    public final List<String> funcNames;
    private Sign<T> missingSign;
    protected AbstractType() {
        signs = new ArrayList<>();
        funcs = new ArrayList<>();
        consts = new ArrayList<>();
        funcNames = new ArrayList<>();
    }

    protected Sign<T> addSign(char name, BinarFunc<T> sign, int priority) {
        Sign<T> s = new Sign<>(name, sign, priority);
        signs.add(s);
        return s;
    }
    protected void unarySign(Sign<T> sign, UnarFunc<T> func){
        sign.canBeUnary = true;
        addFunction("" + sign.name, func, 5);
    }

    protected void setMissingSign(Sign<T> sign){
        this.missingSign = sign;
    }

    public void addConst(String name, T state) {
        consts.add(new Variable<>(name, state));
    }

    public void addFunction(String name, UnarFunc<T> unarFunc, int priority) {
        funcs.add(new Func<>(name, unarFunc, priority));
    }

    public void addFunction(String name, BinarFunc<T> binarFunc, int priority) {
        funcs.add(new Func<>(name, binarFunc, priority));
    }
    public void addFunction(String name, MultiFunc<T> multiFunc, int args, int priority){
        funcs.add(new Func<>(name, multiFunc, args, priority));
    }

    public abstract T toValue(String text);

    public Sign<T> getMissingSign() {
        return missingSign;
    }

    public void addFuncName(String name){
        funcNames.add(name);
    }
}
