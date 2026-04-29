package com.ai.code.core.parse;

/**
 * @author alh
 * @description 代码解析器策略接口
 * @date 2026/4/29 17:13
 */
public interface CodeParse<T> {

    /**
     * 解析代码
     * @param codeContent 代码内容
     * @return 解析结果
     */
    T parseCode(String codeContent);
}
