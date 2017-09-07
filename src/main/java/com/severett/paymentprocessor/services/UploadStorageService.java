package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.model.Upload;
import java.util.Map;
import java.util.Optional;

public interface UploadStorageService {
    
    public void addUpload(Upload upload);
    
    public Optional<Map<String, Object>> getUploadStats();
    
}
