package io.manbang.gravity.trade;

/**
 * @author weilong.hu
 * @since 2022/05/19 10:48
 */
public class Order {

    public static void main(String[] args) {
        new Order().trade();
    }

    public void trade() {
        final Shippers shippers = new Shippers();
        final Driver driver = new Driver();
        final String address = shippers.postOrder();
        driver.acceptOrder(address);
        driver.loadCargo();
        driver.deliverCargo();
    }
}
