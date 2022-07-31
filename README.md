# haruhi-bot

#### 介绍
这是一个java bot...
基于go-cqhttp 正向ws
mysql 5.7
jdk 1.8

#### 软件架构
1. spring boot
2. spring mvc
3. mybatis-push
4. dynamic-datasource
5. maven
6. ...


#### 安装教程
## 准备工作
1.  安装jdk1.8（等同于8）；安装maven（3.6以上）；安装mysql5.7（不需手动建库）；[下载go-cqhttp](https://github.com/Mrs4s/go-cqhttp/releases)
2.  配置jdk和maven的环建变量（百度上很多）；mysql随意
3.  clone master分支或者zip包下载源码
4. 修改配置文件 `src\main\resources\application-dev.yml`
## 下载依赖和打包
1. 进入项目根目录（有pom.xml的那个目录）
2. 在这个目录执行脚本 build.bat(linux使用build.sh)
3. 执行完成之后进入 `target/` 目录，会看到 `haruhi-bot.zip` 文件，复制到其他目录,解压
## 启动bot
1. 启动go-cqhttp（首次启动选择ws正向代理）
2. 解压zip包之后（linux也能解压zip包，使用unzip），修改`application.yml`文件，`bot.ws`配置项与go-cqhttp 中的ws地址保持一致；执行startup.bat文件（linux执行startup.sh文件）
#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

...


#### 特技

....
