package com.debs2012.producers;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.DatumReader;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class producer_customize {
    public static Schema _p_schema;
    private static Properties _p_properties = new Properties();

    public static void main(String[] args) throws IOException {
        //Setting the base environments variable
        String kafkabroker=System.getenv("KAFKA_SERVER").toString();
        String SchemaRegistry = System.getenv("SCHEMA_REG_SERVER").toString();
        String outTopic = System.getenv("OUT_TOPIC").toString();
        Schema.Parser _p_parser= new Schema.Parser();;
        _p_schema = _p_parser.parse(System.getenv("OUT_SCHEMA").toString());
        Long TotalTime = Long.parseLong(System.getenv("TOTAL_TIME").toString());
        Long TimeDelay = Long.parseLong(System.getenv("TIME_DELAY").toString());
        int DuplicationRate = Integer.parseInt(System.getenv("DUPLICATION_RATE").toString());
        String srcPath=System.getenv("DATA_PATH").toString();
        setProp(kafkabroker,SchemaRegistry);

        int msgNumber=0;
        long t= System.currentTimeMillis();
        long end = t+TotalTime;
        FileInputStream inputStream = null;
        Scanner sc = null;

       //Collect all field values to an array list
        List<String> fieldValues = new ArrayList<>();
        for(Field field : _p_schema.getFields()) {
            fieldValues.add(field.name());
        }

        System.out.println("Producing start at: "+System.currentTimeMillis());
        GenericRecordBuilder sendRecordBuilder = new GenericRecordBuilder(_p_schema);
        KafkaProducer<String, GenericRecord> _p_kafkaProducer=new KafkaProducer<String, GenericRecord>(_p_properties);;
        try {
            inputStream = new FileInputStream(srcPath);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine() && System.currentTimeMillis() < end) {
                String line = sc.nextLine();
                System.out.println(msgNumber);
                String[] strs = line.trim().split("\\s+");
                // the work
                sendRecordBuilder.set(fieldValues.get(0), System.currentTimeMillis());
                int i = 1;
                int indexSTR=1;
                while (indexSTR < 55) {
                    String value= strs[indexSTR];
                    String fieldname = fieldValues.get(i);
                    if (i>1) {
                        if (fieldname.equals("pp07")
                                || fieldname.equals("pp08")
                                || fieldname.equals("pp09")
                                || fieldname.equals("pp12")
                                || fieldname.equals("pp15")
                                || fieldname.equals("pp21")
                                || fieldname.equals("pp33")
                                || fieldname.equals("pp36")) {
                            if (DuplicationRate > 0) {
                                sendRecordBuilder.set(fieldname, Integer.parseInt(value));
                                for (int d = 0; d < DuplicationRate; d++) {
                                    i=i+1;
                                    sendRecordBuilder.set(fieldValues.get(i), Integer.parseInt(value));
                                }
                                i=i+1;
                            } else {
                                sendRecordBuilder.set(fieldname, Integer.parseInt(value));
                                i = i + 1;
                            }
                        } else { //other fields
                            sendRecordBuilder.set(fieldname, Integer.parseInt(value));
                            i = i + 1;
                        }
                    }else{
                        sendRecordBuilder.set(fieldname, Long.parseLong(value));
                        i = i + 1;
                    }
                    indexSTR=indexSTR+1;
                }
                GenericData.Record sendRecord = sendRecordBuilder.build();
                ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<String, GenericRecord>(
                        outTopic, sendRecord
                );
                _p_kafkaProducer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e == null) {
                            System.out.println("Success sent Record!");
                            //System.out.println(recordMetadata.toString());
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
                _p_kafkaProducer.flush();
                try {
                    sleep(TimeDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msgNumber = msgNumber + 1;
                //end the work
            }// End of whille looping over data file
            _p_kafkaProducer.close();
            System.out.println("Producing stop at: "+System.currentTimeMillis());
            System.out.println("The total sent messages :" + msgNumber);
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
    }
    public static void setProp ( String kafkaurl, String Schema_reg_url){
        _p_properties.setProperty("bootstrap.servers",kafkaurl);
        _p_properties.setProperty("acks","all");
        _p_properties.setProperty("retries","0");
        _p_properties.setProperty("key.serializer", StringSerializer.class.getName());
        _p_properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
        _p_properties.setProperty("schema.registry.url",Schema_reg_url);
    }
}
