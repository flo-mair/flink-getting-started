package com.flomair;

import com.flomair.data.TrackingRecord;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.configuration.Configuration;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class ForecastFunction extends ProcessWindowFunction<TrackingRecord, String, String, TimeWindow>{

    private ValueState<TrackingRecord> firstRecord;
    private ValueState<TrackingRecord> lastRecord;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<TrackingRecord> firstRecordDescriptor = new ValueStateDescriptor<>("firstRecord", TrackingRecord.class);
        ValueStateDescriptor<TrackingRecord> lastRecordDescriptor = new ValueStateDescriptor<>("lastRecord", TrackingRecord.class);

        firstRecord = getRuntimeContext().getState(firstRecordDescriptor);
        lastRecord = getRuntimeContext().getState(lastRecordDescriptor);

    }

    @Override
    public void process(String s, ProcessWindowFunction<TrackingRecord, String, String, TimeWindow>.Context context, Iterable<TrackingRecord> iterable, Collector<String> collector) throws Exception {

        if (firstRecord.value() == null) {
            TrackingRecord record = new TrackingRecord();
            record.setTimestamp(Long.MAX_VALUE);
            firstRecord.update(record);
        }

        if (lastRecord.value() == null) {
            TrackingRecord record = new TrackingRecord();
            record.setTimestamp(Long.MIN_VALUE);
            lastRecord.update(record);
        }

        for (TrackingRecord record : iterable) {
            if (record.getTimestamp() < firstRecord.value().getTimestamp()) {
                firstRecord.update(record);
            }
            if (record.getTimestamp() > lastRecord.value().getTimestamp()) {
                lastRecord.update(record);
            }
        }

        double traveledDistance = calculateDistance(firstRecord.value().getCoordinates().getLat(), firstRecord.value().getCoordinates().getLon(), lastRecord.value().getCoordinates().getLat(), lastRecord.value().getCoordinates().getLon());
        long traveledTime = lastRecord.value().getTimestamp() - firstRecord.value().getTimestamp();
        double speed = traveledDistance / ((double) traveledTime / 3600 / 1000);
        double distanceRemaining = calculateDistance(lastRecord.value().getCoordinates().getLat(), lastRecord.value().getCoordinates().getLon(), 45.5167, 13.5667);
        long arrivalTimestamp = 1703440800000L;
        long timeRemaining = (arrivalTimestamp - lastRecord.value().getTimestamp()) / 3600000;
        double possibleDistance = timeRemaining * speed;

        String forecast = "";
        if (possibleDistance > distanceRemaining) {
            forecast = "on-time";
        } else {
            forecast = "delayed";
        }
        Instant lastTimestamp = Instant.ofEpochMilli(lastRecord.value().getTimestamp());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

        String output = formatter.format(lastTimestamp) +
                " Current forecast: " +
                forecast +
                ". Remaining distance: " +
                Math.floor(distanceRemaining) +
                " Remaining time (h): " +
                timeRemaining +
                " Travelling speed: " +
                Math.floor(speed);

        collector.collect(output);
    }


    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the change in coordinates
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(deltaLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c;
    }
}
