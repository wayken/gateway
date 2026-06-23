package cloud.apposs.gateway.global.ips;

import cloud.apposs.gateway.util.IpSubnetFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * IP地址信息封装
 */
public final class IpAddress {
    private final String id;

    private final String name;

    private final List<IpSubnetFilter> cidrList;

    public IpAddress(String id, String name, List<String> cidrList) {
        this.id = id;
        this.name = name;
        this.cidrList = new ArrayList<>(cidrList.size());
        for (String cidr : cidrList) {
            this.cidrList.add(new IpSubnetFilter(cidr));
        }
    }

    public String getId() {
        return id;
    }

    public List<IpSubnetFilter> getCidrList() {
        return cidrList;
    }

    public boolean update(IpAddress updatedAddress) {
        if (updatedAddress == null) {
            return false;
        }
        cidrList.clear();
        for (IpSubnetFilter cidr : updatedAddress.getCidrList()) {
            this.cidrList.add(cidr);
        }
        return true;
    }

    public String getName() {
        return name;
    }
}
