package com.ai.code.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.ai.code.constant.AppConstant;
import com.ai.code.controller.AppController;
import com.ai.code.core.AiCodeGeneratorFacade;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.exception.ThrowUtils;
import com.ai.code.model.dto.app.AppQueryRequest;
import com.ai.code.model.entity.User;
import com.ai.code.model.enums.CodeGenTypeEnum;
import com.ai.code.model.vo.AppVO;
import com.ai.code.model.vo.UserVO;
import com.ai.code.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.code.model.entity.App;
import com.ai.code.mapper.AppMapper;
import com.ai.code.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    private final UserService userService;

    private final AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 根据应用获取应用信息
     * @param app 应用
     * @return 应用信息
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq(App::getId, appQueryRequest.getId())
                .like(App::getAppName, appQueryRequest.getAppName())
                .like(App::getCover, appQueryRequest.getCover())
                .like(App::getInitPrompt, appQueryRequest.getInitPrompt())
                .eq(App::getCodeGenType, appQueryRequest.getCodeGenType())
                .eq(App::getDeployKey, appQueryRequest.getDeployKey())
                .eq(App::getPriority, appQueryRequest.getPriority())
                .eq(App::getUserId, appQueryRequest.getUserId())
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVoMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVoMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }


    @Override
    public Flux<String> chatToGenCode(Long appId, String prompt, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 不能为空");
        ThrowUtils.throwIf(prompt == null || prompt.isEmpty(), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 权限校验
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问");
        CodeGenTypeEnum enumByValue = CodeGenTypeEnum.getEnumByValue(app.getCodeGenType());
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        return aiCodeGeneratorFacade.generatorAndSaveCodeStream(prompt, enumByValue, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 仅本人可以部署自己的应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问");
        }
        // 检验是否已有部署key
        if (StringUtils.isBlank(app.getDeployKey())) {
            app.setDeployKey(RandomUtil.randomString(6));
        }
        // 根据代码生成类型，构建目录路径
        String sourceDirName = app.getCodeGenType() + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查目录是否存在
        File sourceFile = new File(sourceDirPath);
        if (!sourceFile.exists() || !sourceFile.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署目录不存在，请检查配置");
        }
        // 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + app.getDeployKey();
        try {
            FileUtil.copyContent(sourceFile, new File(deployDirPath),true);
        } catch (IORuntimeException e) {
            log.error("部署目录复制文件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署目录复制文件失败" + e.getMessage());
        }
        // 更新应用的deployKey和部署时间
        boolean updateRes = this.updateById(App.builder()
                .id(appId)
                .deployKey(app.getDeployKey())
                .deployedTime(LocalDateTime.now())
                .build());
        ThrowUtils.throwIf(!updateRes, ErrorCode.SYSTEM_ERROR, "部署信息更新失败");
        return String.format("%s/%s",AppConstant.CODE_DEPLOY_HOST, app.getDeployKey());
    }
}
