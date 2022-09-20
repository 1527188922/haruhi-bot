# haruhi-bot

#### 介绍
这是一个java bot程序  
没有用到专门的bot框架,下载依赖、打包、部署这些操作直接当作一个普普通通的springboot maven项目对待就行了  
使用maven管理依赖和打包  
基于go-cqhttp （正向ws和http）

#### 软件架构
1. spring boot
2. spring mvc
3. mybatis-push
4. dynamic-datasource
5. maven
6. mysql


# 安装教程

## 准备工作
1. 安装jdk1.8（等同于8）；安装maven（3.6以及上）；安装mysql5.7（不需手动建库）；[下载go-cqhttp](https://github.com/Mrs4s/go-cqhttp/releases)
2. 配置jdk和maven的环建变量（百度上很多）；mysql随意
3. clone master分支或者zip包下载源码
4. 如果直接得到zip包，那么不需要安装maven

## 下载依赖和打包
1. 进入项目根目录（有pom.xml的那个目录）
2. 在这个目录执行脚本 build.bat(linux使用build.sh)
3. 执行完成之后进入 `target/` 目录，会看到 `haruhi-bot.zip` 文件,看到这个表示下载依赖和打包都成功了
4. 把haruhi-bot.zip拎出来放到其他目录，不然每次打包， `target/` 目录都会被清空的
## 启动bot
1. 启动go-cqhttp 需要配置ws正向 和 http
2. 解压`haruhi-bot.zip`（linux也能解压zip包，使用unzip）
3. 修改`application.yml`文件，配置ws地址和http地址，与go-cqhttp中的配置（ip:port）保持一致;
4. ※注意：打包之后（zip包里）的`application.yml`文件，就是打包之前的`resource/application-pro.yml`文件
5. 在`application.yml`文件中配置好数据库名称，只要配置好数据库名称就行，程序会自动建库和建表
6. 执行startup.bat文件（linux执行startup.sh文件）
7. windows关闭bot：关闭cmd窗口即可
8. linux关闭bot：使用命令`jps`可以查看当前服务器上运行的所有java进程，找到进程名为haruhiBot-server.jar的pid，`kill -9 pid`
## 如果需要的话，但又不会装
可以加群，我可以免费帮忙装
![输入图片说明](https://gitee.com/Lelouch-cc/resources-image/raw/master/haruhi-bot/QQ%E5%9B%BE%E7%89%8720220920090612.jpg)

# 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

# 参与贡献

...

# 特技

....
