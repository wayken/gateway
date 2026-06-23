package cloud.apposs.gateway;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    @Test
    public void testMatchReplace() throws Exception {
        // 示例输入
        String input = "http://aaa123/test.html";

        // 正则表达式匹配规则
        // 第一个捕获组：(aa.*) 表示主机名，如 aaa123
        // 第二个捕获组：(.*) 表示文件名，如 test
        Pattern pattern = Pattern.compile("http://aa(.*)/(.*)\\.html");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            // 替换规则，需要所有group都匹配
            String result = matcher.replaceAll("https://bb$1/cc$2");
            System.out.println(result); // 输出: https://bbaaa123/cctest
        } else {
            System.out.println("No match found.");
        }
    }
}
