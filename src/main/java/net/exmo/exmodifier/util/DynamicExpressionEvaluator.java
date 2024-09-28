package net.exmo.exmodifier.util;

import net.exmo.exmodifier.Exmodifier;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class DynamicExpressionEvaluator {
    private final ScriptEngine engine;
    private final Map<String, Object> variables;

    public DynamicExpressionEvaluator() {
        // 获取JavaScript引擎
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("JavaScript");
        // 初始化变量映射
        this.variables = new HashMap<>();
    }

    /**
     * 添加或更新一个变量。
     *
     * @param name 变量名
     * @param value 变量值
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
        engine.put(name, value);
    }

    /**
     * 计算给定表达式的值。
     *
     * @param expression 要计算的表达式
     * @return 表达式的计算结果
     * @throws ScriptException 如果表达式无法被正确解析或执行
     */
    public  <T> T  evaluate(String expression) throws ScriptException {
        // 确保所有已设置的变量都被绑定到脚本引擎中
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        // 评估表达式
        return (T) engine.eval(expression);
    }


}