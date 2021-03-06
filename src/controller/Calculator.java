package controller;

import calculator2.ArrayCalculator;
import calculator2.calculator.executors.Expression;
import calculator2.calculator.executors.Variable;
import calculator2.values.Number;
import model.Language;
import model.help.FullModel;
import threads.Tasks;
import view.elements.CalculatorView;
import view.elements.FunctionsView;
import view.elements.TextElement;
import view.grapher.graphics.Function;
import view.grapher.graphics.Graphic;
import view.grapher.graphics.Implicit;
import view.grapher.graphics.Parametric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.Language.UPDATER_ERRORS;

public class Calculator {
    private ModelUpdater updater;
    private final ArrayCalculator<Double> calculator;
    private final Runnable repaint;
    private Runnable resize;
    private FunctionsView functionsView;
    private CalculatorView calculatorView;
    private Tasks tasks;

    public Calculator(ModelUpdater updater, Runnable repaint) {
        this.updater = updater;
        this.repaint = repaint;
        tasks = new Tasks();
        calculator = new ArrayCalculator<>();
    }

    public void recalculate() {
        tasks.clearTasks();
        tasks.runTask(() -> {
            try {
                updater.setState(Language.CONVERTING);
                List<String> graphs = new ArrayList<>();
                List<String> calc = new ArrayList<>();
                calc.add(calculatorView.getText());
                for (int i = 0; i < updater.list.getElements().size(); ++i) {
                    TextElement e = updater.list.getElements().get(i);
                    if (updater.graphics.get(i) instanceof Function || updater.graphics.get(i) instanceof Implicit) {
                        graphs.add(updater.graphics.get(i).name + "=" + e.getText());
                    } else if (updater.graphics.get(i) instanceof Parametric) {
                        String text = updater.list.getElements().get(i).getText();
                        String[] expressions = text.split(":");
                        if (expressions.length == 2) {
                            calc.add(expressions[0]);
                            calc.add(expressions[1]);
                        } else {
                            throw new RuntimeException(UPDATER_ERRORS[0]);
                        }
                    }
                }
                calculator.calculate(
                        Arrays.asList(functionsView.getText().split("\n")),
                        graphs,
                        calc,
                        new Number()
                );
                calculatorView.setAnswer(calculator.getExpressions().get(0));
                int funcs = 0;
                int parameters = 0;
                for (int i = 0; i < updater.graphics.size(); ++i) {
                    Graphic g = updater.graphics.get(i);
                    if (g instanceof Function) {
                        Function f = (Function) g;
                        List<Variable<Double>> vars = calculator.getVars().get(funcs);
                        if (vars.size() == 0) {
                            Variable<Double> var = new Variable<>();
                            var.setName((f.abscissa) ? "x" : "y");
                            vars.add(var);
                        }
                        if (vars.size() > 1) {
                            throw new RuntimeException(UPDATER_ERRORS[1] + " " + (i + 1) + " " + UPDATER_ERRORS[2]);
                        }
                        TextElement el = updater.list.getElements().get(i);
                        Variable<Double> var = vars.get(0);
                        if (var.getName().equals("y") && f.abscissa) {
                            f.abscissa = false;
                            el.setName(f.name + "(y)");
                        } else if (var.getName().equals("x") && !f.abscissa) {
                            f.abscissa = true;
                            el.setName(f.name + "(x)");
                        }
                        f.update(calculator.getGraphics().get(funcs), var);
                        ++funcs;
                    } else if (g instanceof Parametric) {
                        List<Variable<Double>> vars = calculator.getExpressionVars().get(parameters);
                        if (vars.size() == 0)
                            vars.add(new Variable<>());
                        Variable<Double> varX = vars.get(0);
                        vars = calculator.getExpressionVars().get(parameters + 1);
                        if (vars.size() == 0)
                            vars.add(new Variable<>());
                        Variable<Double> varY = vars.get(0);
                        Expression<Double> funcX = calculator.getExpressions().get(parameters + 1);
                        Expression<Double> funcY = calculator.getExpressions().get(parameters + 2);
                        g.update(funcY, varY);
                        ((Parametric) g).updateX(funcX, varX);
                        parameters += 2;
                    } else if (g instanceof Implicit) {
                        List<Variable<Double>> vars = calculator.getVars().get(funcs);
                        Expression<Double> func = calculator.getGraphics().get(funcs);
                        if (vars.size() > 2)
                            throw new RuntimeException(UPDATER_ERRORS[3]);
                        Variable<Double> varX = null;
                        Variable<Double> varY = null;
                        checkingVars:
                        {
                            if (vars.size() == 0) {
                                varX = new Variable<>();
                                varY = new Variable<>();
                                break checkingVars;
                            } else if (vars.get(0).getName().equals("x")) {
                                varX = vars.get(0);
                            } else if (vars.get(0).getName().equals("y")) {
                                varY = vars.get(0);
                            } else {
                                throw new RuntimeException(UPDATER_ERRORS[3]);
                            }
                            if (vars.size() == 1 && varX == null) {
                                varX = new Variable<>();
                            } else if (vars.size() == 1) {
                                varY = new Variable<>();
                            } else if (vars.get(1).getName().equals("x")) {
                                varX = vars.get(1);
                            } else if (vars.get(1).getName().equals("y")) {
                                varY = vars.get(1);
                            } else {
                                throw new RuntimeException(UPDATER_ERRORS[3]);
                            }
                        }
                        g.update(func, varX);
                        ((Implicit) g).updateY(varY);
                        ++funcs;
                    }
                }
                updater.dangerState = false;
                resetConstant("tm", updater.getSupportFrameManager().getTime());
                calculatorView.update();
                resize.run();
                repaint.run();
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    updater.error(e.toString());
                    e.printStackTrace();
                } else
                    updater.error(e.getMessage());
            }
        });
    }

    public void runResize() {
        tasks.clearTasks();
        if (!updater.dangerState)
            tasks.runTask(() -> {
                try {
                    resize.run();
                    repaint.run();
                } catch (Throwable t) {
                    updater.error(t.getClass().getName());
                }
            });
    }

    public void frameResize() {
        tasks.clearTasks();
        tasks.runTask(() -> {
            if (!updater.dangerState) {
                try {
                    for (Graphic g : updater.graphics) {
                        g.funcChanged();
                    }
                    calculatorView.update();
                    resize.run();
                    repaint.run();
                } catch (Throwable e) {
                    updater.error(e.getClass().getName());
                    e.printStackTrace();
                }
            }
        });
    }

    public void setResize(Runnable resize) {
        this.resize = resize;
        runResize();
    }

    public void resetConstant(String name, double time) {
        calculator.resetConstant(name, time);
    }

    public void setElements(CalculatorView calculatorView, FunctionsView functionsView) {
        this.calculatorView = calculatorView;
        this.functionsView = functionsView;
    }
    void run(Runnable r){
        tasks.runTask(r);
    }
    void makeModel(FullModel m){
        List<String> graphs = new ArrayList<>();
        List<String> graphicsInfo = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < updater.graphics.size(); ++i){
            TextElement view= updater.list.getElements().get(i);
            graphs.add(view.getText());
            Graphic graphic = updater.graphics.get(i);
            sb.append(graphic.name);
            sb.append('\n');
            sb.append(graphic.MAP_SIZE);
            sb.append('\n');
            if(graphic instanceof Function){
                sb.append("Function");
            }else if(graphic instanceof Parametric){
                sb.append("Parametric\n");
                sb.append(((Parametric) graphic).getStartT());
                sb.append(':');
                sb.append(((Parametric) graphic).getEndT());
            }else if(graphic instanceof Implicit){
                sb.append("Implicit\n");
                sb.append(((Implicit) graphic).getSensitivity());
            }
            graphicsInfo.add(sb.toString());
            sb.setLength(0);
        }
        m.graphics = graphs;
        m.graphics_info = graphicsInfo;
        m.functions = this.functionsView.getText();
        m.calculator = this.calculatorView.getText();
    }
    public void fromModel(FullModel m){
        for(int i = 0; i < m.graphics.size(); ++i){
            String graphic = m.graphics.get(i);
            String params = m.graphics_info.get(i);
            if(params.equals(""))
                continue;
            updater.add(graphic, params);
        }
        calculatorView.setText(m.calculator);
        functionsView.setText(m.functions);
    }
}
