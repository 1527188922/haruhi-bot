package com.haruhi.bot.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.haruhi.bot.config.DataSourceConfig;
import com.haruhi.bot.mapper.system.DataBaseInitMapper;
import com.haruhi.bot.mapper.TableInitMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@Component
public class DataBaseService {

    @Autowired
    private DataBaseInitMapper dataBaseInitMapper;
    @Autowired
    private TableInitMapper tableInitMapper;
    @Autowired
    private DynamicDataSourceProperties dynamicDataSourceProperties;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;


    public void initDataBase(){
        log.info("开始初始化数据库...");
        try {
            if(dataBaseInitMapper.dataBaseIsExist(DataSourceConfig.DATA_BASE_BOT) == 0){
                log.info("数据库不存在,开始创建...");
                dataBaseInitMapper.createDataBase(DataSourceConfig.DATA_BASE_BOT);
                log.info("数据库创建成功");
            }
            log.info("开始重新加载数据源...");
            reloadDatabaseSource();

            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_CHECKIN) == 0){
                tableInitMapper.createCheckin(DataSourceConfig.BOT_T_CHECKIN);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_DISABLE_FUNCTION) == 0){
                tableInitMapper.createDisableFunction(DataSourceConfig.BOT_T_DISABLE_FUNCTION);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_PIXIV) == 0){
                tableInitMapper.createPixiv(DataSourceConfig.BOT_T_PIXIV);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_WORD_STRIP) == 0){
                tableInitMapper.createWordStrip(DataSourceConfig.BOT_T_WORD_STRIP);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_VERBAL_TRICKS) == 0){
                tableInitMapper.createVerbalTricks(DataSourceConfig.BOT_T_VERBAL_TRICKS);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_GROUP_CHAT_HISTORY) == 0){
                tableInitMapper.createGroupChatHistory(DataSourceConfig.BOT_T_GROUP_CHAT_HISTORY);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_POKE_REPLY) == 0){
                tableInitMapper.createPokeReply(DataSourceConfig.BOT_T_POKE_REPLY);
            }
            if(dataBaseInitMapper.tableIsExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_SUBSCRIBE_NEWS) == 0){
                tableInitMapper.createSubscribeNews(DataSourceConfig.BOT_T_SUBSCRIBE_NEWS);
            }

            log.info("初始化数据库完成");
        }catch (Exception e){
            log.error("初始化数据库异常",e);
            System.exit(0);
        }
    }

    /**
     * 创建bot数据源
     */
    private void reloadDatabaseSource(){
        addDataSource();
        try {
            dynamicRoutingDataSource.afterPropertiesSet();
        } catch (Exception e) {
            log.error("重新加载数据源失败",e);
            System.exit(0);
        }
    }

    private void addDataSource(){
        DataSourceProperty newMaster = new DataSourceProperty();
        newMaster.setUsername(DataSourceConfig.DATA_BASE_BOT_USERNAME);
        newMaster.setPassword(DataSourceConfig.DATA_BASE_BOT_PASSWORD);
        newMaster.setDriverClassName(DataSourceConfig.DATA_BASE_MASTER_DRIVERCLASSNAME);
        newMaster.setUrl(MessageFormat.format(DataSourceConfig.jdbcUrlTemplate,
                DataSourceConfig.DATA_BASE_BOT_HOST,DataSourceConfig.DATA_BASE_BOT_PORT,DataSourceConfig.DATA_BASE_BOT));
        newMaster.setDruid(new DruidConfig());

        // 将旧主数据拉出来 下面做替换
        DataSourceProperty oldMaster = dynamicDataSourceProperties.getDatasource().get(DataSourceConfig.DATA_SOURCE_MASTER);

        // 将程序配置的数据库作为主数据源
        dynamicDataSourceProperties.getDatasource().put(DataSourceConfig.DATA_SOURCE_MASTER,newMaster);
        dynamicDataSourceProperties.getDatasource().put(DataSourceConfig.DATA_SOURCE_SYSTEM,oldMaster);
    }
}
