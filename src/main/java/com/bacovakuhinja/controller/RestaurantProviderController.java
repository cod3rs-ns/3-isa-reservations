package com.bacovakuhinja.controller;

import com.bacovakuhinja.annotations.SendEmail;
import com.bacovakuhinja.model.RestaurantProvider;
import com.bacovakuhinja.service.RestaurantProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
public class RestaurantProviderController {

    @Autowired
    private RestaurantProviderService providerService;

    @RequestMapping(
            value = "/api/providers",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <Collection <RestaurantProvider>> getProviders() {
        Collection <RestaurantProvider> providers = providerService.findAll();
        return new ResponseEntity <Collection <RestaurantProvider>>(providers, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/providers/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <RestaurantProvider> getProvider(@PathVariable("id") Integer id) {
        RestaurantProvider provider = providerService.findOne(id);
        return new ResponseEntity <RestaurantProvider>(provider, HttpStatus.OK);
    }

    @SendEmail
    @RequestMapping(value = "/api/providers",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <RestaurantProvider> createProvider(@RequestBody RestaurantProvider provider) {
        // TODO generate password
        provider.setPassword("generated_password");
        provider.setVerified("not_verified");
        RestaurantProvider created = providerService.create(provider);
        return new ResponseEntity <RestaurantProvider>(created, HttpStatus.CREATED);
    }

    @RequestMapping(
            value = "/api/providers",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <RestaurantProvider> updateRestaurantManager(@RequestBody RestaurantProvider provider) {
        RestaurantProvider updatedProvider = providerService.update(provider);
        if (updatedProvider == null) {
            return new ResponseEntity <RestaurantProvider>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity <RestaurantProvider>(updatedProvider, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/api/providers/{id}",
            method = RequestMethod.DELETE)
    public ResponseEntity <RestaurantProvider> deleteRestaurantManager(@PathVariable("id") Integer id) {
        providerService.delete(id);
        return new ResponseEntity <RestaurantProvider>(HttpStatus.NO_CONTENT);
    }
}
