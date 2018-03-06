package com.tecnico.sec.hds.server.controllers;

import io.swagger.api.SendAmountApi;
import io.swagger.model.SendAmountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
public class SendAmountController implements SendAmountApi {


    @Override
    public ResponseEntity<Void> sendAmount(@Valid @RequestBody SendAmountRequest body) {
        System.out.println(body);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
