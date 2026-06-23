package cloud.apposs.gateway.dashboard;

import cloud.apposs.bootor.banner.Banner;

import java.io.PrintStream;

public class GatewayDashboardBanner implements Banner {
    private static final String[] BANNER = {
            "___________       _________              ______ ______                     _________",
            "__  ____/_ |     / /__  __ \\_____ __________  /____  /_____________ _____________  /",
            "_  / __ __ | /| / /__  / / /  __ `/_  ___/_  __ \\_  __ \\  __ \\  __ `/_  ___/  __  / ",
            "/ /_/ / __ |/ |/ / _  /_/ // /_/ /_(__  )_  / / /  /_/ / /_/ / /_/ /_  /   / /_/ /  ",
            "\\____/  ____/|__/  /_____/ \\__,_/ /____/ /_/ /_//_.___/\\____/\\__,_/ /_/    \\__,_/   "
    };
    private static final String CLOUDX_BOOT = " :: Gateway Dashboard :: ";
    private static final int STRAT_LINE_SIZE = 38;

    @Override
    public void printBanner(PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAT_LINE_SIZE - (GatewayDashboardConstants.GATEWAY_VERSION.length() + CLOUDX_BOOT.length())) {
            padding.append(" ");
        }
        printStream.println(CLOUDX_BOOT + padding + GatewayDashboardConstants.GATEWAY_VERSION);
        printStream.println();
        printStream.flush();
    }
}
