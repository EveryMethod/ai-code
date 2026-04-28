package com.ai.code.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ai.code.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
