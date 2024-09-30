package net.exmo.exmodifier.util;


import net.exmo.exmodifier.repack.*;
import net.exmo.exmodifier.repack.net.objecthunter.exp4j.Expression;
import net.exmo.exmodifier.repack.net.objecthunter.exp4j.ExpressionBuilder;


import java.util.HashMap;
import java.util.Map;

public class DynamicExpressionEvaluator {
    private final Map<String, Double> variables; // exp4j 使用 double 类型

    public DynamicExpressionEvaluator() {
        this.variables = new HashMap<>();
    }

    /**
     * 添加或更新一个变量。
     *
     * @param name 变量名
     * @param value 变量值
     */
    public DynamicExpressionEvaluator setVariable(String name, double value) {
        variables.put(name, value);
        return this;
    }

    /**
     * 获取变量的值。
     *
     * @param name 变量名
     * @return 变量值
     */
    public double getVariable(String name) {
        return variables.getOrDefault(name, 0.0);
    }

    /**
     * 计算给定表达式的值。
     *
     * @param expression 要计算的表达式
     * @return 表达式的计算结果
     */
    public double evaluate(String expression)  {
        // 使用 ExpressionBuilder 创建表达式
        Expression expr = new ExpressionBuilder(expression)
                .variables(variables.keySet().toArray(new String[0]))  // 设置所有变量
                .build();

        // 设置每个变量的值
        for (Map.Entry<String, Double> entry : variables.entrySet()) {
            expr.setVariable(entry.getKey(), entry.getValue());
        }

        // 评估表达式
        return expr.evaluate();
    }
}