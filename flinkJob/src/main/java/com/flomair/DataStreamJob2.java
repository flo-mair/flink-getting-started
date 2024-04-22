/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flomair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flomair.data.TrackingRecord;
import com.flomair.serdes.TrackingRecordDeserializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.opensearch.sink.OpensearchSink;
import org.apache.flink.connector.opensearch.sink.OpensearchSinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.opensearch.client.Requests;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataStreamJob2 {
    private static final Logger LOG = LoggerFactory.getLogger(DataStreamJob2.class);

    static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // Sets up the execution environment, which is the main entry point
        // to building Flink applications.
        Configuration configuration = new Configuration();
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        env.disableOperatorChaining();
        env.setParallelism(1);

        String brokers = "localhost:29092";
        String openSearchEndpoint = "https://localhost:9200";

        KafkaSource<TrackingRecord> source = KafkaSource.<TrackingRecord>builder()
                .setBootstrapServers(brokers)
                .setTopics("shipping_updates")
                .setGroupId("flinkApplication")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new TrackingRecordDeserializer())
                .build();

        WatermarkStrategy<TrackingRecord> watermarkStrategy = WatermarkStrategy
                .<TrackingRecord>forMonotonousTimestamps()
                .withTimestampAssigner((trackingRecord, timestamp) -> trackingRecord.getTimestamp());


        DataStream<TrackingRecord> inputStream = env
                .fromSource(source, watermarkStrategy, "Kafka Source")
                .uid("kafka-source-shipping-updates");


        OpensearchSink<String> sink = new OpensearchSinkBuilder<String>()
                .setHosts(HttpHost.create(openSearchEndpoint))
                .setConnectionUsername("admin")
                .setConnectionPassword("P0rt0r0z2024!")
                .setBulkFlushInterval(1000)
                .setAllowInsecure(true)
                .setEmitter(
                        (element, context, indexer) ->
                                indexer.add(Requests.indexRequest()
                                        .index("tracking5")
                                        .source(element, XContentType.JSON)))
                .build();


        inputStream
/*                .map(trackingRecord -> {
                    ArrayList<Float> point = new ArrayList<>(2);
                    point.add(Float.valueOf(trackingRecord.getCoordinates().getLat()));
                    point.add(Float.valueOf(trackingRecord.getCoordinates().getLon()));
                    long timestampMillis = Long.parseLong(trackingRecord.getTimestamp())*1000;
                    return new GeoRecord(timestampMillis, point);
                })*/
                .map((trackingRecord -> objectMapper.writeValueAsString(trackingRecord)))
                     //   .print();
                .sinkTo(sink);

        env.execute("Flink Java API Skeleton");
    }


}


