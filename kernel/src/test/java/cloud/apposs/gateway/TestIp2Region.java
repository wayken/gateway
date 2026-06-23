package cloud.apposs.gateway;

import com.alibaba.nacos.common.packagescan.resource.ClassPathResource;
import com.alibaba.nacos.common.packagescan.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.InputStream;

public class TestIp2Region {
    private Searcher searcher;

    @Before
    public void setup() throws Exception {
        Resource resource = new ClassPathResource("/ip2region.xdb");
        InputStream ris = resource.getInputStream();
        byte[] dbBinStr = new byte[ris.available()];
        ris.read(dbBinStr);
        searcher = Searcher.newWithBuffer(dbBinStr);
    }

    @Test
    public void testIpPrint() throws Exception {
        String region = searcher.search("127.0.0.1");
        System.out.println(region);
    }
}
