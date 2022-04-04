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
import java.util.Objects;
import java.util.Properties;
import static java.lang.Thread.sleep;

public class consume_main_topic {
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
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
        long bm05_1=0L,bm06_1=0L, bm07_1=0L,bm08_1=0L, bm09_1=0L, bm10_1=0L;
        long bm05_0=0L,bm06_0=0L, bm07_0=0L,bm08_0=0L, bm09_0=0L, bm10_0=0L;
        long pp07_1=0L,pp08_1=0L, pp09_1=0L,pp12_1=0L, pp15_1=0L, pp21_1=0L, pp33_1=0L, pp36_1=0L;
        long pp07_0=0L,pp08_0=0L, pp09_0=0L,pp12_0=0L, pp15_0=0L, pp21_0=0L, pp33_0=0L, pp36_0=0L;

        long firstMsgTS=0L,lastMsgTS=0L;

        String stringInputWithSpace = "bm05 bm06 bm07 bm08 bm09 bm10 pp07 pp08 pp09 pp12 pp15 pp21 pp33 pp36 ts";
        String[] getVaribleFromString = stringInputWithSpace.trim().split("\\s+");
        // for while
        long t= System.currentTimeMillis();
        long end = t+TotalTimeToRun;  // 6 min
        while (System.currentTimeMillis() < end) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(PollTimeOut);
            for (ConsumerRecord<String, GenericRecord> record : records) {
                GenericRecord customer = record.value();
                for (int i = 0; i < getVaribleFromString.length; i++) {
                    String check = nameOf(customer.get(getVaribleFromString[i]));
                    switch (check) {
                        case "Integer":
                        {
                            if (Objects.equals("bm05", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm05_0=bm05_0+1;
                                } else {
                                    bm05_1=bm05_1+1;
                                }
                            }
                            else if (Objects.equals("bm06", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm06_0=bm06_0+1;
                                } else {
                                    bm06_1=bm06_1+1;
                                }
                            }
                            else if (Objects.equals("bm07", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm07_0=bm07_0+1;
                                } else {
                                    bm07_1=bm07_1+1;
                                }
                            }
                            else if (Objects.equals("bm08", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm08_0=bm08_0+1;
                                } else {
                                    bm08_1=bm08_1+1;
                                }
                            }
                            else if (Objects.equals("bm09", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm09_0=bm09_0+1;
                                } else {
                                    bm09_1=bm09_1+1;
                                }
                            }
                            else if (Objects.equals("bm10", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    bm10_0=bm10_0+1;
                                } else {
                                    bm10_1=bm10_1+1;
                                }
                            }
                            else if (Objects.equals("pp07", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp07_0=pp07_0+1;
                                } else {
                                    pp07_1=pp07_1+1;
                                }
                            }
                            else if (Objects.equals("pp08", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp08_0=pp08_0+1;
                                } else {
                                    pp08_1=pp08_1+1;
                                }
                            }
                            else if (Objects.equals("pp09", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp09_0=pp09_0+1;
                                } else {
                                    pp09_1=pp09_1+1;
                                }
                            }
                            else if (Objects.equals("pp12", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp12_0=pp12_0+1;
                                } else {
                                    pp12_1=pp12_1+1;
                                }
                            }
                            else if (Objects.equals("pp15", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp15_0=pp15_0+1;
                                } else {
                                    pp15_1=pp15_1+1;
                                }
                            }
                            else if (Objects.equals("pp21", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp21_0=pp21_0+1;
                                } else {
                                    pp21_1=pp21_1+1;
                                }
                            }
                            else if (Objects.equals("pp33", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp33_0=pp33_0+1;
                                } else {
                                    pp33_1=pp33_1+1;
                                }
                            }
                            else if (Objects.equals("pp36", new String(getVaribleFromString[i])))
                            {
                                int val = 0;
                                val = (int) customer.get(getVaribleFromString[i]);
                                if (val==0)
                                {
                                    pp36_0=pp36_0+1;
                                } else {
                                    pp36_1=pp36_1+1;
                                }
                            }
                        }
                        break;
                        case "Long":
                        {
                            if (Objects.equals("ts", new String(getVaribleFromString[i])))
                            {
                                if (msgCount>0L)
                                {
                                    lastMsgTS=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                                }else{
                                    firstMsgTS=Long.valueOf(customer.get(getVaribleFromString[i]).toString());
                                }
                            }
                        }
                        break;

                        default:
                        {
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

        System.out.println("----- bm: -----");
        System.out.println("(bm05_0)="+bm05_0+" (bm06_0)="+bm06_0+" (bm07_0)="+bm07_0+" (bm08_0)="+bm08_0+" (bm09_0)="+bm09_0+" (bm10_0)="+bm10_0);
        System.out.println("(bm05_1)="+bm05_1+" (bm06_1)="+bm06_1+" (bm07_1)="+bm07_1+" (bm08_1)="+bm08_1+" (bm09_1)="+bm09_1+" (bm10_1)="+bm10_1);
        System.out.println("----- pp: -----");
        System.out.println("(pp07_0)="+pp07_0+" (pp08_0)="+pp08_0+" (pp09_0)="+pp09_0+" (pp12_0)="+pp12_0+" (pp15_0)="+pp15_0+" (pp21_0)="+pp21_0+" (pp33_0)="+pp33_0+" (pp36_0)="+pp36_0);
        System.out.println("(pp07_1)="+pp07_1+" (pp08_1)="+pp08_1+" (pp09_1)="+pp09_1+" (pp12_1)="+pp12_1+" (pp15_1)="+pp15_1+" (pp21_1)="+pp21_1+" (pp33_1)="+pp33_1+" (pp36_1)="+pp36_1);

        Parm (msgCount,firstMsgTS,lastMsgTS);
        consumer.close();
    }
    private static void Parm (long totalmsg,long first, long last)
    {
        System.out.println("----- Parameters: -----");
        System.out.println("Total messages : "+ totalmsg);
        System.out.println("First message Timestamp: "+first);
        System.out.println("Last message Timestamp: "+last);

        long ms=Instant.ofEpochMilli(first)
                .until(Instant.ofEpochMilli(last),
                        ChronoUnit.MILLIS);

        double AvThroughput=((double)ms)/((double)(totalmsg-1L));
        System.out.println("AvThroughput: "+AvThroughput);

        StringBuffer text = new StringBuffer("");
        if (ms > DAY) {
            text.append(ms / DAY).append(" days ");
            ms %= DAY;
        }
        if (ms > HOUR) {
            text.append(ms / HOUR).append(" hours ");
            ms %= HOUR;
        }
        if (ms > MINUTE) {
            text.append(ms / MINUTE).append(" minutes ");
            ms %= MINUTE;
        }
        if (ms > SECOND) {
            text.append(ms / SECOND).append(" seconds ");
            ms %= SECOND;
        }
        text.append(ms + " ms");
        System.out.println("Time between first and last messages: "+text.toString());
    }
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }
}
