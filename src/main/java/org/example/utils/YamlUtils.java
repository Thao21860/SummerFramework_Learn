package org.example.utils;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlUtils {

    public static Map<String, Object> loadYamlAsPlainMap(){
        Map<String,String> res  = new HashMap<>();
        Yaml yaml = new Yaml();
        // 输入流
        InputStream stream = YamlUtils.class.getClassLoader().
                getResourceAsStream("/application.yml");
        // object 转为 string
        // yaml读取结果为多层级的map，需要拉平
        Map<String, Object> yamMap = yaml.load(stream);
        yamMap = flatten(yamMap);
        return yamMap;
    }

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return flatten(map, null);
    }

    public static String flattenToString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append(key).append("=").append(value).append("\n"));
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> flatten(Map<String, Object> map, String prefix) {
        Map<String, Object> flatMap = new HashMap<>();
        map.forEach((key, value) -> {
            String newKey = prefix != null ? prefix + "." + key : key;
            if (value instanceof Map) {
                flatMap.putAll(flatten((Map<String, Object>) value, newKey));
            } else if (value instanceof List) {
//                List<Map<String, Object>> list = (List<Map<String, Object>>) value;
                List<Object> list = (List<Object>) value;
                // list内仍然会有map
                for (int i = 0; i < list.size(); i++) {
                    Object subEle = list.get(i);
                    if(subEle instanceof String){
                        flatMap.put(newKey +  "[" + i + "]", subEle);
                    }
                    else if (subEle instanceof Map){
                        flatMap.putAll(flatten((Map<String, Object>) list.get(i), newKey + "[" + i + "]"));
                    }
                }
            } else {
                // value 为空时丢弃
                if(value != null) flatMap.put(newKey, value);
            }
        });
        return flatMap;
    }

}
