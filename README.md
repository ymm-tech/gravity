# 重力工程
java agent 插件平台，基本思路就是，虚拟机启用参数配置重力工程一个 `javaagent`，此`agent`是个小核心，它来负责远程加载插件，这些插件既可以是第三方包，也可以是重力提供的SPI插件。根据插件的不同类型，agent会将插件追加到不同级别的构建路径，目前可以分为，`boot`、`system`和`agent`三个地方。
插件下载之后，会根据插件定义`PluginDefine`,对应用系统进行对应的`transform`，以期达到动态管理应用系统的能力。将来，做大后，中间件的很多功能，都可以不需要业务系统升级的情况下直接维护。
## 工程结构
### gravity-agent
java agent 代理，打包后配置为JVM javaagent启动参数启动
### gravity-platform
平台服务端，管理各种插件。平台可以可以在公司级别，应用级别和插件级别控制插件的生效与否。
### gravity-plugin
`gravity-plugin-api`是插件的核心父工程，此工程定义了 `PluginDefine`接口，如果需要开发插件，则都统一以 `gravity-plugin-api`工程为父工程。如果插件有拦截应用系统需求，则需要编写一个实现`PluginDefine`接口的SPI实现类，来配置拦截点和`Advice`或者`Interceptor`。开发完成后，打包后的`jar`包需要上传至`gravity-platform`平台，提供给`gravity-agent`加载使用。
## 工程配置
`gravity-agent`启动的时候，会创建`GRAVITY_HOME`目录，默认为`${user.home}/.gravity`，用来放置插件`jar`包，有三个子目录`boot`、`system`和`agent`，分别放置不同类型的`jar`。`jar`的类型，通过`maven`的`properties`参数`jar-type`配置。
### 启动参数

```shell script
# 生产配置
java -cp xxx.jar -javaagent:/xxx/gravity-agent-xxx.jar=appName=xxx,baseUrl=http://gravity-api.amh-group.com

```
> * `javaagent`：就是`agent`配置，为`agent jar`包全限定路径
> * `appName`：应用系统名
> * `baseUrl`：平台API地址，用于加载远程`jar`
> * `debugOut`：`DebugAgentBuilderCustomizer`特有参数，可以用来存放 `transform`之后的`class`类型，用于定位问题
> * `localDebug`：用于配置，是否本地加载插件，如果配置为`true`，则`agent`只会加载本地`GRAVITY_HOME`路径下的`jar`，同时开启UnitTestContainer，开启同个线程中传递值，方便assert


# 开发环境
单元测试：每个gravity-plugin都要求单元测试，由于plugin特殊性无法直接测试输入输出。因此采用了共享内存的方式，具体参考UnitTestContainer类。

日志增强：gravity-plugin不需要每个都添加一份log4j2.xml，gravity-log4j2-plugin 会自动添加一个默认Console输出的配置，具体参考UnitTestLog4j2PluginDefine类。

避免本地需要上传插件jar包到平台，我们可通过开启本地调试模式，定义maven profile=`local`为本地调试模式，mvn执行pakage时将plugin的jar输出到${GRAVITY_HOME}目录
```shell
mvn clean package -DskipTests -Plocal
```
## IDEA中配置VM参数：
```
-ea -javaagent:gravity-agent/target/gravity-agent-1.0.0-SNAPSHOT.jar=appName=gravity,localDebug=true,debugOut=./tmp

* 注意IDEA的Working Directory为工程根目录（即和.git目录同级，一般情况不填），不要使用$MODULE_WORKING_DIR$
* tmp目录会被git ignore* 
```