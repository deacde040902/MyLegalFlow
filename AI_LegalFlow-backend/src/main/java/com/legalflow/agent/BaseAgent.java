package com.legalflow.agent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseAgent<I, O> implements Agent<I, O> {

    protected final String name;

    public BaseAgent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public O execute(I input) {
        log.info("智能体 {} 开始执行，输入类型: {}", name, 
                input != null ? input.getClass().getSimpleName() : "null");
        try {
            O output = doExecute(input);
            log.info("智能体 {} 执行成功", name);
            return output;
        } catch (Exception e) {
            log.error("智能体 {} 执行失败: {}", name, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean canHandle(Object input) {
        return true;
    }

    protected abstract O doExecute(I input);
}
