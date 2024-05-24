package com.flomair.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TrackingRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private long timestamp;

    private Coordinates coordinates;
    private String deliveryId;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class Coordinates implements Serializable {
        private static final long serialVersionUID = 1L;

        private float lat;
        private float lon;
    }

}
