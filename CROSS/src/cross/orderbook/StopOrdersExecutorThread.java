package cross.orderbook;

import java.util.LinkedList;

import cross.api.notifications.Notification;
import cross.api.notifications.Trade;
import cross.exceptions.InvalidOrder;
import cross.orders.MarketOrder;
import cross.orders.StopOrder;
import cross.orders.db.Orders;
import cross.users.db.Users;

public class StopOrdersExecutorThread extends Thread {

    private OrderBook orderBook;

    private Boolean isRunning = true;

    public StopOrdersExecutorThread(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.isRunning = true;
    }

    public void stopRunning() {
        this.isRunning = false;
    }

    @Override
    public void run() {

        Thread.currentThread().setName(this.getClass().getSimpleName());

        LinkedList<MarketOrder> sharedList = this.orderBook.getStopNowMarketOrdersToExecute();

        while (this.isRunning) {

            LinkedList<MarketOrder> ordersToProcessThisCycle = new LinkedList<>();

            // synch to prevent main thread from adding new stop orders after market orders execution and make concurrent modifications.
            synchronized (sharedList) {

                    while (sharedList.isEmpty() && this.isRunning) {
                        try {
                            if (this.orderBook.getVerboseLogging()) {
                                System.out.println("\n\n\n\n\n\n\n\n\n");
                                System.out.printf("DEBUG: %s is WAITING, NO STOP, NOW MARKET ORDERS TO EXECUTE.\n", this.getClass().getSimpleName());
                                System.out.println("\n\n\n\n\n\n\n\n\n");
                            }
                            sharedList.notify();
                            sharedList.wait();
                        } catch (InterruptedException ex) {
                            // I have been woken up by the main thread to work or interrupted to stop.
                            if (this.orderBook.getVerboseLogging()) {
                                System.out.println("\n\n\n\n\n\n\n\n\n");
                                System.out.printf("DEBUG: %s was INTERRUPTED.\n", this.getClass().getSimpleName());
                            }
                            if (!this.isRunning) {
                                return;
                            }
                        }
                    }

                    ordersToProcessThisCycle.addAll(sharedList);
                    sharedList.clear();

                    if (this.orderBook.getVerboseLogging()) {
                        for (MarketOrder marketOrder : ordersToProcessThisCycle) {
                            System.out.printf("DEBUG: %s will EXECUTE the following STOP, NOW MARKET ORDER: %s.\n", this.getClass().getSimpleName(), marketOrder.toString());
                        }
                    }

                    for (MarketOrder marketOrder : ordersToProcessThisCycle) {
                        if (this.orderBook.getVerboseLogging()) {
                            System.out.printf("DEBUG: %s EXECUTING the following STOP, NOW MARKET ORDER: %s.\n", this.getClass().getSimpleName(), marketOrder.toString());
                        }
                        Boolean executed = false;
                        try {
                            executed = this.orderBook.executeOrder(marketOrder);
                            StopOrder originalStop = null;
                            originalStop = (StopOrder) Orders.getOrderById(marketOrder.getComingFromStopOrderId());
                            if (executed) {
                                if (originalStop != null) {
                                    originalStop.setQuantity(marketOrder.getQuantity());
                                    originalStop.setTimestamp(marketOrder.getTimestamp());
                                }
                            } else{
                                // Unsatisfied stop order.
                                // On db orders file is not written. It's written the corresponding market order after execution.
                                // Only need to remove from the RAM.
                                Orders.removeOrderById(marketOrder.getComingFromStopOrderId());
                                originalStop.setId(-1);
                                Notification notification = new Notification();
                                Trade notExecutedStop = new Trade(originalStop);
                                notification.addTrade(notExecutedStop);
                                Users.notifyUsers(notification);
                            }
                        } catch (NullPointerException | IllegalArgumentException | IllegalStateException| InvalidOrder ex) {
                            System.err.println("Error executing the STOP, NOW MARKET ORDER. Trying to continue skipping it.");
                            continue;
                        }
                        if (executed) { 
                            if (this.orderBook.getVerboseLogging()) {
                                System.out.printf("DEBUG: %s EXECUTED a STOP, NOW MARKET ORDER: %s.\n", this.getClass().getSimpleName(), marketOrder.toString());
                                System.out.println("\n\n\n\n\n\n\n\n\n");
                            }
                        }else {
                            if (this.orderBook.getVerboseLogging()) {
                                System.out.printf("DEBUG: %s DID NOT EXECUTE a STOP, NOW MARKET, INSATISFIED ORDER: %s.\n", this.getClass().getSimpleName(), marketOrder.toString());
                                System.out.println("\n\n\n\n\n\n\n\n\n");
                            }
                        }
                    }

            }

        }

    }
    
    
}
