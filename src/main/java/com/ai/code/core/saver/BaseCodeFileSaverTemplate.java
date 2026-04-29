package com.ai.code.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.code.exception.BusinessException;
import com.ai.code.exception.ErrorCode;
import com.ai.code.model.enums.CodeGenTypeEnum;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author alh
 * @description 代码文件保存模版方法类
 * @date 2026/4/29 20:41
 */
public abstract class BaseCodeFileSaverTemplate<T> {

    /**
     * 代码保存根路径
     */
    public static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存代码标准实现 模版方法
     *
     * @param result 代码生成结果
     * @return 保存的文件目录对象
     */
    public final File saveCode(@NotNull T result) {
        // 验证输入
        validInput(result);
        // 构建唯一路径
        String baseDirPath = buildUniqueDir();
        // 保存文件，具体实现交给子类
        saveFile(result, baseDirPath);
        // 返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     * 保存文件，具体实现交给子类
     *
     * @param result     代码生成结果
     * @param baseDirPath 基础目录路径
     */
    protected abstract void saveFile(T result, String baseDirPath);

    /**
     * 基础校验，具体实现交给子类
     *
     * @param result 代码生成结果
     */
    protected void validInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存代码结果不能为空");
        }
    }


    /**
     * 构建唯一目录
     *
     * @return 唯一目录
     */
    protected String buildUniqueDir() {
        String bizType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 获取代码生成类型 交由子类实现
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存文件
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @param fileName 文件名
     */
    public void saveFile(String filePath, String content, String fileName) {
        if (StringUtils.isNotBlank(content)) {
            String path = filePath + File.separator + fileName;
            FileUtil.writeString(content, path, StandardCharsets.UTF_8);
        }
    }

}
