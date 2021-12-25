/*
 *
 * First version in 2017.
 * vesrion 5-light (2020).
 * '$Author: Ameer B. A. Alaasam $'
 * alaasam.ameer.b@gmail.com
 *
 */
 
package org.ameer.kafka;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.Properties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * The KafkaaProducer actor receives data as Record of tokens from input port, 
 * serializes the data as an Avro message and publishes it into the Kafka message queue 
 * Also the schema registry server provided by Confluent must be used.
 * Also it able to check if the related environment parameters in the host are set to values,
 * if yes, so set them to the variable in Actor, 
 * or take the values from the GUI.
 * the environment parameters as follow:
 * OUT_KAFKA_SERVER
 * OUT_SCHEMA_REG_SERVER
 * OUT_TOPIC
 * OUT_AVRO_SCHEMA
 * OUT_PARMS_TO_READ // it can be sets of parameters separated by spaces
 * 
 * @author Ameer B. A. Alaasam
 */

public class KafkaaProducer extends LimitedFiringSource {
	
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

	public KafkaaProducer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		
		P_checkCallProp = true;
		
		input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.RECORD);
		
		P_KafkaAddressInputParam = new PortParameter(this, "Kafka Address and Port ");
		P_SchemaRegistryInputParam = new PortParameter(this, "Schema Registry Address and Port ");
		P_TopicParam = new PortParameter(this, "Topic To Subscribe ");
		P_SchemaParam = new PortParameter(this, "Schema ");
		P_ParametersToReadParam = new PortParameter(this, "Parameters To Read ");
				
		P_KafkaAddressInputParam.setStringMode(true);
		P_KafkaAddressInputParam.setTypeEquals(BaseType.STRING);
		P_KafkaAddressInputParam.getPort().setTypeEquals(BaseType.STRING);
		P_KafkaAddressInputParam.addChoice("192.168.1.2:9092");
		P_KafkaAddressInputParam.addChoice("localhost:9092");
		
				
		P_SchemaRegistryInputParam.setStringMode(true);
		P_SchemaRegistryInputParam.setTypeEquals(BaseType.STRING);
		P_SchemaRegistryInputParam.getPort().setTypeEquals(BaseType.STRING);
		P_SchemaRegistryInputParam.addChoice("http://192.168.1.2:8081");
		P_SchemaRegistryInputParam.addChoice("http://localhost:8081");
				
		P_TopicParam.setStringMode(true);
		P_TopicParam.setTypeEquals(BaseType.STRING);
		P_TopicParam.getPort().setTypeEquals(BaseType.STRING);
		P_TopicParam.addChoice("Kepler-customer-avro");
		P_TopicParam.addChoice("test-avro");
				
		P_SchemaParam.setStringMode(true);
		P_SchemaParam.setTypeEquals(BaseType.STRING);
		P_SchemaParam.getPort().setTypeEquals(BaseType.STRING);
		P_SchemaParam.addChoice("none");
		
			
		P_ParametersToReadParam.setStringMode(true);
		P_ParametersToReadParam.setTypeEquals(BaseType.STRING);
		P_ParametersToReadParam.getPort().setTypeEquals(BaseType.STRING);
		P_ParametersToReadParam.addChoice("first_name last_name age height weight automated_email");
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		
		Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        
        if (P_checkCallProp)
			callProp();
        
        String [] getVaribleFromString = _p_parametersToRead.trim().split("\\s+");
        
        String myTopic=_p_topic;
        
        if (input.hasToken(0)) {
        	
        	RecordToken in_record = (RecordToken) input.get(0);
        	
        	
        	GenericRecordBuilder sendRecordBuilder = new GenericRecordBuilder(_p_schema);
        	
        	for (int i =0 ; i < getVaribleFromString.length;i++)
            {
        		if (in_record.get(getVaribleFromString[i])==null)
        		{
        			System.out.println("No such field in record: "+getVaribleFromString[i]);
        		}else {
        		String check=nameOf(in_record.get(getVaribleFromString[i]));
                switch (check) {
                    case "StringToken":
                    {
                    	sendRecordBuilder.set(getVaribleFromString[i], in_record.get(getVaribleFromString[i]).toString());
                    }
                        break;
                        
                    case "IntToken":
                    {
                    	Integer  value = Integer.parseInt( in_record.get(getVaribleFromString[i]).toString());
                    	sendRecordBuilder.set(getVaribleFromString[i], value );

                    }
                        break;
                        
                    case "LongToken":
                    {
                    	Long value = Long.valueOf( in_record.get(getVaribleFromString[i]).toString().replace("L", ""));
                    	sendRecordBuilder.set(getVaribleFromString[i], value );
                    }
                        break;
                        
                    case "DoubleToken":
                    {
                    	Double value = Double.parseDouble( in_record.get(getVaribleFromString[i]).toString());
                    	sendRecordBuilder.set(getVaribleFromString[i], value );
                    }
                        break;
                        
                    case "FloatToken":
                    {
                    	Float value = Float.parseFloat( in_record.get(getVaribleFromString[i]).toString());
                    	sendRecordBuilder.set(getVaribleFromString[i], value );
                    }
                        break;
                        
                    case "BooleanToken":
                    {
                    	Boolean value = Boolean.parseBoolean( in_record.get(getVaribleFromString[i]).toString());
                    	sendRecordBuilder.set(getVaribleFromString[i], value );
                    }
                        break;

                    default:
                    {
                    }
                        break;
                }
        		}

            }
        	
			// Appending the sending timestamp
        	sendRecordBuilder.set("k_send_ts", System.currentTimeMillis());
        	
        	GenericData.Record sendRecord = sendRecordBuilder.build();
        	
        	ProducerRecord<String, GenericRecord> producerRecord = new ProducerRecord<String, GenericRecord>(
        			myTopic,sendRecord
            );
        	
        	_p_kafkaProducer.send(producerRecord, new Callback() {
                 @Override
                 public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                     if (e == null) {
                         System.out.println("Success!");
                     } else {
                         e.printStackTrace();
                     }
                 }
             });
        	_p_kafkaProducer.flush();
        	
        }
        
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////	
	
	/** Ip and port for Kafka server. */
	private static PortParameter P_KafkaAddressInputParam;
	
	/** Url and port for Schema registry server. */
	private static PortParameter P_SchemaRegistryInputParam;
	
	/** Kafka topic to publishes the data. */
	private static PortParameter P_TopicParam;
	
	/** The schema */
	private static PortParameter P_SchemaParam;
	
	/** A set of parameters which reflects the fields names  required to read from each incoming record */
	private static PortParameter P_ParametersToReadParam;
	
	TypedIOPort input;
	
    private static boolean P_checkCallProp;
	
	private static String _p_kafkaUrlAndPort = "";
	private static String _p_schemaRegistryUrlAndPort = "";
	private static String _p_topic = "";
	private static String _p_schemaStr = "";
	private static String _p_parametersToRead = "";
	
	private static Schema.Parser _p_parser;
	private static Schema _p_schema;
	
	private static Properties _p_properties = new Properties();
	
	private static KafkaProducer<String, GenericRecord> _p_kafkaProducer;
	
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////
	
	// Return the type of Object
		private static String nameOf(Object o) {
	        return o.getClass().getSimpleName();
	    }
		
		// Set variable value and Kafka properties
		private static void callProp () throws IllegalActionException {
			
			/*
			 * This section, check if the related environment parameters in the host are set to values, if yes, 
			 * so set them to the variable in Actor, or take the values from the GUI.
			 */
			//---------------------------------------------
			if (System.getenv("OUT_KAFKA_SERVER")!=null)
			{
				if (!System.getenv("OUT_KAFKA_SERVER").isEmpty())
				{
					_p_kafkaUrlAndPort = System.getenv("OUT_KAFKA_SERVER").toString();		
				}
				else
				{
					_p_kafkaUrlAndPort = ((StringToken)P_KafkaAddressInputParam.getToken()).stringValue();
				}
			}else {
				_p_kafkaUrlAndPort = ((StringToken)P_KafkaAddressInputParam.getToken()).stringValue();
			}
			
			if (System.getenv("OUT_SCHEMA_REG_SERVER")!=null)
			{
				if (!System.getenv("OUT_SCHEMA_REG_SERVER").isEmpty())
				{
					_p_schemaRegistryUrlAndPort = System.getenv("OUT_SCHEMA_REG_SERVER").toString();		
				}
				else
				{
					_p_schemaRegistryUrlAndPort = ((StringToken)P_SchemaRegistryInputParam.getToken()).stringValue();
				}
			}else {
				_p_schemaRegistryUrlAndPort = ((StringToken)P_SchemaRegistryInputParam.getToken()).stringValue();
			}
			
			if (System.getenv("OUT_TOPIC")!=null)
			{
				if (!System.getenv("OUT_TOPIC").isEmpty())
				{
					_p_topic = System.getenv("OUT_TOPIC").toString();		
				}
				else
				{
					_p_topic = ((StringToken)P_TopicParam.getToken()).stringValue();
				}
			}else {
				_p_topic = ((StringToken)P_TopicParam.getToken()).stringValue();
			}
			
			if (System.getenv("OUT_AVRO_SCHEMA")!=null)
			{
				if (!System.getenv("OUT_AVRO_SCHEMA").isEmpty())
				{
					_p_schemaStr = System.getenv("OUT_AVRO_SCHEMA").toString();		
				}
				else
				{
					_p_schemaStr = ((StringToken)P_SchemaParam.getToken()).stringValue();
				}
			}else {
				_p_schemaStr = ((StringToken)P_SchemaParam.getToken()).stringValue();
			}
			
			
			if (System.getenv("OUT_PARMS_TO_READ")!=null)
			{
				if (!System.getenv("OUT_PARMS_TO_READ").isEmpty())
				{
					_p_parametersToRead = System.getenv("OUT_PARMS_TO_READ").toString();		
				}
				else
				{
					_p_parametersToRead = ((StringToken)P_ParametersToReadParam.getToken()).stringValue();
				}
			}else {
				_p_parametersToRead = ((StringToken)P_ParametersToReadParam.getToken()).stringValue();
			}
			_p_properties.setProperty("bootstrap.servers",_p_kafkaUrlAndPort);
			_p_properties.setProperty("acks","all");
			_p_properties.setProperty("retries","0");
			_p_properties.setProperty("key.serializer", StringSerializer.class.getName());
			_p_properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
			_p_properties.setProperty("schema.registry.url",_p_schemaRegistryUrlAndPort);			
			_p_parser = new Schema.Parser();			
			_p_schema = _p_parser.parse(_p_schemaStr);			
			_p_kafkaProducer=new KafkaProducer<String, GenericRecord>(_p_properties);			
			P_checkCallProp = false;
		}	

}