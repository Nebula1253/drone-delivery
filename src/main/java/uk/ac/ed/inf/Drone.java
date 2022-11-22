package uk.ac.ed.inf;

import java.util.Arrays;

public class Drone {
    private Order[] ordersToDeliver;
    private final LngLat appletonTower = new LngLat(3.1870, 55.9444);

    public Drone(Order[] ordersToDeliver) {
        Arrays.sort(ordersToDeliver, (o1, o2) -> Double.compare(o2.getDeliveryLocation().distanceTo(appletonTower), o1.getDeliveryLocation().distanceTo(appletonTower)));
        this.ordersToDeliver = ordersToDeliver;
    }
}
