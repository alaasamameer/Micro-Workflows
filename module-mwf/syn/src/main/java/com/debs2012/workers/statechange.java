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

import java.awt.*;
import java.util.Properties;

public class statechange {
    public static Schema.Parser _p_parser = new Schema.Parser();
    public static Schema _p_schema = _p_parser.parse("{      \n" +
            "\t\"type\": \"record\",\n" +
            "\t\"namespace\": \"com.debs2012\",\n" +
            "\t\"name\": \"Debs2012_DetectStateChange\",\n" +
            "\t\"version\": \"1\",      \n" +
            "\t\"doc\": \"Avro Schema for Debs2012 DetectStateChange KStream\",\n" +
            "\t\"fields\": [\n" +
            "         { \"name\": \"ts\", \"type\": \"long\", \"logicalType\": \"timestamp-millis\", \"doc\": \"EPOCH millis Timestamp \"},\n" +
            "         { \"name\": \"index\", \"type\": \"long\", \"doc\": \"message index\" },\n" +
            "         { \"name\": \"pp07\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp08\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp09\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp12\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp15\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp21\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp33\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "         { \"name\": \"pp36\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s07_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s07_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s08_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s08_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s09_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s09_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s12_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s12_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s15_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s15_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s21_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s21_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s33_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s33_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"s36_edge\", \"type\": [\"null\", \"int\"], \"default\": null },\n" +
            "\t { \"name\": \"s36_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"msg1_index\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"msg1_org_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"msg1_rcv_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"msg2_index\", \"type\": [\"null\", \"long\"], \"default\": null },\n" +
            "\t { \"name\": \"msg2_org_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "\t { \"name\": \"msg2_rcv_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null },\n" +
            "         { \"name\": \"k_send_ts\", \"type\": [\"null\", \"long\"], \"logicalType\": \"timestamp-millis\", \"default\": null }   \n" +
            "\t] \n" +
            "}");

    public static void main(String[] args) {
        // Print starting time
        System.out.println("Stream service start at: "+System.currentTimeMillis());

        // Args set
        String app_id=System.getenv("SOURCE_GROUP_ID").toString();
        String kafkabroker=System.getenv("SOURCE_KAFKA_SERVER").toString();
        String SchemaRegistry = System.getenv("SOURCE_SCHEMA_REG_SERVER").toString();
        String inTopic = System.getenv("SOURCE_TOPICS").toString();
        String outTopic = System.getenv("OUT_TOPIC").toString();

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
        KStream<String, GenericRecord> MainkStreaminput = builder.stream(inTopic);

        KTable<String, GenericRecord> ResultKTable =MainkStreaminput
                .mapValues(value-> getEditedGenericRecord(value))
                .selectKey((key,value)->"st_change")
                .groupByKey()
                .aggregate(
                        ()-> new GenericData.Record(_p_schema),
                        (aggkey, newValue,aggValue)-> {
                            /*
                            // this is only to trace the time arrive of each msg
                            System.out.println(" aggValue, index: "+aggValue.get("index"));
                            System.out.println("aggValue, msg1_index: "+aggValue.get("msg1_index")+" msg1_rcv_ts: "+aggValue.get("msg1_rcv_ts"));
                            System.out.println("aggValue, msg2_index: "+aggValue.get("msg2_index")+" msg2_rcv_ts: "+aggValue.get("msg2_rcv_ts"));
                            System.out.println("------------------");
                            System.out.println(" newValue, index: "+newValue.get("index"));
                            System.out.println("newValue, msg1_index: "+newValue.get("msg1_index")+" msg1_rcv_ts: "+newValue.get("msg1_rcv_ts"));
                            System.out.println("newValue, msg2_index: "+newValue.get("msg2_index")+" msg2_rcv_ts: "+newValue.get("msg2_rcv_ts"));
                            System.out.println("###########################");
                            */
                            return getFinalGenericRecord(newValue,aggValue);
                        }
                );

        KStream<String, GenericRecord>[] lastRec= ResultKTable.toStream().branch(
                (key, value) ->
                        value.get("s07_edge")!=null
                                || value.get("s08_edge")!=null
                                || value.get("s09_edge")!=null
                                || value.get("s12_edge")!=null
                                || value.get("s15_edge")!=null
                                || value.get("s21_edge")!=null
                                || value.get("s33_edge")!=null
                                || value.get("s36_edge")!=null
                ,
                (key, value) -> true

        );

        lastRec[0].mapValues(value -> {
            value.put("k_send_ts", System.currentTimeMillis());
          //  System.out.println(value);
            return value;
        })
                .to(outTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp(); // only do this in dev - not in prod
        streams.start();

        // print the topology
        //streams.localThreadsMetadata().forEach(data -> System.out.println(data));

        // shutdown hook to correctly close the streams application
        //Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
            System.out.println("Stream service stop at: "+System.currentTimeMillis());
        }));
    }

    // To edit original received record --------------------------------------
    private static GenericRecord getEditedGenericRecord(GenericRecord curr) {
        GenericRecord outRecord = new GenericData.Record(_p_schema);

        // This part to set and print the time of received any msg from source topic
        long rcvdTime=System.currentTimeMillis();
        outRecord.put("msg1_rcv_ts",rcvdTime);
        //----------------

        Long cur_ts = Long.valueOf( curr.get("ts").toString());
        long cr= cur_ts.longValue();
        outRecord.put("ts",cr);

        Long cur_index = Long.valueOf( curr.get("index").toString());
        long ix= cur_index.longValue();
        outRecord.put("index",ix);
        System.out.println(ix+","+rcvdTime);

        String [] bm = {"pp07","pp08","pp09","pp12","pp15","pp21","pp33","pp36"};
        for (int i=0;i<bm.length;i++){
            Integer cu = Integer.valueOf(curr.get(bm[i]).toString());
            int cuVAL=cu.intValue();
            outRecord.put(bm[i],cuVAL);
        }

        return outRecord;
    }

    private static GenericRecord getFinalGenericRecord(GenericRecord curr, GenericRecord prev) {
        GenericRecord outRecord = new GenericData.Record(_p_schema);
        String [] bm = {"pp07","pp08","pp09","pp12","pp15","pp21","pp33","pp36"};
        String [] s_edge = {"s07_edge","s08_edge","s09_edge","s12_edge","s15_edge","s21_edge","s33_edge","s36_edge"};
        String [] s_ts = {"s07_ts","s08_ts","s09_ts","s12_ts","s15_ts","s21_ts","s33_ts","s36_ts"};

        Long cur_ts = Long.valueOf( curr.get("ts").toString());
        long cr= cur_ts.longValue();

        Long cur_index = Long.valueOf( curr.get("index").toString());
        long ix= cur_index.longValue();

        // bm05 and s05_edge
        if (prev.get("index")!=null) {

            for (int i=0;i<bm.length;i++){

                Integer cu = Integer.valueOf(curr.get(bm[i]).toString());
                int cuVAL=cu.intValue();
                Integer pr = Integer.valueOf(prev.get(bm[i]).toString());
                int prVAL=pr.intValue();

                if (cuVAL == 0 && prVAL == 1) {
                    outRecord.put(s_edge[i], 0);
                    outRecord.put(s_ts[i], cr);
                }else if (cuVAL == 1 && prVAL == 0){
                    outRecord.put(s_edge[i], 1);
                    outRecord.put(s_ts[i], cr);
                }else {
                    outRecord.put(s_edge[i], null);
                    outRecord.put(s_ts[i], null);
                }

            }

            outRecord.put("msg1_index",prev.get("index"));
            outRecord.put("msg1_org_ts",prev.get("ts"));
            outRecord.put("msg2_index",curr.get("index"));
            outRecord.put("msg2_org_ts",curr.get("ts"));

            if (prev.get("msg2_rcv_ts")!=null){
                outRecord.put("msg1_rcv_ts",prev.get("msg2_rcv_ts"));
                outRecord.put("msg2_rcv_ts",curr.get("msg1_rcv_ts"));
            }else{
                outRecord.put("msg1_rcv_ts",prev.get("msg1_rcv_ts"));
                outRecord.put("msg2_rcv_ts",curr.get("msg1_rcv_ts"));
            }
        }else{
           outRecord.put("msg1_rcv_ts",curr.get("msg1_rcv_ts"));
        }

        outRecord.put("ts",cr);
        outRecord.put("index",ix);

        for (int i=0;i<bm.length;i++){
            Integer cu = Integer.valueOf(curr.get(bm[i]).toString());
            int cuVAL=cu.intValue();
            outRecord.put(bm[i],cuVAL);
        }


        return outRecord;
    }

    // Return the type of Object
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }

}