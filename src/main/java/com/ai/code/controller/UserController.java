package com.ai.code.controller;

import com.ai.code.commom.BaseResponse;
import com.ai.code.commom.ResultUtils;
import com.ai.code.exception.ErrorCode;
import com.ai.code.exception.ThrowUtils;
import com.ai.code.model.dto.UserLoginRequestDTO;
import com.ai.code.model.dto.UserRegisterRequestDTO;
import com.ai.code.model.vo.LoginUserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.ai.code.model.entity.User;
import com.ai.code.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 保存用户。
     *
     * @param user 用户
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody User user) {
        return userService.save(user);
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return userService.removeById(id);
    }

    /**
     * 根据主键更新用户。
     *
     * @param user 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody User user) {
        return userService.updateById(user);
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<User> list() {
        return userService.list();
    }

    /**
     * 根据主键获取用户。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("getInfo/{id}")
    public User getInfo(@PathVariable Long id) {
        return userService.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<User> page(Page<User> page) {
        return userService.page(page);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequestDTO 注册参数
     * @return 注册结果
     */
    @PostMapping("register")
    public BaseResponse<?> userRegister(@RequestBody @Valid UserRegisterRequestDTO userRegisterRequestDTO) {
        try {
            log.info("com.ai.code.controller.UserController.userRegister userRegisterRequestDTO:{}", userRegisterRequestDTO);
            return ResultUtils.success(userService.userRegister(
                    userRegisterRequestDTO.getUserAccount(),
                    userRegisterRequestDTO.getUserPassword(),
                    userRegisterRequestDTO.getCheckPassword()
            ));
        } catch (Exception e) {
            log.error("com.ai.code.controller.UserController.userRegister exception:", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequestDTO 登录参数
     * @param request           请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public BaseResponse<?> userLogin(@RequestBody @Valid UserLoginRequestDTO userLoginRequestDTO, HttpServletRequest request) {
        try {
            log.info("com.ai.code.controller.UserController.userLogin userLoginRequestDTO:{}", userLoginRequestDTO);
            ThrowUtils.throwIf(userLoginRequestDTO == null, ErrorCode.PARAMS_ERROR);
            return ResultUtils.success(userService.userLogin(
                    userLoginRequestDTO.getUserAccount(),
                    userLoginRequestDTO.getUserPassword(),
                    request
            ));
        } catch (Exception e) {
            log.error("com.ai.code.controller.UserController.userLogin exception:", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 登录结果
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getLoginUserVO(userService.getLoginUser(request)));
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 注销结果
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userLogout(request));
    }


}
