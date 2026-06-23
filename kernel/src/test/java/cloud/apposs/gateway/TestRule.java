package cloud.apposs.gateway;

import cloud.apposs.gateway.rules.*;
import cloud.apposs.gateway.rules.mvel.MVELRule;
import org.junit.Assert;
import org.junit.Test;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * MVEL2规则引擎测试，参考链接：
 * <pre>
 * https://blog.csdn.net/u012373815/article/details/108007640
 * </pre>
 */
public class TestRule {
    @Test
    public void testMvelRule() throws Exception {
        Rule rule = new MVELRule("weather rule mvel")
                .name("weather rule mvel")
                .priority(2)
                .description("if it rains then take an umbrella")
                .when("rain == true && hungry== true")
                .then("System.out.println(\"[MVELRule]It rains, take an umbrella!\");")
                .then("System.out.println(\"[MVELRule]It rains, take an umbrella! again!!\");");
        RulesEngine engine = new DefaultRulesEngine();
        Facts facts = new Facts();
        boolean expcted = true;
        facts.put("rain", expcted);
        facts.put("hungry", expcted);
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertEquals(expcted, result);
    }

    @Test
    public void testMvelRequest() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("(http.parameter['username'] == 'john_doe') && (http.parameter['password'] == '12345')");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelMatches() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("(http.parameter['username'] ~= 'john.*') && (http.parameter['password'] ~= '.*45')");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelStartsWith() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("(http.parameter['username'] .startsWith(\"john\")) && (http.parameter['password'] .startsWith(\"12\"))");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelEndsWith() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("(http.parameter['username'] .endsWith (\"doe\")) && (http.parameter['password'] .endsWith (\"45\"))");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelContains() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("(http.parameter['username'].contains(\"doe\")) && (http.parameter['password'].contains(\"45\"))");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelNotContains() throws Exception {
        Facts facts = new Facts();
        facts.put("http", new HashMap<String, Object>() {{
            put("parameter", new HashMap<String, Object>() {{
                put("username", "john_doe");
                put("password", "12345");
            }});
        }});
        Rule rule = new MVELRule("parameter rule mvel")
                .priority(2)
                .when("!(http.parameter['username'].contains(\"doe1\")) && !(http.parameter['password'].contains(\"451\"))");
        RulesEngine engine = new DefaultRulesEngine();
        Rules rules = new Rules();
        rules.register(rule);
        boolean result = engine.fire(rules, facts);
        System.out.println("result: " + result);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelCustomFunction() throws Exception {
        String expression = "1 == 1 && nameEqualIgnoreCase(name, targetName)";
        Map<String, Object> context = new HashMap<>();
        context.put("name", "AA");
        context.put("targetName", "aa");
        context.put("nameEqualIgnoreCase", TestRule.class.getDeclaredMethod("nameEqualIgnoreCase", String.class, String.class));
        boolean result = (Boolean) MVEL.eval(expression, context);
        Assert.assertTrue(result);
    }

    @Test
    public void testMvelIpIn() throws Exception {
        String expression = "ip .in (\"192.168.1.1\", \"192.168.1.2\")";
        Map<String, Object> context = new HashMap<>();
        context.put("ip", new Ips("192.168.1.1"));
        Serializable compiled = MVEL.compileExpression(expression);
        boolean result = (Boolean) MVEL.executeExpression(compiled, context);
        Assert.assertTrue(result);
    }

    public class Ips {
        private String ip;
        public Ips(String ip) {
            this.ip = ip;
        }
        public boolean in(String... ips) {
            for (String ip : ips) {
                if (ip.equals(this.ip)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean nameEqualIgnoreCase(String str1, String str2) {
        System.out.println("str1: " + str1 + ", str2: " + str2);
        return str1 != null && str1.equalsIgnoreCase(str2);
    }
}
