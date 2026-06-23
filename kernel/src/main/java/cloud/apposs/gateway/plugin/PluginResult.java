package cloud.apposs.gateway.plugin;

public final class PluginResult {
    // 当前插件执行结果，通过则继续执行后续过滤器
    private final boolean success;

    // 当前插件执行不通过后是否拦截器自行处理，不抛出异常，true则不抛出异常，false则抛出异常
    private final boolean skip;

    // 没有触发任何规则，直接跳过
    public static final PluginResult SUCCESS = new PluginResult(true, false);
    // 触发了规则，由规则自行处理，不抛出异常
    public static final PluginResult SKIP = new PluginResult(true, true);
    // 出现了异常，直接跳过后续规则
    public static final PluginResult FAILURE = new PluginResult(false, false);

    public PluginResult(boolean success, boolean skip) {
        this.success = success;
        this.skip = skip;
    }

    public boolean success() {
        return success;
    }

    public boolean skip() {
        return skip;
    }
}
