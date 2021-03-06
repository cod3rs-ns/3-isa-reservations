package com.bacovakuhinja.service;

import com.bacovakuhinja.model.ReservationGuest;

import java.util.Collection;

public interface ReservationGuestService {

    Collection<ReservationGuest> findAll();

    Collection<ReservationGuest> findAllByReservationAndStatus(Integer reservationId, String status);

    boolean isOwner(Integer reservationId, Integer userId);

    boolean isInvited(Integer reservationId, Integer userId);

    boolean isAccepted(Integer reservationId, Integer userId);

    ReservationGuest acceptInvitation(Integer reservationId, Integer userId);

    ReservationGuest declineInvitation(Integer reservationId, Integer userId);

    ReservationGuest create(ReservationGuest reservationGuest);

    ReservationGuest update(ReservationGuest reservationGuest);

    String getOwner(Integer reservationId);

    void delete(ReservationGuest reservationGuest);

    Integer numberOfReservationGuests(Integer reservationId);
}
