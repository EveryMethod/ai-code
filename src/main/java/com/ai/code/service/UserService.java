package com.ai.code.service;

import com.mybatisflex.core.service.IService;
import com.ai.code.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

}
