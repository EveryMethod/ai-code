package com.ai.code.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.code.ai.model.HtmlCodeResult;
import com.ai.code.ai.model.MultiFileCodeResult;
import com.ai.code.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author alh
 * @description 代码文件保存服务
 * @date 2026/4/29 14:51
 */
@Deprecated
public class CodeFileSaver {

    /**
     * 代码保存根路径
     */
    public static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";


    /**
     * 保存HtmlCodeResult
     *
     * @param result HtmlCodeResult
     * @return 保存的文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        saveFile(baseDirPath, result.getHtmlCode(), "index.html");
        return new File(baseDirPath);
    }

    /**
     * 保存MultiFileCodeResult
     *
     * @param result MultiFileCodeResult
     * @return 保存的文件
     */
    public static File saveMultiFileResult(MultiFileCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveFile(baseDirPath, result.getHtmlCode(), "index.html");
        saveFile(baseDirPath, result.getCssCode(), "style.css");
        saveFile(baseDirPath, result.getJsCode(), "script.js");
        return new File(baseDirPath);
    }


    /**
     * 构建唯一目录
     *
     * @param bizType 业务类型
     * @return 唯一目录
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存文件
     *
     * @param filePath 文件路径
     * @param content  文件内容
     * @param fileName 文件名
     */
    private static void saveFile(String filePath, String content, String fileName) {
        String path = filePath + File.separator + fileName;
        FileUtil.writeString(content, path, StandardCharsets.UTF_8);
    }
}
