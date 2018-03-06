package com.tecnico.sec.hds.server.controllers;

import io.swagger.annotations.ApiParam;
import io.swagger.api.CheckAmountApi;
import io.swagger.model.CheckAmountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * Created by jp_s on 3/6/2018.
 */
public class CheckAmountController implements CheckAmountApi {
    @Override
    public ResponseEntity<Integer> checkAmount(@ApiParam(value = "", required = true) @RequestBody @Valid CheckAmountRequest amount) {
        System.out.print(amount);
        return new ResponseEntity<Integer>(HttpStatus.OK);
    }
}
