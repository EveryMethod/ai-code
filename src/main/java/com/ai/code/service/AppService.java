package com.ai.code.service;

import com.ai.code.model.dto.app.AppQueryRequest;
import com.ai.code.model.entity.User;
import com.ai.code.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ai.code.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
public interface AppService extends IService<App> {

    /**
     * 根据应用获取应用视图对象
     * @param app 应用
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 根据应用查询条件获取查询包装器
     * @param appQueryRequest 应用查询条件
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 根据应用列表获取应用视图对象列表
     * @param appList 应用列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据应用ID和提示词生成代码
     * @param appId 应用ID
     * @param prompt 提示词
     * @param loginUser 登录用户
     * @return 生成的代码
     */
    Flux<String> chatToGenCode(Long appId, String prompt, User loginUser);


    /**
     * 部署应用
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return 部署结果
     */
    String deployApp(Long appId, User loginUser);
}
