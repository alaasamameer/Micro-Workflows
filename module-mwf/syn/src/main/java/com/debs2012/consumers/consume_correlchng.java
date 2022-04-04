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

public class consume_correlchng {

    public static void main(String[] args) {

        String kafkaServer=args[0].toString(); // Kafka broker Ip and port i.e localhost:9092
        String SchemaRegistry=args[1].toString(); // Schema registry Ip and port i.e localhost:8081
        String gid = args[2].toString();  // Group id for the application
        String topic = args[3].toString(); // Topic to be consume
        Long TotalTimeToRun = Long.parseLong(args[4].toString());  // total running time
        String NumRecordsToPoll=args[5].toString(); // The number of record which to be consumed in single each pull
        Long PollTimeOut= Long.parseLong(args[6].toString()); // the time out for pull

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
        
        long v_First_k_send_ts=0L;
        long v_Last_K_send_ts=0L;
        long v_First_index=0L;
        long v_Last_index=0L;
        

        String stringInputWithSpace = "k_send_ts index";
        String[] getVaribleFromString = stringInputWithSpace.trim().split("\\s+");

        long t= System.currentTimeMillis();
        long end = t+TotalTimeToRun;
        
        // While start
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
                            
                            if (Objects.equals("k_send_ts", new String(getVaribleFromString[i])))
                            {
                                v_Last_K_send_ts=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                                if (msgCount==0) v_First_k_send_ts=v_Last_K_send_ts;
                            }
                            if (Objects.equals("index", new String(getVaribleFromString[i])))
                            {
                                v_Last_index=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                                if (msgCount==0) v_First_index=v_Last_index;
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
                
                msgCount=msgCount+1;
            }
            consumer.commitSync();
            try {
                sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        // While end

        System.out.println("======== Metrics ======== ");
        System.out.println("Total messages : "+ msgCount);
        System.out.println("------------------------- ");
        System.out.println("The first index: "+ v_First_index);
        System.out.println("The first k_send_t: "+ v_First_k_send_ts);
        System.out.println("------------------------- ");
        System.out.println("The last index: "+ v_Last_index);
        System.out.println("The last k_send_t: "+ v_Last_K_send_ts);
        System.out.println("------------------------- ");
        long v_dif_betwen_first_last_k_send_t=Instant.ofEpochMilli(v_First_k_send_ts)
                .until(Instant.ofEpochMilli(v_Last_K_send_ts),
                        ChronoUnit.MILLIS);
        double Av_ksend=((double) v_dif_betwen_first_last_k_send_t)/((double) msgCount);
        System.out.println("AV_T between all ksend in (MILLIS): "+ Av_ksend);
        System.out.println("========================== ");
    }
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }
}
