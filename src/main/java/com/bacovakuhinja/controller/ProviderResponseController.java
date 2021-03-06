package com.bacovakuhinja.controller;

import com.bacovakuhinja.model.ProviderResponse;
import com.bacovakuhinja.model.RestaurantProvider;
import com.bacovakuhinja.service.OfferRequestService;
import com.bacovakuhinja.service.ProviderResponseService;
import com.bacovakuhinja.service.RestaurantProviderService;
import com.bacovakuhinja.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;

@RestController
public class ProviderResponseController {

    @Autowired
    private ProviderResponseService responseService;

    @Autowired
    private OfferRequestService offerRequestService;

    @Autowired
    private RestaurantProviderService providerService;

    @RequestMapping(
            value = "/api/provider_responses",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection <ProviderResponse>> getProviderResponses() {
        Collection <ProviderResponse> responses = responseService.findAll();
        return new ResponseEntity <Collection <ProviderResponse>>(responses, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/provider_responses/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <ProviderResponse> getProviderResponse(@PathVariable("id") Integer id) {
        ProviderResponse response = responseService.findOne(id);
        return new ResponseEntity <ProviderResponse>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/provider_responses",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <ProviderResponse> createProviderResponse(@RequestBody ProviderResponse providerResponse) {
        ProviderResponse answer;
        if (providerResponse.getOffer().getStatus().equals(Constants.OfferStatus.CLOSED) || providerResponse.getOffer().getDeadline().before(new Date()) || providerResponse.getStatus().equals(Constants.ResponseStatus.REJECTED)) {
            answer = null;
            return new ResponseEntity <ProviderResponse>(answer, HttpStatus.NO_CONTENT);
        }
        ProviderResponse created = responseService.create(providerResponse);
        return new ResponseEntity <ProviderResponse>(created, HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/api/provider_responses",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <ProviderResponse> updateProviderResponse(@RequestBody ProviderResponse providerResponse) {
        ProviderResponse answer;
        ProviderResponse sent = responseService.findOne(providerResponse.getResponseId());
        if (sent.getOffer().getStatus().equals(Constants.OfferStatus.CLOSED) || sent.getOffer().getDeadline().before(new Date()) || sent.getStatus().equals(Constants.ResponseStatus.REJECTED)) {
            answer = null;
        } else {
            ProviderResponse updatedResponse = responseService.update(providerResponse);
            if (updatedResponse == null) {
                return new ResponseEntity <ProviderResponse>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity <ProviderResponse>(updatedResponse, HttpStatus.OK);
        }
        return new ResponseEntity <ProviderResponse>(answer, HttpStatus.NO_CONTENT);

    }

    @RequestMapping(
            value = "/api/provider_responses/{id}",
            method = RequestMethod.DELETE)
    public ResponseEntity <ProviderResponse> deleteProviderResponse(@PathVariable("id") Integer id) {
        ProviderResponse answer;
        ProviderResponse sent = responseService.findOne(id);
        if (sent.getOffer().getStatus().equals(Constants.OfferStatus.CLOSED) || sent.getOffer().getDeadline().before(new Date()) || sent.getStatus().equals(Constants.ResponseStatus.REJECTED)) {
            answer = null;
            return new ResponseEntity <ProviderResponse>(answer, HttpStatus.NO_CONTENT);
        }
        responseService.delete(id);
        return new ResponseEntity <ProviderResponse>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(
            value = "api/provider_responses/o={offer_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection <ProviderResponse>> getResponsesByOffer(@PathVariable("offer_id") Integer id) {
        Collection <ProviderResponse> providerResponses = responseService.findAllByOffer(offerRequestService.findOne(id));
        return new ResponseEntity <Collection <ProviderResponse>>(providerResponses, HttpStatus.OK);
    }

    @RequestMapping(
            value = "api/provider_responses/p={provider_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection <ProviderResponse>> getResponsesByProvider(@PathVariable("provider_id") Integer id) {
        RestaurantProvider provider = providerService.findOne(id);
        Collection <ProviderResponse> providerResponses = responseService.findAllByProvider(provider);
        return new ResponseEntity <Collection <ProviderResponse>>(providerResponses, HttpStatus.OK);
    }
}
