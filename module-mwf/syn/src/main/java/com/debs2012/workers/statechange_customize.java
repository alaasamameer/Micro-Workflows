package com.debs2012.workers;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class statechange_customize {
    public static String [] bm,s_edge,s_ts;
    public static Schema _p_schema;

    public static void main(String[] args) throws IOException {
        // Print starting time
        System.out.println("Statechange service start at: "+System.currentTimeMillis());

        //Setting the base environments variable
        String app_id=System.getenv("GROUP_ID").toString();
        String kafkabroker=System.getenv("KAFKA_SERVER").toString();
        String SchemaRegistry = System.getenv("SCHEMA_REG_SERVER").toString();
        String inTopic = System.getenv("SOURCE_TOPICS").toString();
        String outTopic = System.getenv("OUT_TOPIC").toString();
        Schema.Parser _p_parser= new Schema.Parser();;
        _p_schema = _p_parser.parse(System.getenv("OUT_SCHEMA").toString());
        bm=(System.getenv("SENSORS").toString()).split(",");
        s_edge=new String [bm.length];
        s_ts=new String [bm.length];
        for (int i=0;i<bm.length;i++){
            String str=bm[i].replace("pp","");
            s_edge[i]="s"+str+"_edge";
            s_ts[i]="s"+str+"_ts";
        }
        //-----END----Setting the base environments variable

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
                        (aggkey, newValue,aggValue)-> getFinalGenericRecord(newValue,aggValue)
                );

        KStream<String, GenericRecord>[] lastRec= ResultKTable.toStream().branch(
                (key, value) -> getPredicatesEdge(value),
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
            System.out.println("Statechange service stop at: "+System.currentTimeMillis());
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

        for (int i=0;i<bm.length;i++){
            Integer cu = Integer.valueOf(curr.get(bm[i]).toString());
            int cuVAL=cu.intValue();
            outRecord.put(bm[i],cuVAL);
        }

        return outRecord;
    }

    private static GenericRecord getFinalGenericRecord(GenericRecord curr, GenericRecord prev) {
        GenericRecord outRecord = new GenericData.Record(_p_schema);

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

    private static boolean getPredicatesEdge (GenericRecord val){
        int loopEadge=bm.length;
        for (int i=0;i<loopEadge;i++){
            if (val.get(s_edge[i])!=null)
                return true;
        }
        return false;
    }
}
