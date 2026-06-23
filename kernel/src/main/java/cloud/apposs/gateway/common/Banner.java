package cloud.apposs.gateway.common;

import cloud.apposs.gateway.GatewayConstants;

import java.io.PrintStream;

public class Banner {
	private static final String[] BANNER = {
		"_________      _____                               ",
		"__  ____/_____ __  /________      _______ _____  __",
		"_  / __ _  __ `/  __/  _ \\_ | /| / /  __ `/_  / / /",
		"/ /_/ / / /_/ // /_ /  __/_ |/ |/ // /_/ /_  /_/ /",
		"\\____/  \\__,_/ \\__/ \\___/____/|__/ \\__,_/ _\\__, /",
		"========================================  /____/"
		};
	private static final String CLOUDX_BOOT = " :: Teambeit Gateway :: ";
	private static final int STRAP_LINE_SIZE = 48;
	
	public void printBanner(PrintStream printStream) {
		for (String line : BANNER) {
			printStream.println(line);
		}
		StringBuilder padding = new StringBuilder();
		while (padding.length() < STRAP_LINE_SIZE - (GatewayConstants.GATEWAY_VERSION.length() + CLOUDX_BOOT.length())) {
			padding.append(" ");
		}
		printStream.println(CLOUDX_BOOT + padding + GatewayConstants.GATEWAY_VERSION);
		printStream.println();
		printStream.flush();
	}
}
