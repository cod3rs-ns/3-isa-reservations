package com.bacovakuhinja.controller;

import com.bacovakuhinja.annotations.Authorization;
import com.bacovakuhinja.model.*;
import com.bacovakuhinja.service.*;
import com.bacovakuhinja.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class OrderController {

    @Autowired
    private RestaurantTableService tableService;

    @Autowired
    private ClientOrderService clientOrderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private WaiterService waiterService;

    @Autowired
    private ReservationService reservationService;

    @RequestMapping(
            value = "api/orders/{table_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection<ClientOrder>> getOrdersOfTable(@PathVariable("table_id") Integer tableId) {
        Collection<ClientOrder> tableOrders = clientOrderService.getOrdersForBill(tableId);
        return new ResponseEntity <Collection <ClientOrder>>(tableOrders, HttpStatus.OK);
    }

    @RequestMapping(
            value = "api/orders/reserved/{table_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection<ClientOrder>> getOrdersFromReservation(@PathVariable("table_id") Integer tableId) {
        Collection<ClientOrder> tableOrders = clientOrderService.getOrdersFromReservation(tableId);
        return new ResponseEntity <Collection <ClientOrder>>(tableOrders, HttpStatus.OK);
    }


    @RequestMapping(
            value = "api/order/{order_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <ClientOrder> getOrderById(@PathVariable("order_id") Integer orderId) {
        ClientOrder order = clientOrderService.findOne(orderId);
        return new ResponseEntity <ClientOrder>(order, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/orders/{restaurantId}/{tableId}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOrder> addOrder(@RequestBody ClientOrder order, @PathVariable("tableId") Integer tableId, @PathVariable("restaurantId") Integer restaurantId) {
        RestaurantTable table = tableService.findOne(tableId);
        order.setTable(table);

        ClientOrder newOrder = createOrder(order, restaurantId);

        return new ResponseEntity <ClientOrder>(newOrder, HttpStatus.OK);
    }

    private ClientOrder createOrder(ClientOrder order, int restaurantId){
        if (order.getReservation() != null) {
            order.setReservation(reservationService.findOne(order.getReservation().getReservationId()));
        }
        ClientOrder newOrder = clientOrderService.create(order);

        // Notify for new items
        HashMap<String, ArrayList<OrderItem>> foodMap = new HashMap<String, ArrayList<OrderItem>>();
        ArrayList<OrderItem> foodList = new ArrayList<OrderItem>();
        HashMap<String, ArrayList<OrderItem>> drinkMap = new HashMap<String, ArrayList<OrderItem>>();
        ArrayList<OrderItem> drinkList = new ArrayList<OrderItem>();

        for (Iterator<OrderItem> it = order.getItems().iterator(); it.hasNext(); ) {
            OrderItem i = it.next();
            i.setOrder(newOrder);
            i.setMenuItem(menuItemService.findOne(i.getMenuItem().getMenuItemId()));
            i.setRestaurantId(restaurantId);
            i.setTableId(newOrder.getTable().getTableId());
            OrderItem nItem = orderItemService.create(i);
            if (nItem.getMenuItem().getType().equals(Constants.MenuItemType.FOOD))
                foodList.add(nItem);
            else
                drinkList.add(nItem);
        }

        foodMap.put(Constants.NotificationOrderStatus.NEW, foodList);
        drinkMap.put(Constants.NotificationOrderStatus.NEW, drinkList);
        this.template.convertAndSend("/subscribe/ActiveFood/" + restaurantId, foodMap);
        this.template.convertAndSend("/subscribe/ActiveDrink/" + restaurantId, drinkMap);

        return newOrder;
    }


    @RequestMapping(
            value = "/api/orders/{restaurantId}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOrder> editOrder(@RequestBody ClientOrder order, @PathVariable("restaurantId") Integer restaurantId) {

        ClientOrder oldOrder = clientOrderService.findOne(order.getOrderId());
        ClientOrder updated = updateOrder(order, oldOrder, restaurantId);
        clientOrderService.update(updated);

        return new ResponseEntity <ClientOrder>(updated, HttpStatus.OK);
    }

    private ClientOrder updateOrder(ClientOrder newOrder, ClientOrder oldOrder, int r_id){
        Set<OrderItem> oldItems = new HashSet<OrderItem>();
        oldItems.addAll(oldOrder.getItems());

        // Notify for new items
        HashMap<String, ArrayList<OrderItem>> foodMap = new HashMap<String, ArrayList<OrderItem>>();
        ArrayList<OrderItem> foodNew = new ArrayList<OrderItem>();
        ArrayList<OrderItem> foodRemove = new ArrayList<OrderItem>();
        ArrayList<OrderItem> foodUpdate = new ArrayList<OrderItem>();

        // Notify for new items
        HashMap<String, ArrayList<OrderItem>> drinksMap = new HashMap<String, ArrayList<OrderItem>>();
        ArrayList<OrderItem> drinkNew = new ArrayList<OrderItem>();
        ArrayList<OrderItem> drinkRemove = new ArrayList<OrderItem>();
        ArrayList<OrderItem> drinkUpdate = new ArrayList<OrderItem>();


        Set<OrderItem> newItems = new HashSet<OrderItem>();

        for (Iterator<OrderItem> itNew = newOrder.getItems().iterator(); itNew.hasNext(); ) {
            OrderItem newItem = itNew.next();
            boolean flag = false;
            for(Iterator<OrderItem> itOld = oldItems.iterator(); itOld.hasNext();){
                OrderItem oldItem = itOld.next();
                if(oldItem.getMenuItem().getMenuItemId() == newItem.getMenuItem().getMenuItemId()){
                    if(oldItem.getAmount() != newItem.getAmount()) {
                        oldItem.setAmount(newItem.getAmount());
                        if (oldItem.getMenuItem().getType().equals(Constants.MenuItemType.FOOD))
                            foodUpdate.add(oldItem);
                        else
                            drinkUpdate.add(oldItem);
                    }
                    newItems.add(oldItem);
                    flag = true;
                    break;
                }
            }

            if(!flag) {
                newItem.setOrder(oldOrder);
                newItem.setRestaurantId(r_id);
                newItem.setMenuItem(menuItemService.findOne(newItem.getMenuItem().getMenuItemId()));
                newItem.setTableId(oldOrder.getTable().getTableId());
                OrderItem item = orderItemService.create(newItem);
                newItems.add(item);
                if(newItem.getMenuItem().getType().equals(Constants.MenuItemType.FOOD))
                    foodNew.add(newItem);
                else
                    drinkNew.add(newItem);
            }
        }

        oldOrder.getItems().clear();
        oldOrder.getItems().addAll(newItems);
        oldItems.removeAll(newItems);

        for(Iterator<OrderItem> itOld = oldItems.iterator(); itOld.hasNext();){
            OrderItem oi= itOld.next();
            if(oi.getMenuItem().getType().equals(Constants.MenuItemType.FOOD))
                foodRemove.add(oi);
            else
                drinkRemove.add(oi);
            orderItemService.delete(oi.getItemId());
        }

        foodMap.put(Constants.NotificationOrderStatus.NEW, foodNew);
        foodMap.put(Constants.NotificationOrderStatus.REMOVE , foodRemove);
        foodMap.put(Constants.NotificationOrderStatus.UPDATE, foodUpdate);

        drinksMap.put(Constants.NotificationOrderStatus.NEW, drinkNew);
        drinksMap.put(Constants.NotificationOrderStatus.REMOVE , drinkRemove);
        drinksMap.put(Constants.NotificationOrderStatus.UPDATE, drinkUpdate);

        this.template.convertAndSend("/subscribe/ActiveFood/" + r_id, foodMap);
        this.template.convertAndSend("/subscribe/ActiveDrink/" + r_id, drinksMap);

        return oldOrder;
    }

    @RequestMapping(
            value = "api/order/canEdit/{order_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Boolean> canEdit(@PathVariable("order_id") Integer orderId) {
        ClientOrder order = clientOrderService.findOne(orderId);
        for(OrderItem oi : order.getItems()){
            if(!oi.getState().equals(Constants.OrderStatus.CREATED)){
                return new ResponseEntity <Boolean>(false, HttpStatus.OK);
            }
        }
        return new ResponseEntity <Boolean>(true, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/order/{orderId}",
            method = RequestMethod.DELETE
    )
    public void deleteOrder(@PathVariable("orderId") Integer orderId) {
        ClientOrder order = clientOrderService.findOne(orderId);

        if (order == null) return;

        clientOrderService.delete(order.getOrderId());
    }

    @Authorization(role = Constants.UserRoles.WAITER)
    @RequestMapping(
            value = "/api/orders/setWaiter/{orderId}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOrder> setOrderWaiter(final HttpServletRequest request, @PathVariable("orderId") Integer orderId) {
        User user = (User) request.getAttribute(Constants.Authorization.LOGGED_USER);
        Waiter waiter = waiterService.findOne(user.getUserId());
        ClientOrder order = clientOrderService.findOne(orderId);
        order.setWaiterId(waiter.getUserId());
        ClientOrder updated = clientOrderService.update(order);
        return new ResponseEntity<ClientOrder>(updated, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/orders/changeStatus/{orderId}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientOrder> changeStatus(@PathVariable("orderId") Integer orderId) {
        ClientOrder oldOrder = clientOrderService.findOne(orderId);
        oldOrder.setStatus(Constants.OrderStatus.ACCEPTED);
        ClientOrder updated = clientOrderService.update(oldOrder);

        return new ResponseEntity <ClientOrder>(updated, HttpStatus.OK);
    }

}

