package com.debs2012.workers;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.avro.generic.GenericRecord;

import java.util.Properties;
public class correlatestatechange {
    public static Schema.Parser _p_parser = new Schema.Parser();
    public static Schema _p_schema = _p_parser.parse("{      \n" +
            "\t\"type\": \"record\",\n" +
            "\t\"namespace\": \"com.debs2012\",\n" +
            "\t\"name\": \"Debs2012_CorrelateStateChange\",\n" +
            "\t\"version\": \"1\",      \n" +
            "\t\"doc\": \"Avro Schema for Debs2012 CorrelateStateChange\",\n" +
            "\t\"fields\": [\n" +
            "         { \"name\": \"ts\", \"type\": \"long\", \"logicalType\": \"timestamp-millis\", \"doc\": \"EPOCH millis Timestamp \"},\n" +
            "         { \"name\": \"index\", \"type\": \"long\", \"doc\": \"message index\" },\n" +
            "\t { \"name\": \"s0715_dt_1\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0715_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s0715_dt_0\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0715_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s0821_dt_1\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0821_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s0821_dt_0\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0821_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s0933_dt_1\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0933_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s0933_dt_0\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s0933_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s1236_dt_1\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s1236_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s1236_dt_0\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"s1236_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "     { \"name\": \"s15_edge_1\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s15_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s21_edge_1\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s21_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s33_edge_1\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s33_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s36_edge_1\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s36_ts_1\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s15_edge_0\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s15_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s21_edge_0\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s21_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s33_edge_0\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s33_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s36_edge_0\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s36_ts_0\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"msg_rcv_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "         { \"name\": \"k_send_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null }   \n" +
            "\t] \n" +
            "}");

    public static void main(String[] args) {
        // Args set
        String app_id=args[0].toString();
        String kafkabroker=args[1].toString();
        String SchemaRegistry = args[2].toString();
        String inTopic = args[3].toString();
        String outTopic = args[4].toString();
        //
        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, app_id);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkabroker);
        config.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SchemaRegistry);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        // we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        //config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
        // Exactly once processing!!
        config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        StreamsBuilder builder = new StreamsBuilder();

        // Step 1: We create the topic of users keys to colours
        KStream<String, GenericRecord> MainkStreaminput = builder.stream(inTopic );

        KTable<String, GenericRecord> ResultKTable =MainkStreaminput
                .mapValues(value-> {

                    value.put("msg1_rcv_ts",System.currentTimeMillis());

                    value.put("ts",value.get("k_send_ts"));

                    return value;
                })
                .selectKey((key,value)->"corr_st_ch")
                .groupByKey()
                .aggregate(
                        ()-> new GenericData.Record(_p_schema),
                        (aggkey, newValue,aggValue)-> getFinalGenericRecord(newValue,aggValue)
                );

        KStream<String, GenericRecord>[] lastRec= ResultKTable.toStream().branch(
                (key, value) ->
                        value.get("s0715_dt_1")!=null
                                || value.get("s0821_dt_1")!=null
                                || value.get("s0933_dt_1")!=null
                                || value.get("s1236_dt_1")!=null
                                || value.get("s0715_dt_0")!=null
                                || value.get("s0821_dt_0")!=null
                                || value.get("s0933_dt_0")!=null
                                || value.get("s1236_dt_0")!=null
                ,
                (key, value) -> true

        );

        lastRec[0].mapValues(value -> {
            value.put("k_send_ts", System.currentTimeMillis());
            System.out.println(value);
            return value;
        })
                .to(outTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp(); // only do this in dev - not in prod
        streams.start();

        // print the topology
        streams.localThreadsMetadata().forEach(data -> System.out.println(data));

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }


    private static GenericRecord getFinalGenericRecord(GenericRecord curr, GenericRecord prev) {
        GenericRecord outRecord = new GenericData.Record(_p_schema);

        String [] s_edge_x = {"s07_edge","s08_edge","s09_edge","s12_edge"};
        String [] s_edge_ts_x = {"s07_ts","s08_ts","s09_ts","s12_ts"};

        String [] s_edge_y = {"s15_edge","s21_edge","s33_edge","s36_edge"};
        String [] s_edge_ts_y= {"s15_ts","s21_ts","s33_ts","s36_ts"};

        String [] s_edge_y_1 = {"s15_edge_1","s21_edge_1","s33_edge_1","s36_edge_1"};
        String [] s_edge_ts_y_1 = {"s15_ts_1","s21_ts_1","s33_ts_1","s36_ts_1"};

        String [] s_edge_y_0 = {"s15_edge_0","s21_edge_0","s33_edge_0","s36_edge_0"};
        String [] s_edge_ts_y_0 = {"s15_ts_0","s21_ts_0","s33_ts_0","s36_ts_0"};

        String [] s_dt_1 ={"s0715_dt_1","s0821_dt_1","s0933_dt_1","s1236_dt_1"};
        String [] s_dt_ts_1 ={"s0715_ts_1","s0821_ts_1","s0933_ts_1","s1236_ts_1"};

        String [] s_dt_0 ={"s0715_dt_0","s0821_dt_0","s0933_dt_0","s1236_dt_0"};
        String [] s_dt_ts_0 ={"s0715_ts_0","s0821_ts_0","s0933_ts_0","s1236_ts_0"};


        // dt
        if (prev.get("index")!=null) {
            for (int i=0;i<s_dt_1.length;i++) {
                if (curr.get(s_edge_x[i]) != null) { // there is current x

                    if (prev.get(s_edge_y_1[i]) != null) // there is prev y
                    {
                        Integer cu_x = Integer.valueOf(curr.get(s_edge_x[i]).toString());
                        int cu_x_VAL = cu_x.intValue();

                        Integer prev_y = Integer.valueOf(prev.get(s_edge_y_1[i]).toString());
                        int prev_y_VAL = prev_y.intValue();

                        Long cur_ts = Long.valueOf(curr.get(s_edge_ts_x[i]).toString());
                        long crts = cur_ts.longValue();
                        Long prev_ts = Long.valueOf(prev.get(s_edge_ts_y_1[i]).toString());
                        long prevts = prev_ts.longValue();

                        if (cu_x_VAL == 1 && prev_y_VAL == 1) { //state 1
                            // do dt 1
                            outRecord.put(s_dt_1[i], crts - prevts);
                            outRecord.put(s_dt_ts_1[i], prevts);
                        } else if (cu_x_VAL == 0 && prev_y_VAL == 0) { //sate 0
                            // do dt 0
                            outRecord.put(s_dt_0[i], crts - prevts);
                            outRecord.put(s_dt_ts_0[i], prevts);
                        }
                    }

                    if (prev.get(s_edge_y_0[i]) != null) // there is prev y
                    {
                        Integer cu_x = Integer.valueOf(curr.get(s_edge_x[i]).toString());
                        int cu_x_VAL = cu_x.intValue();

                        Integer prev_y = Integer.valueOf(prev.get(s_edge_y_0[i]).toString());
                        int prev_y_VAL = prev_y.intValue();

                        Long cur_ts = Long.valueOf(curr.get(s_edge_ts_x[i]).toString());
                        long crts = cur_ts.longValue();
                        Long prev_ts = Long.valueOf(prev.get(s_edge_ts_y_0[i]).toString());
                        long prevts = prev_ts.longValue();

                        if (cu_x_VAL == 0 && prev_y_VAL == 0) { //state 1
                            // do dt 1
                            outRecord.put(s_dt_0[i], crts - prevts);
                            outRecord.put(s_dt_ts_0[i], prevts);
                        }
                    }
                }// loop
            }

            for (int i=0;i<s_edge_y_0.length;i++){
                if (curr.get(s_edge_y[i])!=null){ // there is new y
                    Integer cu_y = Integer.valueOf(curr.get(s_edge_y[i]).toString());
                    int cu_y_VAL = cu_y.intValue();
                    if (cu_y_VAL==1){
                        outRecord.put(s_edge_y_1[i],curr.get(s_edge_y[i]));
                        outRecord.put(s_edge_ts_y_1[i],curr.get(s_edge_ts_y[i]));
                        outRecord.put(s_edge_y_0[i],prev.get(s_edge_y_0[i]));
                        outRecord.put(s_edge_ts_y_0[i],prev.get(s_edge_ts_y_0[i]));
                    }else if (cu_y_VAL==0){
                        outRecord.put(s_edge_y_0[i],curr.get(s_edge_y[i]));
                        outRecord.put(s_edge_ts_y_0[i],curr.get(s_edge_ts_y[i]));
                        outRecord.put(s_edge_y_1[i],prev.get(s_edge_y_1[i]));
                        outRecord.put(s_edge_ts_y_1[i],prev.get(s_edge_ts_y_1[i]));
                    }
                }else{ // no new y
                    outRecord.put(s_edge_y_1[i],prev.get(s_edge_y_1[i]));
                    outRecord.put(s_edge_ts_y_1[i],prev.get(s_edge_ts_y_1[i]));
                    outRecord.put(s_edge_y_0[i],prev.get(s_edge_y_0[i]));
                    outRecord.put(s_edge_ts_y_0[i],prev.get(s_edge_ts_y_0[i]));
                }
            }
        }else{
            for (int i=0;i<s_edge_y.length;i++){
                outRecord.put(s_edge_y_1[i],curr.get(s_edge_y[i]));
                outRecord.put(s_edge_ts_y_1[i],curr.get(s_edge_ts_y[i]));
                outRecord.put(s_edge_y_0[i],curr.get(s_edge_y[i]));
                outRecord.put(s_edge_ts_y_0[i],curr.get(s_edge_ts_y[i]));
            }
        }

        // Fill the common fields
        outRecord.put("ts",curr.get("ts"));
        outRecord.put("index",curr.get("index"));
        outRecord.put("msg_rcv_ts",curr.get("msg1_rcv_ts"));

        //return the final record
        return outRecord;
    }
}

