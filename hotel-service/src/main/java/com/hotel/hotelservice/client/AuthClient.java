package com.hotel.hotelservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hotel.hotelservice.config.FeignConfig;
import com.hotel.hotelservice.dto.request.CreateManagerRequest;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthClient {

    @PostMapping("/auth/internal/create-manager")
    void createManager(@RequestBody CreateManagerRequest request);
}
