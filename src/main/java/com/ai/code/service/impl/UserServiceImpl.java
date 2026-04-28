package com.ai.code.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.ai.code.constant.UserConstant;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.model.dto.user.UserQueryRequest;
import com.ai.code.model.enums.UserRoleEnum;
import com.ai.code.model.vo.LoginUserVO;
import com.ai.code.model.vo.UserVO;
import com.ai.code.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ai.code.model.entity.User;
import com.ai.code.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    private final UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        long count = userMapper.selectCountByQuery(
                new QueryWrapper()
                        .eq(User::getUserAccount, userAccount));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        String encryptPassword = getEncryptPassword(userPassword);
        User user = User.builder()
                .userAccount(userAccount)
                .userPassword(encryptPassword)
                .userName("无名")
                .userRole(UserRoleEnum.USER.getValue())
                .build();
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String salt = "info";
        return DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        User user = userMapper.selectOneByQuery(new QueryWrapper()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword));
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号或密码错误");
        }
        // 记录用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 返回脱敏用户信息
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (Objects.isNull(user) || Objects.isNull(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录状态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        return QueryWrapper.create()
                .eq(UserQueryRequest::getId, request.getId())
                .eq(UserQueryRequest::getUserRole, request.getUserRole())
                .like(UserQueryRequest::getUserAccount, request.getUserAccount())
                .like(UserQueryRequest::getUserName, request.getUserName())
                .like(UserQueryRequest::getUserProfile, request.getUserProfile())
                .orderBy(UserQueryRequest::getSortField, "ascend".equals(request.getSortOrder()));
    }


}
