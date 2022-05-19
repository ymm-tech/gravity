# gravity 满帮agent方案

## gravity agent
gravity是满帮基于[java agent](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html#package.description)自研的一款字节码层面AOP框架，目的是基于满帮业务场景，为插件开发同学降低字节码增强难度，目前满帮内部基于gravity开发出了六十余款插件，大致覆盖的业务场景如下：

|场景名称|简介|业务场景|
|  ----  | ----  |----  |
| ironman | 满帮mesh方案 |涵盖rpc，mq，redis，config等多种中间件组件，彻底隔离api与实现，让开发同学无痛无感升级，后续也将开源，敬请期待 |
| venom   | 满帮混沌场景 |故障演练，模拟测试，涵盖多种中间件，可模拟多场景异常 |
| mock    | 满帮测试框架 |mock演练测试，涵盖多场景，多种组件的模拟 |
| hubble  | 满帮APM解决方案 |业务系统性能实时监控，链路埋点 |
| common  | 常规增强 |一些常规场景的增强，比如[TTL](https://github.com/alibaba/transmittable-thread-local)集成 |

## 1.简单使用
首先简单模拟一笔发货订单场景
```java
/**
 * 货主
 */
public class Shippers {
    /**
     * 发布订单
     */
    public String postOrder() {
        return "南京市 雨花台区 万博科技园 运满满总部 50立方 10吨";
    }
}

/**
 * 司机
 */
public class Driver {
    /**
     * 接单
     */
    public boolean acceptOrder(String address) {
        if (Objects.nonNull(address) && address.startsWith("南京市")) {
            return true;
        }
        return false;
    }

    /**
     * 装货
     */
    public String loadCargo() {
        return "load cargo success.";
    }

    /**
     * 运货
     */
    public String deliverCargo() {
        return "deliver cargo success.";
    }
}

/**
 * 发货订单
 */
public class Order {

    public static void main(String[] args) {
        new Order().trade();
    }

    public void trade() {
        final Shippers shippers = new Shippers();
        final Driver driver = new Driver();
        final String address = shippers.postOrder();
        driver.acceptOrder(address);
        driver.loadCargo();
        driver.deliverCargo();
    }
}
```

这时候，我们希望可以监控该笔订单的行为，决定通过无侵入方式打印出入参<br>
首先引入pom依賴:
```
<dependency>
   <groupId>io.manbang</groupId>
   <artifactId>gravity-plugin-api</artifactId>
   <version>1.0.0</version>
</dependency>
```
插件定义，描述目标的织入点:
```java
/**
 * @since 2022/05/19 10:55
 */
public class AopPluginDefine implements PluginDefine {
    @Override
    public ElementMatcher<TypeDescription> getTypeMatcher() {
        return ElementMatchers.named("io.manbang.gravity.trade.Driver")
                .or(ElementMatchers.named("io.manbang.gravity.trade.Shippers"));
    }

    @Override
    public Plugin[] getPlugins() {
        return new Plugin[]{Plugin.advice(ElementMatchers.isMethod(), "io.manbang.gravity.plugin.monitor.AopAdvice").withMethod()};
    }
}
```
具体织入的逻辑：
```java
/**
 * @since 2022/05/19 11:05
 */
public class AopAdvice implements Advice {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(AopAdvice.class.getName());

    @Override
    public void enterMethod(ExecuteContext context) {
        final Method method = context.getMethod();
        final Object[] argument = context.getArguments();
        final StringBuilder builder = new StringBuilder();
        builder.append("method enter:").append(method.getName());
        for (int i = 0; i < argument.length; i++) {
            builder.append(" arg  number:").append(i).append(" arg :").append(argument[i]);
        }
        log.info(builder.toString());
    }

    @Override
    public void exitMethod(ExecuteContext context) {
        final Method method = context.getMethod();
        final Object result = context.getResult();
        final StringBuilder builder = new StringBuilder();
        builder.append("method exit:").append(method.getName());
        builder.append("result:").append(result);
        log.info(builder.toString());
    }
}
```
新建`SPI`文件：`/META-INF/services/io.manbang.gravity.plugin.PluginDefine`，内容为新创建的插件定义`AopPluginDefine` <br><br>
打包该插件，并将打包好的插件放置于`{user.home}/.gravity/cargo-publish-app/agent`目录下(`user.home`路径可以通过执行`java -XshowSettings:properties -version`得到，`cargo-publish-app`为应用名，可以自行定义)<br><br>
执行`Order`的`main`方法在启动时，新增`VM`命令：`-javaagent:XXXX/XXXX/gravity-agent.jar=appName=cargo-publish-app`，`gravity-agent.jar`下载路径：[agent](https://github.com/ymm-tech/gravity/blob/27c4399955cc0961c7c2538ea37bb3cdf93bc182/gravity-agent.jar)<br>
可以观察到控制台输出预期想要的业务出入参：
```
五月 19, 2022 4:49:05 下午 io.manbang.gravity.agent.GravityAgent instrument
信息: 加载插件：AopPluginDefine
五月 19, 2022 4:49:05 下午 io.manbang.gravity.agent.PluginTransformer transform
信息: io.manbang.gravity.trade.Shippers
五月 19, 2022 4:49:05 下午 io.manbang.gravity.agent.PluginTransformer getClassLoader
信息: The current classLoader is sun.misc.Launcher$AppClassLoader@18b4aac2 , pluginDefine: AopPluginDefine , transform: io.manbang.gravity.trade.Shippers
五月 19, 2022 4:49:06 下午 io.manbang.gravity.agent.GravityServiceBoot startServices
信息: 开始启动重力服务……
五月 19, 2022 4:49:06 下午 io.manbang.gravity.agent.PluginTransformer transform
信息: io.manbang.gravity.trade.Driver
五月 19, 2022 4:49:06 下午 io.manbang.gravity.agent.PluginTransformer getClassLoader
信息: The current classLoader is sun.misc.Launcher$AppClassLoader@18b4aac2 , pluginDefine: AopPluginDefine , transform: io.manbang.gravity.trade.Driver
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice enterMethod
信息: method enter:postOrder
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice exitMethod
信息: method exit:postOrderresult:南京市 雨花台区 万博科技园 运满满总部 50立方 10吨
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice enterMethod
信息: method enter:acceptOrder arg  number:0 arg :南京市 雨花台区 万博科技园 运满满总部 50立方 10吨
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice exitMethod
信息: method exit:acceptOrderresult:true
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice enterMethod
信息: method enter:loadCargo
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice exitMethod
信息: method exit:loadCargoresult:load cargo success.
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice enterMethod
信息: method enter:deliverCargo
五月 19, 2022 4:49:06 下午 io.manbang.gravity.plugin.monitor.AopAdvice exitMethod
信息: method exit:deliverCargoresult:deliver cargo success.
```
具体示例已经放在`gravity-demo`和`gravity-plugin`中
