package com.haruhi.bot.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.haruhi.bot.config.DataSourceConfig;
import com.haruhi.bot.mapper.DataBaseInitMapper;
import com.haruhi.bot.mapper.TableInitMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
    @Resource
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    @PostConstruct
    private void init(){
        log.info("开始初始化数据库...");
        dataBaseInit();
    }

    public void dataBaseInit(){
        try {
            if(dataBaseInitMapper.isDataBaseExist(DataSourceConfig.DATA_BASE_BOT) == 0){
                log.info("数据库不存在,开始创建...");
                dataBaseInitMapper.createDataBase(DataSourceConfig.DATA_BASE_BOT);
            }
            log.info("建库成功,开始重新加载数据源...");
            reloadDatabaseSource();

            if(dataBaseInitMapper.isTableExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_CHECKIN) == 0){
                tableInitMapper.createCheckin(DataSourceConfig.BOT_T_CHECKIN);
            }
            if(dataBaseInitMapper.isTableExist(DataSourceConfig.DATA_BASE_BOT,DataSourceConfig.BOT_T_DISABLE_HANDLER) == 0){
                tableInitMapper.createDisableHandler(DataSourceConfig.BOT_T_DISABLE_HANDLER);
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
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setUsername(DataSourceConfig.DATA_BASE_BOT_USERNAME);
        dataSourceProperty.setPassword(DataSourceConfig.DATA_BASE_BOT_PASSWORD);
        dataSourceProperty.setDriverClassName(DataSourceConfig.DATA_BASE_MASTER_DRIVERCLASSNAME);
        dataSourceProperty.setUrl(MessageFormat.format(DataSourceConfig.jdbcUrlTemplate,
                DataSourceConfig.DATA_BASE_BOT_HOST,DataSourceConfig.DATA_BASE_BOT_PORT,DataSourceConfig.DATA_BASE_BOT));
        dataSourceProperty.setDruid(new DruidConfig());
        dynamicDataSourceProperties.getDatasource().put(DataSourceConfig.DATA_SOURCE_BOT_NAME,dataSourceProperty);
        try {
            dynamicRoutingDataSource.afterPropertiesSet();
        } catch (Exception e) {
            log.error("重新加载数据源失败",e);
            System.exit(0);
        }
    }
}
