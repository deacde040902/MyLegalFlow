package com.legalflow.agent;

public interface Agent<I, O> {
    String getName();

    O execute(I input);

    boolean canHandle(Object input);
}
