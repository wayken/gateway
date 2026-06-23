package cloud.apposs.gateway.config;

import cloud.apposs.util.Param;
import cloud.apposs.util.ReflectUtil;
import cloud.apposs.util.ResourceUtil;
import cloud.apposs.util.StrUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * YAML配置解析器
 */
public final class YamlLoader {
    /**
     * 将 YAML文件解析为指定对象数据结构
     * @param  object   原始对象，内部属性需要基于 {@link Value} 注解进行封装
     * @param  filename YAML文件
     */
    public static void parse(Object object, String filename) throws Exception {
        Param properties = load(filename);
        Class<?> clazz = object.getClass();
        do {
            Map<String, Method> methods = ReflectUtil.getDeclaredMethodMap(clazz);
            doParseOptional(properties, methods, object, clazz);
            clazz = clazz.getSuperclass();
        } while (clazz != null);
    }

    /**
     * 将 YAML文件解析为 Param 数据结构
     * @param  filename YAML文件
     * @return Param 数据结构
     */
    public static Param load(String filename) throws IOException {
        Param result = Param.builder();
        Yaml yaml = new Yaml();
        InputStream resource = ResourceUtil.getResource(filename);
        result.putAll(getFlattenedMap(yaml.loadAs(resource, Map.class)));
        return result;
    }

    private static Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        doBuildFlattenedMap(result, source, null);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void doBuildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        source.forEach((key, value) -> {
            if (StrUtil.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                }
                else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            }
            else if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                doBuildFlattenedMap(result, map, key);
            }
            else if (value instanceof Collection) {
                // Need a compound key
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                }
                else {
                    int count = 0;
                    for (Object object : collection) {
                        doBuildFlattenedMap(result, Collections.singletonMap(
                                "[" + (count++) + "]", object), key);
                    }
                }
            }
            else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }

    private static void doParseOptional(Param document, Map<String, Method> methods, Object object, Class<?> clazz) throws Exception {
        // 是否有指定要读取的JSON节点，默认从根节点开始解析
        if (document == null || document.size() <= 0) {
            return;
        }

        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            // 读取每个方法对应的 YAML 属性名
            String methodName = entry.getKey();
            Field propertyField = clazz.getDeclaredField(entry.getKey());
            Value methodAnnotation = propertyField.getAnnotation(Value.class);
            String propertyName = methodAnnotation != null ? methodAnnotation.value(): methodName;
            if (document.containsKey(propertyName)) {
                doParsePropertyNode(document, propertyName, object, clazz, entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean doParsePropertyNode(Param document, String methodName,
                                        Object model, Class<?> modelClazz, Method method) throws Exception {
        // 解析XML PROPERTY属性值并反射调用到类中
        Class<?>[] methodTypes = method.getParameterTypes();
        // setXXX(Object obj)方法必须要有参数
        if (methodTypes.length != 1) {
            return false;
        }

        if (ReflectUtil.isGenericType(methodTypes[0])) {
            // 对象属性为普通数据类型
            Object nodeVal = document.getObject(methodName);
            if (nodeVal != null) {
                method.invoke(model, nodeVal);
            }
        } else if (methodTypes[0].equals(List.class)) {
            // 对象属性为List对象
            Field field = modelClazz.getDeclaredField(methodName);
            field.setAccessible(true);
            // 获取List泛型类型
            ParameterizedType pt = (ParameterizedType)field.getGenericType();
            Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
            if (ReflectUtil.isGenericType(genericClazz)) {
                // List值为普通数据类型，直接赋值List
                Object nodeVal = document.getObject(methodName);
                if (nodeVal != null) {
                    method.invoke(model, nodeVal);
                }
            } else {
                // List值为自定义对象类型，递归解析对象并添加到List中
                List<Object> fieldList = (List<Object>) field.get(model);
                if (fieldList == null) {
                    fieldList = new LinkedList<Object>();
                    method.invoke(model, fieldList);
                }
                List<Param> childDocList = document.getList(methodName);
                Map<String, Method> modelMethods = ReflectUtil.getDeclaredMethodMap(genericClazz);
                for (Param childDoc : childDocList) {
                    Object fieldObject = genericClazz.newInstance();
                    doParseOptional(childDoc, modelMethods, fieldObject, fieldObject.getClass());
                    fieldList.add(fieldObject);
                }
            }
        } else if (methodTypes[0].equals(Map.class)) {
            // 对象属性为Map对象
            Field field = modelClazz.getDeclaredField(methodName);
            field.setAccessible(true);
            Map<Object, Object> fieldMap = (Map<Object, Object>) field.get(model);
            if (fieldMap == null) {
                fieldMap = new HashMap<Object, Object>();
                method.invoke(model, fieldMap);
            }
            // 获取Map泛型类型
            Type mapType = field.getGenericType();
            if (!ParameterizedType.class.isAssignableFrom(mapType.getClass()) &&
                    ((ParameterizedType) mapType).getActualTypeArguments().length != 2) {
                return false;
            }
            Type[] mapTypes = ((ParameterizedType) mapType).getActualTypeArguments();
            Class<?> valGenericClazz = (Class<?>)mapTypes[1];
            if (ReflectUtil.isGenericType(valGenericClazz)) {
                // Map值为普通数据类型，继续解析Map其下的键值对
                Param param = document.getParam(methodName);
                for (String key : param.keySet()) {
                    fieldMap.put(key, param.getObject(key));
                }
            } else {
                // Map值为自定义对象类型，递归解析对象并添加到Map中
                Param param = document.getParam(methodName);
                Map<String, Method> modelMethods = ReflectUtil.getDeclaredMethodMap(valGenericClazz);
                for (String key : param.keySet()) {
                    Object fieldObject = valGenericClazz.newInstance();
                    Param childDoc = param.getParam(key);
                    doParseOptional(childDoc, modelMethods, fieldObject, fieldObject.getClass());
                    fieldMap.put(key, fieldObject);
                }
            }
        } else {
            // 对象属性为自定义对象
            // 通过读取方法名在JSON中的配置递归解析JSON节点
            Field field = modelClazz.getDeclaredField(methodName);
            // 先获取属性上的值，没有则new一个对象，注意属性对象必须提供空构造函数
            field.setAccessible(true);
            Object fieldObject = field.get(model);
            // 属性值为空并且没有setXXX方法则不走反射
            if (fieldObject == null) {
                fieldObject = field.getType().newInstance();
                method.invoke(model, fieldObject);
            }
            // 有可能属性类继承新增了方法，需要重新获取
            Class<?> fieldClazz = fieldObject.getClass();

            Param childDoc = document.getParam(methodName);
            Map<String, Method> fieldMethods = ReflectUtil.getDeclaredMethodMap(fieldClazz);
            doParseOptional(childDoc, fieldMethods, fieldObject, fieldClazz);
        }

        return true;
    }
}
