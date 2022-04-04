package com.debs2012.consumers;

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

public class consume_statechange_local {

    public static void main(String[] args) {

        //Setting the base environments variable
        String kafkaServer=System.getenv("KAFKA_SERVER").toString();
        String SchemaRegistry=System.getenv("SCHEMA_REG_SERVER").toString();
        String gid = System.getenv("GROUP_ID").toString();
        String topic = System.getenv("SOURCE_TOPICS").toString();
        Long TotalTimeToRun = Long.parseLong(System.getenv("TOTAL_TIME").toString());
        String NumRecordsToPoll=System.getenv("RECORDS_PER_PULL").toString();
        Long PollTimeOut= Long.parseLong(System.getenv("PULL_TIMEOUT").toString());
        //-----END----Setting the base environments variable

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
        long Actual_Send_Recive=0L;
        long TotalTDif_org_ksend=0L;
        long msg2_rcv_ts=0L;
        long k_send_ts=0L;
        long msg2_org_ts=0L;
        long index=0L, totalIndex=0L;

        // compare each msg2_org_ts with the local reciving time
        String stringInputWithSpace = "k_send_ts msg2_rcv_ts msg2_org_ts index";
        String[] getVaribleFromString = stringInputWithSpace.trim().split("\\s+");

        long t= System.currentTimeMillis();
        long end = t+TotalTimeToRun;
        while (System.currentTimeMillis() < end) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(PollTimeOut);
            for (ConsumerRecord<String, GenericRecord> record : records) {
                GenericRecord customer = record.value();
                System.out.println(customer);
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
                            if (Objects.equals("index", new String(getVaribleFromString[i])))
                            {
                                index=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
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
                totalIndex=totalIndex+index;
                //System.out.println(index+","+k_send_ts);

                TotalTDif_lastmsgrcv__ksend=TotalTDif_lastmsgrcv__ksend+
                        Instant.ofEpochMilli(msg2_rcv_ts)
                                .until(Instant.ofEpochMilli(k_send_ts),
                                        ChronoUnit.MILLIS);

                TotalTDif_org_rcv=TotalTDif_org_rcv+
                        Instant.ofEpochMilli(msg2_org_ts)
                                .until(Instant.ofEpochMilli(msg2_rcv_ts),
                                        ChronoUnit.MILLIS);

                Actual_Send_Recive=Instant.ofEpochMilli(msg2_org_ts).until(
                        Instant.ofEpochMilli(System.currentTimeMillis()), ChronoUnit.MILLIS);

                System.out.println(Actual_Send_Recive);

                TotalTDif_org_ksend=TotalTDif_org_ksend+
                        Instant.ofEpochMilli(msg2_org_ts)
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

        System.out.println("Total index is: "+ totalIndex);
    }
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }

}
