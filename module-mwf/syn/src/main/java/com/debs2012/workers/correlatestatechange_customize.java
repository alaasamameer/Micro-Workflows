package com.debs2012.workers;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;

public class correlatestatechange_customize {
    public static Schema.Parser _p_parser = new Schema.Parser();
    public static String [] s_edge_x,s_edge_ts_x,s_edge_y,s_edge_ts_y,s_edge_y_1, s_edge_ts_y_1, s_edge_y_0, s_edge_ts_y_0, s_dt_1,s_dt_ts_1,s_dt_0,s_dt_ts_0;
    public static Schema _p_schema;

    public static void main(String[] args) {

        //Setting the base environments variable
        String app_id=System.getenv("GROUP_ID").toString();
        String kafkabroker=System.getenv("KAFKA_SERVER").toString();
        String SchemaRegistry = System.getenv("SCHEMA_REG_SERVER").toString();
        String inTopic = System.getenv("SOURCE_TOPICS").toString();
        String outTopic = System.getenv("OUT_TOPIC").toString();
        _p_schema = _p_parser.parse(System.getenv("OUT_SCHEMA").toString());
        s_edge_x=(System.getenv("SENSORS_EDGES_X").toString()).split(",");
        s_edge_ts_x=new String[s_edge_x.length];
        s_edge_y=(System.getenv("SENSORS_EDGES_Y").toString()).split(",");

        s_edge_ts_y=new String[s_edge_y.length];
        s_edge_y_1=new String[s_edge_x.length];
        s_edge_ts_y_1=new String[s_edge_x.length];
        s_edge_y_0=new String[s_edge_x.length];
        s_edge_ts_y_0=new String[s_edge_x.length];
        s_dt_1=new String[s_edge_x.length];
        s_dt_ts_1=new String[s_edge_x.length];
        s_dt_0=new String[s_edge_x.length];
        s_dt_ts_0=new String[s_edge_x.length];

        for (int i=0;i<s_edge_x.length;i++){

            String strX=s_edge_x[i].replace("_edge","");
            String strY=s_edge_y[i].replace("_edge","");

            s_edge_ts_x[i]=strX+"_ts";
            s_edge_ts_y[i]=strY+"_ts";

            s_edge_y_1[i]=strY+"_edge_1";
            s_edge_ts_y_1[i]=strY+"_ts_1";

            s_edge_y_0[i]=strY+"_edge_0";
            s_edge_ts_y_0[i]=strY+"_ts_0";

            String strY_without_s=strY.replace("s","");

            s_dt_1[i]=strX+strY_without_s+"_dt_1";
            s_dt_ts_1[i]=strX+strY_without_s+"_ts_1";

            s_dt_0[i]=strX+strY_without_s+"_dt_0";
            s_dt_ts_0[i]=strX+strY_without_s+"_ts_0";

        }
        //-----END----Setting the base environments variable

        // Setting the stream properties
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
        //-----END----Setting the stream properties

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
                (key, value) -> getPredicatesDT_1_0(value),
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
    private static boolean getPredicatesDT_1_0 (GenericRecord val){
        int loopDT=s_edge_x.length;
        for (int i=0;i<loopDT;i++){
            if (val.get(s_dt_1[i])!=null || val.get(s_dt_0[i])!=null )
                return true;
        }
        return false;
    }
}
