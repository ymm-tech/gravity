# gravity 满帮agent方案

## gravity agent
gravity是满帮基于[java agent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html#package.description)自研的一款字节码层面AOP框架，目的是基于满帮业务场景，为插件开发同学降低字节码增强难度，目前满帮内部基于gravity开发出了六十余款插件，大致覆盖的业务场景如下：

|场景名称|简介|业务场景|
|  ----  | ----  |----  |
| ironman | 满帮mesh方案 |涵盖rpc，mq，redis，config等多种中间件组件，彻底隔离api与实现，让开发同学无痛无感升级，后续也将开源，敬请期待 |
| venom   | 满帮混沌场景 |故障演练 |
| mock    | 满帮测试框架 |mock演练测试 |
| hubble  | 满帮APM解决方案 |业务系统实时监控 |
| common  | 常规增强 |一些常规场景的增强，比如[TTL](https://github.com/alibaba/transmittable-thread-local)集成 |
