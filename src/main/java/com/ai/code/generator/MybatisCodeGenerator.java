package com.ai.code.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * @description 代码生成器入口类。
 * @author alh
 * @date 2026/04/28 11:31:09
 */
public class MybatisCodeGenerator {

    /**
     * 需要生成的表名数组。
     */
    private static final String[] TABLE_NAMES = {"chat_history"};

    /**
     * 代码生成器入口方法。
     *
     * @param args 参数。
     */
    public static void main(String[] args) {
        // 获取数据源信息
        Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> datasourceConfig = dict.getByPath("spring.datasource");

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl((String) datasourceConfig.get("url"));
        dataSource.setUsername((String) datasourceConfig.get("username"));
        dataSource.setPassword(String.valueOf(datasourceConfig.get("password")));

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }


    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("com.ai.code.generatorResult");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                .setLogicDeleteColumn("is_delete");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        globalConfig.enableController();

        globalConfig.getJavadocConfig()
                .setAuthor("alh")
                .setSince("2026-04-28 11:31:09");

        return globalConfig;
    }
}