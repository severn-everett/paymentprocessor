package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.exceptions.InvalidUploadException;
import com.severett.paymentprocessor.exceptions.UploadExpiredException;
import com.severett.paymentprocessor.model.Upload;
import org.json.JSONException;

public interface UploadParseService {

    public Upload parseUpload(String input) throws JSONException, UploadExpiredException, InvalidUploadException;
    
}
