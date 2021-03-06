package com.bacovakuhinja.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "client_orders")
public class ClientOrder implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "co_id")
    private Integer orderId;

    @Column(name = "co_date")
    private Date date;

    @Column(name = "co_deadline")
    private Date deadline;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "co_reservation_id")
    private Reservation reservation;

    @Column(name = "co_client_id")
    private Integer clientId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "co_table_id")
    private RestaurantTable table;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE , CascadeType.REMOVE})
    private Set<OrderItem> items = new HashSet<OrderItem>(0);

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_bill_id")
    private Bill bill;

    @JsonIgnore
    @Column(name = "co_waiter_id")
    private Integer waiterId;

    @Column(name = "co_status")
    private String status;

    public ClientOrder(){
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public void setTable(RestaurantTable table) {
        this.table = table;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonIgnore
    public Set<OrderItem> getItems() {
        return items;
    }

    @JsonProperty
    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Integer getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(Integer waiterId) {
        this.waiterId = waiterId;
    }

    @Override
    public String toString() {
        return "ClientOrder{" +
                "orderId=" + orderId +
                ", date=" + date +
                ", deadline=" + deadline +
                ", table=" + table +
                ", items=" + items +
                '}';
    }

    @JsonIgnore
    public Reservation getReservation() {
        return reservation;
    }

    @JsonProperty
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
}
