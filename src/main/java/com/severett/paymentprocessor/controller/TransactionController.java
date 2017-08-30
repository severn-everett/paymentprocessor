package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.model.ITransactionStore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
    
    @Autowired
    ITransactionStore transactionStore;
    
    @RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody        
    public String statistics(HttpServletRequest request, HttpServletResponse response) {
        JSONObject responseObj = null;
        try {
            responseObj = new JSONObject(transactionStore.getTransactionStats());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            responseObj = new JSONObject();
            responseObj.put("Error", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return responseObj.toString();
    }
    
}
