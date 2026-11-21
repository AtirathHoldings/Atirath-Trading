package com.atirath.trading.common;

import com.atirath.trading.modules.auth.dto.device.DeviceLimitExceededDto;
import lombok.Getter;

@Getter
public class DeviceLimitExceededException extends RuntimeException {

    private final DeviceLimitExceededDto limitData;

    public DeviceLimitExceededException(DeviceLimitExceededDto limitData) {
        super(limitData != null ? limitData.getMessage() : "Maximum active devices limit reached");
        this.limitData = limitData;
    }
}
