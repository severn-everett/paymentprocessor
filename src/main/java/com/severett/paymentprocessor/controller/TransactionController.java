package com.severett.paymentprocessor.controller;

import com.severett.paymentprocessor.model.ITransactionStore;
import com.severett.paymentprocessor.model.Transaction;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {
    
    @Autowired
    ITransactionStore transactionStore;
    
    @RequestMapping(value = "/transactions", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String transactions(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
        JSONObject responseObj = new JSONObject();
        try {
            JSONObject requestObj = new JSONObject(requestBody);
            if ((!requestObj.isNull("amount")) && (!requestObj.isNull("timestamp"))) {
                double amt = requestObj.getDouble("amount");
                Instant timestamp = Instant.ofEpochMilli(requestObj.getLong("timestamp"));
                if (timestamp.compareTo(Instant.now().minusSeconds(60L)) >= 0) {
                    transactionStore.addTransaction(new Transaction(timestamp, amt));
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            } else {
                throw new JSONException("'amt' and 'timestamp' must be defined.");
            }
        } catch (JSONException jsone) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseObj.put("error", "request");
            responseObj.put("message", jsone.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseObj.put("error", "internal");
            responseObj.put("message", e.getMessage());
        }
        return responseObj.toString();
    }
    
    @RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody        
    public String statistics(HttpServletRequest request, HttpServletResponse response) {
        JSONObject responseObj;
        try {
            responseObj = new JSONObject(transactionStore.getTransactionStats());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            responseObj = new JSONObject();
            responseObj.put("error", "internal");
            responseObj.put("message", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return responseObj.toString();
    }
    
}