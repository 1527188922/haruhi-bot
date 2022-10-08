# haruhi-bot

#### 介绍
这是一个java bot程序  
没有用到专门的bot框架,下载依赖、打包、部署这些操作直接当作一个普普通通的springboot maven项目对待就行了  
使用maven管理依赖和打包  
基于go-cqhttp （正向ws和http）

#### 软件架构
1. spring boot
2. spring mvc
3. mybatis-plus
4. dynamic-datasource
5. maven
6. mysql


# 安装教程

## 准备工作
1. 安装jdk1.8（等同于8）；安装maven（3.6以及上）；安装mysql5.7（不需手动建库）；[下载go-cqhttp](https://github.com/Mrs4s/go-cqhttp/releases)
2. 下载master分支的源码

## 下载依赖和打包
1. 执行脚本 `build.bat`(linux使用`build.sh`), ,执行这个脚本之前要配置好maven和jdk的环境变量
2. 执行完成之后进入 `target/` 目录，会看到 `haruhi-bot.zip` 文件,看到这个表示下载依赖和打包都成功了
## 启动bot
1. 启动go-cqhttp 需要配置ws正向 和 http
2. 解压`haruhi-bot.zip`,修改`application.yml`配置文件
3. 执行`startup.bat`文件（linux执行`startup.sh`文件）
## 关闭bot
1. windows叉掉cmd窗口或者双击`stop.bat`都可关闭(linux执行`stop.sh`)
2. 关闭go-cqhttp
## 不会装
不会装或者遇到问题都可以加群，免费帮忙解决或安装
![输入图片说明](https://gitee.com/Lelouch-cc/resources-image/raw/master/haruhi-bot/QQ%E5%9B%BE%E7%89%8720220920090612.jpg)

# 使用说明

## 已实现功能
功能文档：https://blog.csdn.net/cxy152718/article/details/126539271  
![输入图片说明](https://gitee.com/Lelouch-cc/resources-image/raw/master/haruhi-bot/function/%E5%8A%9F%E8%83%BD1.jpg)
![输入图片说明](https://gitee.com/Lelouch-cc/resources-image/raw/master/haruhi-bot/function/%E5%8A%9F%E8%83%BD2.jpg)
![输入图片说明](https://gitee.com/Lelouch-cc/resources-image/raw/master/haruhi-bot/function/%E5%8A%9F%E8%83%BD3.jpg)  


# 参与贡献

...

# 特技

....
