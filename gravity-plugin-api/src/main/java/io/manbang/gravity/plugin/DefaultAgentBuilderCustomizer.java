package io.manbang.gravity.plugin;

import io.manbang.gravity.bytebuddy.agent.builder.AgentBuilder;

enum DefaultAgentBuilderCustomizer implements AgentBuilderCustomizer {
    INSTANCE;

    @Override
    public AgentBuilder customize(AgentOptions options, AgentBuilder builder) {
        return builder.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
}
