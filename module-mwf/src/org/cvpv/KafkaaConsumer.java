/*
 *
 * First version in 2017.
 * vesrion 5-light (2020).
 * '$Author: Ameer B. A. Alaasam $'
 * alaasam.ameer.b@gmail.com
 *
 */

package org.ameer.kafka;

import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import java.util.Collections;
import java.util.Properties;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * The KafkaaConsumer actor retrieves data from an Kafka server. 
 * Also the schema registry server provided by Confluent must be used.
 * This Actor outputs data as Record of token.
 * Also it able to check if the related environment parameters in the host are set to values,
 * if yes, so set them to the variable in Actor, 
 * or take the values from the GUI.
 * the environment parameters as follow:
 * SOURCE_KAFKA_SERVER
 * SOURCE_SCHEMA_REG_SERVER
 * SOURCE_GROUP_ID
 * SOURCE_PARMS_TO_READ // it can be sets of parameters separated by spaces
 * SOURCE_TOPICS // it can be sets of parameters separated by spaces
 * SOURCE_CONSUME_TIMEOUT
 * 
 * @author Ameer B. A. Alaasam
 */

public class KafkaaConsumer extends LimitedFiringSource {
	
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
	
	public KafkaaConsumer(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		
		output.setTypeEquals(BaseType.RECORD);
		
		checkCallProp = true;
		
		KafkaAddressInputParam = new PortParameter(this, "Kafka Address and Port ");
		SchemaRegistryInputParam = new PortParameter(this, "Schema Registry Address and Port ");
		TopicParam = new PortParameter(this, "Topic To Subscribe ");
		GroupIdParam = new PortParameter(this, "Group Id To Subscribe ");
		TimeoutParam = new PortParameter(this, "Timeout To Poll :");
		ParametersToReadParam = new PortParameter(this, "Parameters To Read ");
		
		KafkaAddressInputParam.setStringMode(true);
		KafkaAddressInputParam.setTypeEquals(BaseType.STRING);
		KafkaAddressInputParam.getPort().setTypeEquals(BaseType.STRING);
		KafkaAddressInputParam.addChoice("192.168.1.2:9092");
		KafkaAddressInputParam.addChoice("localhost:9092");

		
		SchemaRegistryInputParam.setStringMode(true);
		SchemaRegistryInputParam.setTypeEquals(BaseType.STRING);
		SchemaRegistryInputParam.getPort().setTypeEquals(BaseType.STRING);
		SchemaRegistryInputParam.addChoice("http://192.168.1.2:8081");
		SchemaRegistryInputParam.addChoice("http://localhost:8081");
		
		TopicParam.setStringMode(true);
		TopicParam.setTypeEquals(BaseType.STRING);
		TopicParam.getPort().setTypeEquals(BaseType.STRING);
		TopicParam.addChoice("customer-avro");
		TopicParam.addChoice("test-avro");
		
		GroupIdParam.setStringMode(true);
		GroupIdParam.setTypeEquals(BaseType.STRING);
		GroupIdParam.getPort().setTypeEquals(BaseType.STRING);
		GroupIdParam.addChoice("group-1");
		GroupIdParam.addChoice("group-2");
		
		TimeoutParam.setStringMode(true);
		TimeoutParam.setTypeEquals(BaseType.STRING);
		TimeoutParam.getPort().setTypeEquals(BaseType.STRING);
		TimeoutParam.addChoice("5");
		
		ParametersToReadParam.setStringMode(true);
		ParametersToReadParam.setTypeEquals(BaseType.STRING);
		ParametersToReadParam.getPort().setTypeEquals(BaseType.STRING);
		ParametersToReadParam.addChoice("first_name last_name age height weight automated_email");
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		
		if (checkCallProp)
			callProp();
	   
        records = consumer.poll(_timeOut);
        for (ConsumerRecord<String, GenericRecord> record : records){
            GenericRecord customer = record.value();
            String [] getVaribleFromString = _parametersToRead.trim().split("\\s+");
            
            String[] labels = new String[getVaribleFromString.length];
            Token[] values = new Token[getVaribleFromString.length];
              
            for (int i =0 ; i < getVaribleFromString.length;i++)
            {
                String check=nameOf(customer.get(getVaribleFromString[i]));
                switch (check) {
                    case "Utf8":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new StringToken(customer.get(getVaribleFromString[i]).toString());
                    }
                        break;
                        
                    case "Integer":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new IntToken ((Integer) customer.get(getVaribleFromString[i]));
                    }
                        break;
                        
                    case "Long":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new LongToken ((Long) customer.get(getVaribleFromString[i]));
                    }
                        break;
                        
                    case "Double":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new DoubleToken ((Double) customer.get(getVaribleFromString[i]));

                    }
                        break;
                        
                    case "Float":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new FloatToken ((Float) customer.get(getVaribleFromString[i]));
                    }
                        break;
                        
                    case "Boolean":
                    {
                        labels[i] = getVaribleFromString[i];
                        values[i] = new BooleanToken ((Boolean) customer.get(getVaribleFromString[i]));
                    }
                        break;

                    default:
                    {
                    }
                        break;
                }

            }
            
        RecordToken result = new RecordToken(labels, values);

        output.send(0, result); 
            
        }
        consumer.commitSync();
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////	
	
	/** Ip and port for Kafka server. */
	private PortParameter KafkaAddressInputParam;
	
	/** Url and port for Schema registry server. */
	private PortParameter SchemaRegistryInputParam;
	
	/** A set of Kafka topics to consume data from them. */
	private PortParameter TopicParam;
	
	/** Group ID which used by kafka server to define the group of consumer */
	private PortParameter GroupIdParam;
	
	/** The time out of consuming data from a given Kafka topic */
	private PortParameter TimeoutParam;
	
    /** A set of parameters which reflects the fields names which required to read from each incoming message */
	private PortParameter ParametersToReadParam;
	
	private boolean checkCallProp;
	private String _kafkaUrlAndPort = "";
	private String _schemaRegistryUrlAndPort = "";
	private String _topic = "";
	private String _groupId = "";
	private Long _timeOut = 0L;
	private String _parametersToRead = "";
	private Properties properties = new Properties();
	private KafkaConsumer<String, GenericRecord> consumer;
	private ConsumerRecords<String,GenericRecord> records;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////
	
	// Return the type of Object
	private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }
	
	// Set variable value and Kafka properties
	private void callProp () throws IllegalActionException {
		
		/*
		 * This section, check if the related environment parameters in the host are set to values, if yes, 
		 * so set them to the variable in Actor, or take the values from the GUI.
		 */
		//---------------------------------------------
		if (System.getenv("SOURCE_KAFKA_SERVER")!=null)
		{
			if (!System.getenv("SOURCE_KAFKA_SERVER").isEmpty())
			{
				_kafkaUrlAndPort = System.getenv("SOURCE_KAFKA_SERVER").toString();		
			}
			else
			{
				_kafkaUrlAndPort = ((StringToken)KafkaAddressInputParam.getToken()).stringValue();
			}
		}else {
			_kafkaUrlAndPort = ((StringToken)KafkaAddressInputParam.getToken()).stringValue();
		}
		
		if (System.getenv("SOURCE_SCHEMA_REG_SERVER")!=null)
		{
			if (!System.getenv("SOURCE_SCHEMA_REG_SERVER").isEmpty())
			{
				_schemaRegistryUrlAndPort = System.getenv("SOURCE_SCHEMA_REG_SERVER").toString();		
			}
			else
			{
				_schemaRegistryUrlAndPort = ((StringToken)SchemaRegistryInputParam.getToken()).stringValue();
			}
		}else {
			_schemaRegistryUrlAndPort = ((StringToken)SchemaRegistryInputParam.getToken()).stringValue();
		}
		
		if (System.getenv("SOURCE_GROUP_ID")!=null)
		{
			if (!System.getenv("SOURCE_GROUP_ID").isEmpty())
			{
				_groupId = System.getenv("SOURCE_GROUP_ID").toString();		
			}
			else
			{
				_groupId = ((StringToken)GroupIdParam.getToken()).stringValue();
			}
		}else {
			_groupId = ((StringToken)GroupIdParam.getToken()).stringValue();
		}
		
		if (System.getenv("SOURCE_PARMS_TO_READ")!=null)
		{
			if (!System.getenv("SOURCE_PARMS_TO_READ").isEmpty())
			{
				_parametersToRead = System.getenv("SOURCE_PARMS_TO_READ").toString();		
			}
			else
			{
				_parametersToRead = ((StringToken)ParametersToReadParam.getToken()).stringValue();
			}
		}else {
			_parametersToRead = ((StringToken)ParametersToReadParam.getToken()).stringValue();
		}
		
		if (System.getenv("SOURCE_TOPICS")!=null)
		{
			if (!System.getenv("SOURCE_TOPICS").isEmpty())
			{
				_topic = System.getenv("SOURCE_TOPICS").toString();		
			}
			else
			{
				_topic = ((StringToken)TopicParam.getToken()).stringValue();
			}
		}else {
			_topic = ((StringToken)TopicParam.getToken()).stringValue();
		}
		
		if (System.getenv("SOURCE_CONSUME_TIMEOUT")!=null)
		{
			if (!System.getenv("SOURCE_CONSUME_TIMEOUT").isEmpty())
			{
				_timeOut = Long.valueOf(System.getenv("SOURCE_CONSUME_TIMEOUT").toString());		
			}
			else
			{
				_timeOut = Long.valueOf(((StringToken)TimeoutParam.getToken()).stringValue());
			}
		}else {
			_timeOut = Long.valueOf(((StringToken)TimeoutParam.getToken()).stringValue());
		}
		 
		properties.setProperty("bootstrap.servers",_kafkaUrlAndPort);
	    properties.setProperty("group.id",_groupId);
	    properties.setProperty("enable.auto.commit","false");
	    properties.setProperty("auto.offset.reset","earliest");
	    properties.put("max.poll.records",1);
	    properties.setProperty("key.deserializer", StringDeserializer.class.getName());
	    properties.setProperty("value.deserializer", KafkaAvroDeserializer.class.getName());
	    properties.setProperty("schema.registry.url",_schemaRegistryUrlAndPort);  
		 
	    consumer = new KafkaConsumer<String, GenericRecord>(properties);
	    consumer.subscribe(Collections.singleton(_topic));
		checkCallProp = false;
	}	
}