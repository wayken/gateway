package cloud.apposs.gateway.hotloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 动态类加载器，用于动态加载类文件
 */
public class DynamicClassLoader {
    private final ClassLoader classLoader;

    public DynamicClassLoader(File resource) throws IOException {
        URL url = resource.toURI().toURL();
        this.classLoader = new URLClassLoader(new URL[]{url}, DynamicClassLoader.class.getClassLoader());
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return classLoader.loadClass(name);
    }
}
