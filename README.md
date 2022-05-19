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

这时候，我们希望可以监控下该笔订单的
