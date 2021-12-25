package com.debs2012;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import static java.lang.Thread.sleep;
public class SourceProducer {
    public static void main(String[] args) throws IOException {
        String kafkaServer=args[0].toString();
        String SchemaRegistry=args[1].toString();
        String topic = args[2].toString();
        Long TotalTime = Long.parseLong(args[3].toString());
        Long TimeDelay = Long.parseLong(args[4].toString());
        String srcPath=args[5].toString();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers",kafkaServer);
        properties.setProperty("acks","all");
        properties.setProperty("retries","0");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("schema.registry.url",SchemaRegistry);
        KafkaProducer<String, Debs2012DataPointV2> kafkaProducer = new KafkaProducer<>(properties);
        int msgNumber=0;
        long t= System.currentTimeMillis();
        long end = t+TotalTime;
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(srcPath);
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine() && System.currentTimeMillis() < end ) {
                String line = sc.nextLine();
                System.out.println(msgNumber);
                String[] strs = line.trim().split("\\s+");
                Debs2012DataPointV2 dataPoint = Debs2012DataPointV2.newBuilder()
                        .setTs(System.currentTimeMillis())
                        .setIndex(Long.parseLong(strs[1]))
                        .setMf01(Integer.parseInt(strs[2]))
                        .setMf02(Integer.parseInt(strs[3]))
                        .setMf03(Integer.parseInt(strs[4]))
                        .setPc13(Integer.parseInt(strs[5]))
                        .setPc14(Integer.parseInt(strs[6]))
                        .setPc15(Integer.parseInt(strs[7]))
                        .setPc25(Integer.parseInt(strs[8]))
                        .setPc26(Integer.parseInt(strs[9]))
                        .setPc27(Integer.parseInt(strs[10]))
                        .setRes(Integer.parseInt(strs[11]))
                        .setBm05(Integer.parseInt(strs[12]))
                        .setBm06(Integer.parseInt(strs[13]))
                        .setBm07(Integer.parseInt(strs[14]))
                        .setBm08(Integer.parseInt(strs[15]))
                        .setBm09(Integer.parseInt(strs[16]))
                        .setBm10(Integer.parseInt(strs[17]))
                        .setPp01(Integer.parseInt(strs[18]))
                        .setPp02(Integer.parseInt(strs[19]))
                        .setPp03(Integer.parseInt(strs[20]))
                        .setPp04(Integer.parseInt(strs[21]))
                        .setPp05(Integer.parseInt(strs[22]))
                        .setPp06(Integer.parseInt(strs[23]))
                        .setPp07(Integer.parseInt(strs[24]))
                        .setPp08(Integer.parseInt(strs[25]))
                        .setPp09(Integer.parseInt(strs[26]))
                        .setPp10(Integer.parseInt(strs[27]))
                        .setPp11(Integer.parseInt(strs[28]))
                        .setPp12(Integer.parseInt(strs[29]))
                        .setPp13(Integer.parseInt(strs[30]))
                        .setPp14(Integer.parseInt(strs[31]))
                        .setPp15(Integer.parseInt(strs[32]))
                        .setPp16(Integer.parseInt(strs[33]))
                        .setPp17(Integer.parseInt(strs[34]))
                        .setPp18(Integer.parseInt(strs[35]))
                        .setPp19(Integer.parseInt(strs[36]))
                        .setPp20(Integer.parseInt(strs[37]))
                        .setPp21(Integer.parseInt(strs[38]))
                        .setPp22(Integer.parseInt(strs[39]))
                        .setPp23(Integer.parseInt(strs[40]))
                        .setPp24(Integer.parseInt(strs[41]))
                        .setPp25(Integer.parseInt(strs[42]))
                        .setPp26(Integer.parseInt(strs[43]))
                        .setPp27(Integer.parseInt(strs[44]))
                        .setPp28(Integer.parseInt(strs[45]))
                        .setPp29(Integer.parseInt(strs[46]))
                        .setPp30(Integer.parseInt(strs[47]))
                        .setPp31(Integer.parseInt(strs[48]))
                        .setPp32(Integer.parseInt(strs[49]))
                        .setPp33(Integer.parseInt(strs[50]))
                        .setPp34(Integer.parseInt(strs[51]))
                        .setPp35(Integer.parseInt(strs[52]))
                        .setPp36(Integer.parseInt(strs[53]))
                        .setPc01(Integer.parseInt(strs[54]))
                        .build();
                ProducerRecord<String, Debs2012DataPointV2> producerRecord = new ProducerRecord<String, Debs2012DataPointV2>(
                        topic,dataPoint
                );
                kafkaProducer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e==null){
                            System.out.println("Success sent :"+ dataPoint);
                            //System.out.println(recordMetadata.toString());
                        }else{
                            e.printStackTrace();
                        }
                    }
                });
                kafkaProducer.flush();
                try {
                    sleep(TimeDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                msgNumber=msgNumber+1;
            }
            kafkaProducer.close();
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
}
