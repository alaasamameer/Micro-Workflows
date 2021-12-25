package com.debs2012;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class debs2012_state_change_cn {
    public static void main(String[] args) {

        String kafkaServer=args[0].toString();
        String SchemaRegistry=args[1].toString();
        String gid = args[2].toString();
        String topic = args[3].toString();
        Long TotalTimeToRun = Long.parseLong(args[4].toString());
        String NumRecordsToPoll=args[5].toString();
        Long PollTimeOut= Long.parseLong(args[6].toString());

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", kafkaServer);
        properties.setProperty("group.id", gid);
        properties.setProperty("enable.auto.commit", "false");
        properties.setProperty("auto.offset.reset", "earliest");
        properties.setProperty("max.poll.records",NumRecordsToPoll);
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", KafkaAvroDeserializer.class.getName());
        properties.setProperty("schema.registry.url", SchemaRegistry);
        KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(properties);
        consumer.subscribe(Collections.singleton(topic));
        System.out.println(" Waiting for data ....... ");

        long msgCount=0L;

        // to calculate the deferant betwen each msg2_rcv_ts and Ksend time
        long TotalTDif_lastmsgrcv__ksend=0L;
        long TotalTDif_org_rcv=0L;
        long TotalTDif_org_ksend=0L;
        long TotalTDif_rcv_k_st_chng_ts=0L;
        long TotalTDif_k_st_chng_ts_ksend=0L;
        long msg2_rcv_ts=0L;
        long k_send_ts=0L;
        long msg2_org_ts=0L;
        long k_st_chng_ts=0L;


        String stringInputWithSpace = "k_send_ts msg2_rcv_ts msg2_org_ts k_st_chng_ts";
        String[] getVaribleFromString = stringInputWithSpace.trim().split("\\s+");

        long t= System.currentTimeMillis();
        long end = t+TotalTimeToRun;
        while (System.currentTimeMillis() < end) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(PollTimeOut);
            for (ConsumerRecord<String, GenericRecord> record : records) {
                GenericRecord customer = record.value();
                HashMap<String, Object> myMap = new HashMap<String, Object>();
                for (int i = 0; i < getVaribleFromString.length; i++) {
                    String check = nameOf(customer.get(getVaribleFromString[i]));
                    switch (check) {
                        case "Utf8":
                            myMap.put(getVaribleFromString[i], customer.get(getVaribleFromString[i]).toString());
                            break;
                        case "Integer":
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));

                        }
                        break;

                        case "Long":
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));

                            // To calculate the average time consumed between
                            // msg2 receive time : msg2_rcv_ts
                            // till the result are sent : k_send_ts
                            if (Objects.equals("msg2_rcv_ts", new String(getVaribleFromString[i])))
                            {
                                msg2_rcv_ts=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                            }
                            if (Objects.equals("k_send_ts", new String(getVaribleFromString[i])))
                            {
                                k_send_ts=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                            }
                            // till the result are sent : k_send_ts
                            if (Objects.equals("msg2_org_ts", new String(getVaribleFromString[i])))
                            {
                                msg2_org_ts=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                            }
                            if (Objects.equals("k_st_chng_ts", new String(getVaribleFromString[i])))
                            {
                                k_st_chng_ts=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                            }

                        }
                        break;

                        case "Double":
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));
                        }
                        break;

                        case "Float":
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));
                        }
                        break;

                        case "Boolean":
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));
                        }
                        break;

                        default:
                        {
                            myMap.put(getVaribleFromString[i],customer.get(getVaribleFromString[i]));
                        }
                        break;
                    }

                }

                TotalTDif_lastmsgrcv__ksend=TotalTDif_lastmsgrcv__ksend+
                        Instant.ofEpochMilli(msg2_rcv_ts)
                                .until(Instant.ofEpochMilli(k_send_ts),
                                        ChronoUnit.MILLIS);

                TotalTDif_org_rcv=TotalTDif_org_rcv+
                        Instant.ofEpochMilli(msg2_org_ts)
                                .until(Instant.ofEpochMilli(msg2_rcv_ts),
                                        ChronoUnit.MILLIS);

                TotalTDif_org_ksend=TotalTDif_org_ksend+
                        Instant.ofEpochMilli(msg2_org_ts)
                                .until(Instant.ofEpochMilli(k_send_ts),
                                        ChronoUnit.MILLIS);

                TotalTDif_rcv_k_st_chng_ts=TotalTDif_rcv_k_st_chng_ts+
                        Instant.ofEpochMilli(msg2_rcv_ts)
                                .until(Instant.ofEpochMilli(k_st_chng_ts),
                                        ChronoUnit.MILLIS);

                TotalTDif_k_st_chng_ts_ksend=TotalTDif_k_st_chng_ts_ksend+
                        Instant.ofEpochMilli(k_st_chng_ts)
                                .until(Instant.ofEpochMilli(k_send_ts),
                                        ChronoUnit.MILLIS);
                msgCount=msgCount+1;
            }
            consumer.commitSync();
            try {
                sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println("Total messages : "+ msgCount);

        double Av_T_msg2_Ksend=((double) TotalTDif_lastmsgrcv__ksend)/((double) msgCount);
        System.out.println("AV_T between msg2_rcv_ts and k_send_ts (MILLIS): "+ Av_T_msg2_Ksend);

        double Av_TotalTDif_org__rcv=((double) TotalTDif_org_rcv)/((double) msgCount);
        System.out.println("AV_T between msg2_org_ts and msg2_rcv_ts (MILLIS): "+ Av_TotalTDif_org__rcv);

        double Av_TotalTDif_org__Ksend=((double) TotalTDif_org_ksend)/((double) msgCount);
        System.out.println("AV_T between msg2_org_ts and k_send_ts (MILLIS): "+ Av_TotalTDif_org__Ksend);

        double Av_TotalTDif_rcv_k_st_chng_ts=((double) TotalTDif_rcv_k_st_chng_ts)/((double) msgCount);
        System.out.println("AV_T between msg2_rcv and k_st_chng_ts (MILLIS): "+ Av_TotalTDif_rcv_k_st_chng_ts);

        double Av_TotalTDif_k_st_chng_ts_Ksend=((double) TotalTDif_k_st_chng_ts_ksend)/((double) msgCount);
        System.out.println("AV_T between k_st_chng_ts and k_send_ts (MILLIS): "+ Av_TotalTDif_k_st_chng_ts_Ksend);
    }
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }
}