package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.exceptions.InvalidUploadException;
import com.severett.paymentprocessor.exceptions.UploadExpiredException;
import com.severett.paymentprocessor.model.Upload;
import java.time.Instant;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class UploadParseServiceImpl implements UploadParseService {

    @Override
    public Upload parseUpload(String input) throws JSONException, UploadExpiredException, InvalidUploadException {
        JSONObject requestObj = new JSONObject(input);
        if ((!requestObj.isNull("count")) && (!requestObj.isNull("timestamp"))) {
            long count = requestObj.getLong("count");
            Instant timestamp = Instant.ofEpochMilli(requestObj.getLong("timestamp"));
            if ((timestamp.compareTo(Instant.now().minusSeconds(60L)) >= 0) && (count > 0)) {
                return new Upload(timestamp, count);
            } else if (count <= 0) {
                throw new InvalidUploadException("'count' must be greater than zero.");
            } else {
                throw new UploadExpiredException(timestamp);
            }
        } else {
            throw new InvalidUploadException("'count' and 'timestamp' must be defined.");
        }
    }
    
}
