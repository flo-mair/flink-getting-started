package com.flomair.serdes;

import com.flomair.data.TrackingRecord;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TrackingRecordDeserializer implements DeserializationSchema<TrackingRecord> {

    public static final ObjectMapper objectMapper= new ObjectMapper();

    @Override
    public TrackingRecord deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, TrackingRecord.class);
    }

    @Override
    public boolean isEndOfStream(TrackingRecord trackingRecord) {
        return false;
    }

    @Override
    public TypeInformation<TrackingRecord> getProducedType() {
        return TypeInformation.of(TrackingRecord.class);
    }
}
