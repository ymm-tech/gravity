package io.manbang.gravity.trade;

import java.util.Objects;

/**
 * 司机
 *
 * @author weilong.hu
 * @since 2022/05/19 10:25
 */
public class Driver {
    /**
     * 接单
     */
    public boolean acceptOrder(String address) {
        if (Objects.nonNull(address) && address.startsWith("南京市")) {
            return true;
        }
        return false;
    }

    /**
     * 装货
     */
    public String loadCargo() {
        return "load cargo success.";
    }

    /**
     * 运货
     */
    public String deliverCargo() {
        return "deliver cargo success.";
    }
}
