package io.manbang.gravity.plugin;


import net.bytebuddy.agent.builder.AgentBuilder;

enum DefaultAgentBuilderCustomizer implements AgentBuilderCustomizer {
    INSTANCE;

    @Override
    public AgentBuilder customize(AgentOptions options, AgentBuilder builder) {
        return builder.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
}
