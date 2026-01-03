package com.hotel.hotelservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkCreateRoomRequest {
    private Long categoryId;
    private Integer start;
    private Integer end;
}
