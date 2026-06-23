package cloud.apposs.gateway.watch.fs;

import cloud.apposs.gateway.watch.WatchEventType;
import cloud.apposs.gateway.watch.WatchListener;
import cloud.apposs.logger.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.*;

public class PluginMonitor implements Runnable {
    private final String watchDir;

    private final WatchService watchService;

    private final WatchListener watchListener;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public PluginMonitor(String watchDir, WatchListener watchListener) throws IOException {
        this.watchDir = watchDir;
        this.watchListener = watchListener;
        this.watchService = FileSystems.getDefault().newWatchService();
        handleFsPathsInit();
    }

    @Override
    public void run() {
        try {
            Paths.get(watchDir).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            while (running.get()) {
                final WatchKey key = watchService.take();
                try {
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        final WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (kind == OVERFLOW) {
                            continue;
                        }
                        if (kind == ENTRY_CREATE) {
                            Path watchable = (Path) key.watchable();
                            Path path = (Path) watchEvent.context();
                            String filePath = watchable.toString() + "/" + path.toString();
                            byte[] data = readFile(filePath);
                            watchListener.onNodeChanged(filePath, data, WatchEventType.ADDED);
                        } else if (kind == ENTRY_DELETE) {
                            Path watchable = (Path) key.watchable();
                            Path path = (Path) watchEvent.context();
                            String filePath = watchable.toString() + "/" + path.toString();
                            watchListener.onNodeChanged(filePath, null, WatchEventType.DELETED);
                        } else if (kind == ENTRY_MODIFY) {
                            Path watchable = (Path) key.watchable();
                            Path path = (Path) watchEvent.context();
                            String filePath = watchable.toString() + "/" + path.toString();
                            if (!Files.exists(Paths.get(filePath))) {
                                continue;
                            }
                            byte[] data = readFile(filePath);
                            watchListener.onNodeChanged(filePath, data, WatchEventType.UPDATED);
                        }
                    }
                } catch (Exception e) {
                    // 有可能配置解析有异常，输出错误后继续监听
                    Logger.warn(e, "Gateway Waf File Watch Service for Path: " + watchDir + " Error");
                }
                boolean valid = key.reset();
                if (!valid) {
                    Logger.warn("Gateway Waf File Watch Service for Path: " + watchDir + " key is invalid");
                }
            }
        } catch (Exception e) {
        }
    }

    private void handleFsPathsInit() throws IOException {
        Files.walkFileTree(Paths.get(watchDir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String filePath = file.toString();
                byte[] data = readFile(filePath);
                watchListener.onNodeChanged(filePath, data, WatchEventType.ADDED);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public void close() {
        try {
            running.set(false);
            watchService.close();
        } catch (Exception e) {
        }
    }
}
