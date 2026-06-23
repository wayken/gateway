package cloud.apposs.gateway.hotloader;

import cloud.apposs.gateway.GatewayContext;
import cloud.apposs.gateway.plugin.Plugin;
import cloud.apposs.ioc.BeanDefinition;
import cloud.apposs.ioc.annotation.Component;
import cloud.apposs.logger.Logger;
import cloud.apposs.util.CharsetUtil;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HotLoadRunner {
    /** 热加载的 LIB 包路径 */
    private final String hotLoadPath;

    private final GatewayContext context;

    public HotLoadRunner(String hotLoadPath, GatewayContext context) {
        this.hotLoadPath = hotLoadPath;
        this.context = context;
    }

    /**
     * 扫描热加载目录下的所有文件，
     * 如果是Jar文件则加载插件到网关IOC容器中方便后续路由使用，
     * 为了避免插件加载失败导致网关服务启动失败，所有插件加载失败不会影响网关服务启动
     *
     * @return 是否扫描成功
     */
    public boolean scan() {
        try {
            Path path = handleLoadPath(hotLoadPath);
            boolean exists = Files.exists(path);
            if (!exists) {
                Logger.warn("HotLoadRunner Scan for Path %s not exists", path);
                return false;
            }
            File file = path.toFile();
            if (!file.isDirectory()) {
                return false;
            }
            File[] files = file.listFiles();
            if (files == null) {
                return false;
            }
            for (File resource : files) {
                if (resource.isFile() && resource.getName().endsWith(".jar")) {
                    handlePluginAdd(path, resource.toPath());
                }
            }
            return true;
        } catch (Exception e) {
            Logger.error(e, "HotLoadRunner Scan for Path: " + hotLoadPath + " failed");
            return false;
        }
    }

    /**
     * 处理插件的添加
     *
     * @param  watchable 监听的目录
     * @param  path      监听的文件
     * @return 是否处理成功
     */
    private boolean handlePluginAdd(Path watchable, Path path) throws Exception {
        File file = handleLoadPluginFile(watchable, path);
        if (file == null) {
            return false;
        }
        Logger.info("HotLoadRunner for Path: " + file.getAbsolutePath() + " start add hotload");
        // 解析Jar文件中添加 Component.class 注解的插件类
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
            DynamicClassLoader classLoader = new DynamicClassLoader(file);
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String jarEntryName = jarEntry.getName();
                if (!jarEntryName.endsWith(".class")) {
                    continue;
                }
                String className = jarEntryName.replace("/", ".").substring(0, jarEntryName.length() - 6);
                if (handleInitialBeanDefinition(className, classLoader)) {
                    Logger.info("HotLoadRunner for JarFile: " + file.getName() + " Load class " + className + " Success");
                }
            }
            return true;
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }

    private File handleLoadPluginFile(Path watchable, Path path) throws Exception {
        String pluginFilePath = path.toString();
        if (!path.isAbsolute()) {
            pluginFilePath = watchable.toString() + File.separator + path.toString();
            pluginFilePath = URLDecoder.decode(pluginFilePath, CharsetUtil.UTF_8.name());
        }
        // 当前文件为Jar文件则进行热加载
        File file = new File(pluginFilePath);
        if (!file.isFile() || !file.getName().endsWith(".jar")) {
            return null;
        }
        return file;
    }

    /**
     * 新增插件的BeanDefinition，方便后续IOC容器进行统一创建
     *
     * @param  className   插件类名
     * @param  classLoader 类加载器
     * @return 是否处理成功
     */
    private boolean handleInitialBeanDefinition(String className, DynamicClassLoader classLoader) throws Exception {
        // 通过动态类加载器加载类
        Class<?> clazz = classLoader.loadClass(className);
        // 判断是否是插件类，插件类必须是Component注解标注并且实现了Plugin接口的类
        boolean isPluginMatched = !clazz.isInterface() && !clazz.isAnnotation() && !clazz.isEnum()
                && Plugin.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Component.class);
        if (!isPluginMatched) {
            return false;
        }
        // 添加BeanDefinition，方便后续IOC容器进行统一创建
        BeanDefinition beanDefinition = new BeanDefinition(clazz, false);
        context.getCompiler().addBeanDefinition(className, beanDefinition);
        return true;
    }

    /**
     * 获取热加载目录，如果是相对路径则转换以当前项目为父目录转换成绝对路径
     *
     * @param  hotLoadPath 热加载目录
     * @return 转换后的绝对路径
     */
    private static Path handleLoadPath(String hotLoadPath) {
        Path path = Paths.get(hotLoadPath);
        if (!path.isAbsolute()) {
            String rootPath = System.getProperty("user.dir");
            path = Paths.get(rootPath, hotLoadPath);
        }
        return path;
    }
}
