package org.dromara.easyai.randomForest;


import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidapeng
 * @description 内存数据表
 * @date 3:48 下午 2020/2/17
 */
public class DataTable {//数据表
    private final Map<String, List<Integer>> table = new ConcurrentHashMap<>();
    private final Set<String> keyType;//表的属性
    private String key;//最终分类字段
    private int length;

    public String getKey() {
        return key;
    }

    public int getLength() {
        return length;
    }

    public int getSize() {//获取属性的数量
        return keyType.size();
    }

    public Map<String, List<Integer>> getTable() {
        return table;
    }

    public Set<String> getKeyType() {
        return keyType;
    }

    public void setKey(String key) throws Exception {
        if (keyType.contains(key)) {
            this.key = key;
        } else {
            throw new Exception("NOT FIND KEY");
        }
    }

    public DataTable(Set<String> key) throws Exception {//表的属性
        if (key != null) {
            this.keyType = key;
            for (String name : key) {
                if (name == null) {
                    throw new Exception("属性列表中，有一个属性的值为null");
                }
                table.put(name, new ArrayList<>());
            }
        } else {
            throw new Exception("属性列表为空");
        }
    }

    public void insert(Object ob) {
        try {
            Class<?> body = ob.getClass();
            length++;
            for (String name : keyType) {
                String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Method method = body.getMethod(methodName);
                Object dm = method.invoke(ob);
                List<Integer> list = table.get(name);
                if (dm instanceof Integer) {//数据表只允许加入Integer类型数据
                    list.add((int) dm);
                } else {
                    throw new Exception("数据表只允许加入Integer类型数据");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
