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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class statechng_correlchng {

    private static Schema.Parser _p_parser_statechnge = new Schema.Parser();
    private static Schema _p_schema_statechange;
    private static Schema.Parser _p_parser_corrlchnge = new Schema.Parser();
    private static Schema _p_schema_corrlchnge ;

    private static Properties config = new Properties();

    private static String app_id;
    private static String kafkabroker;
    private static String SchemaRegistry;
    private static String inTopic ;
    private static String outTopic;
    private static StreamsBuilder builder;

    private static long TotalTDif_lastmsgrcv__ksend=0L;
    private static long TotalTDif_org_rcv=0L;
    private static long TotalTDif_org_ksend=0L;
    private static long msgCount=0L;

    public static void main(String[] args) {

        // 1- set schemas
        setSchemas();

        // 2- Set the user command line parameters
        app_id=args[0].toString();
        kafkabroker=args[1].toString();
        SchemaRegistry = args[2].toString();
        inTopic = args[3].toString();
        outTopic = args[4].toString();

        // 3- set stream Properties
        getStreamProperties();

        // 4- set stream builder
        builder = new StreamsBuilder();

        // do the work
        KStream<String, GenericRecord> MainkStreaminput = builder.stream(inTopic);
        CorrelChange(StateChange (MainkStreaminput)).to(outTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp(); // only do this in dev - not in prod
        streams.start();

        // print the topology
        //streams.localThreadsMetadata().forEach(data -> System.out.println(data));

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }

    // -----------------methods to set stream Properties----------------------
    private static void getStreamProperties (){
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
    }

    // --------------------state change methods-------------------------------
    private static KStream<String, GenericRecord> StateChange (KStream<String, GenericRecord> MainkStreaminput){

        KTable<String, GenericRecord> ResultKTable =MainkStreaminput
                .mapValues(value-> getEditedGenericRecord(value))
                .selectKey((key,value)->"st_change")
                .groupByKey()
                .aggregate(
                        ()-> new GenericData.Record(_p_schema_statechange),
                        (aggkey, newValue,aggValue)-> getFinalGenericRecord(newValue,aggValue)
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

        return lastRec[0].mapValues(value -> {
            value.put("k_send_ts", System.currentTimeMillis());
            //
            if (value.get("msg2_rcv_ts")!=null){
                Long msg2RCVts = Long.valueOf( value.get("msg2_rcv_ts").toString());
                long msg2rcvts= msg2RCVts.longValue();

                Long msg2ORGts = Long.valueOf( value.get("msg2_org_ts").toString());
                long msg2orgts= msg2ORGts.longValue();

                Long kSENDts = Long.valueOf( value.get("k_send_ts").toString());
                long ksendts= kSENDts.longValue();

                TotalTDif_lastmsgrcv__ksend=TotalTDif_lastmsgrcv__ksend+
                        Instant.ofEpochMilli(msg2rcvts)
                                .until(Instant.ofEpochMilli(ksendts),
                                        ChronoUnit.MILLIS);

                TotalTDif_org_rcv=TotalTDif_org_rcv+
                        Instant.ofEpochMilli(msg2orgts)
                                .until(Instant.ofEpochMilli(msg2rcvts),
                                        ChronoUnit.MILLIS);

                TotalTDif_org_ksend=TotalTDif_org_ksend+
                        Instant.ofEpochMilli(msg2orgts)
                                .until(Instant.ofEpochMilli(ksendts),
                                        ChronoUnit.MILLIS);
                msgCount=msgCount+1;

                System.out.println("----------------------------");

                System.out.println("StateChange: "+value);

                System.out.println("Total messages : "+ msgCount);

                double Av_T_msg2_Ksend=((double) TotalTDif_lastmsgrcv__ksend)/((double) msgCount);
                System.out.println("TotalTDif_lastmsgrcv__ksend: "+ TotalTDif_lastmsgrcv__ksend);
                System.out.println("AV_T between msg2_rcv_ts and k_send_ts (MILLIS): "+ Av_T_msg2_Ksend);

                double Av_TotalTDif_org__rcv=((double) TotalTDif_org_rcv)/((double) msgCount);
                System.out.println("TotalTDif_org_rcv: "+ TotalTDif_org_rcv);
                System.out.println("AV_T between msg2_org_ts and msg2_rcv_ts (MILLIS): "+ Av_TotalTDif_org__rcv);

                double Av_TotalTDif_org__Ksend=((double) TotalTDif_org_ksend)/((double) msgCount);
                System.out.println("TotalTDif_org_ksend: "+ TotalTDif_org_ksend);
                System.out.println("AV_T between msg2_org_ts and k_send_ts (MILLIS): "+ Av_TotalTDif_org__Ksend);

                System.out.println("----------------------------");

            }

            return value;
        });

    }
    // To edit original received record --------------------------------------
    private static GenericRecord getEditedGenericRecord(GenericRecord curr) {
        GenericRecord outRecord = new GenericData.Record(_p_schema_statechange);

        outRecord.put("msg1_rcv_ts",System.currentTimeMillis());

        Long cur_ts = Long.valueOf( curr.get("ts").toString());
        long cr= cur_ts.longValue();
        outRecord.put("ts",cr);

        Long cur_index = Long.valueOf( curr.get("index").toString());
        long ix= cur_index.longValue();
        outRecord.put("index",ix);

        String [] bm = {"pp07","pp08","pp09","pp12","pp15","pp21","pp33","pp36"};
        for (int i=0;i<bm.length;i++){
            Integer cu = Integer.valueOf(curr.get(bm[i]).toString());
            int cuVAL=cu.intValue();
            outRecord.put(bm[i],cuVAL);
        }

        return outRecord;
    }
    private static GenericRecord getFinalGenericRecord(GenericRecord curr, GenericRecord prev) {
        GenericRecord outRecord = new GenericData.Record(_p_schema_statechange);
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

    // --------------------correlate change methods-------------------------------
    private static KStream<String, GenericRecord> CorrelChange (KStream<String, GenericRecord> recFromStateChange){

        KTable<String, GenericRecord> ResultKTable =recFromStateChange
                .mapValues(value-> {

                    value.put("msg1_rcv_ts",System.currentTimeMillis());

                    value.put("ts",value.get("k_send_ts"));

                    return value;
                })
                .selectKey((key,value)->"corr_st_ch")
                .groupByKey()
                .aggregate(
                        ()-> new GenericData.Record(_p_schema_corrlchnge),
                        (aggkey, newValue,aggValue)-> getFinalCorrelGenericRecord(newValue,aggValue)
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
        return lastRec[0].mapValues(value -> {
            value.put("k_send_ts", System.currentTimeMillis());
            /*System.out.println("Correlate Change : "+value);*/
            return value;
        });
    }

    private static GenericRecord getFinalCorrelGenericRecord(GenericRecord curr, GenericRecord prev) {
        GenericRecord outRecord = new GenericData.Record(_p_schema_corrlchnge);

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


    // ------------------------Set Schema and parser --------------------------------
    private static void setSchemas (){
        _p_schema_statechange = _p_parser_statechnge.parse("{      \n" +
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


        _p_schema_corrlchnge = _p_parser_corrlchnge .parse("{      \n" +
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
    }

}
