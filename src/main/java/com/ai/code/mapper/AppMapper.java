package com.ai.code.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ai.code.model.entity.App;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用 映射层。
 *
 * @author alh
 * @since 2026-04-28 11:31:09
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {

}
