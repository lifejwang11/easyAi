package org.dromara.easyai.i;

import java.util.Map;

/**
 * @author lidapeng
 * @time 2025/4/9 09:55
 * @des 自定义编码模块
 */
public interface CustomEncoding {
    void backError(Map<Integer, Float> wg, int id);//返回最后一层误差
}
