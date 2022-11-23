package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Drone {
    private ArrayList<Order> ordersToDeliver;
    private final LngLat appletonTower = new LngLat(3.1870, 55.9444);
    private int nr_moves = 2000;
    private String orderDate;

    public Drone(Order[] ordersToDeliver, String orderDate) {
        //Arrays.sort(ordersToDeliver, (o1, o2) -> Double.compare(o2.getDeliveryLocation().distanceTo(appletonTower), o1.getDeliveryLocation().distanceTo(appletonTower)));
        this.ordersToDeliver = new ArrayList<>(List.of(ordersToDeliver));
        this.orderDate = orderDate;
    }

    public void deliverOrders() {
        while (ordersToDeliver.size() > 0) {
            Order currentOrd = ordersToDeliver.remove(0);
            // deliver order

        }

        DataManager.writeToJSONFile("deliveries-" + orderDate + ".json", ordersToDeliver);
    }
}
