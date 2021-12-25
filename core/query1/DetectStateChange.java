/*
 *
 * First version in 2017.
 * vesrion 5-light (2020).
 * '$Author: Ameer B. A. Alaasam $'
 * alaasam.ameer.b@gmail.com
 *
 */
 
package org.ameer.debs2012.query1;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class DetectStateChange extends TypedAtomicActor  {

    TypedIOPort input;
    TypedIOPort output;
    private RecordToken tempRec;
    private long op_index;
   
    
    private static PortParameter inPP;
    private static String [] ArrayinPP;
    
    private static PortParameter inTS;
    private static String TSinput;
    
    private static PortParameter inTopic;
    private static String Topic;
    
    private static PortParameter inIndex;
    private static String Index;
    
    private static PortParameter inMsgRcvTime;
    private static String MsgRcvTime;
    
    private static PortParameter outEdges;
    private static String [] ArrayOutEdges;
    
    private static PortParameter outTS;
    private static String [] ArrayOutTS;
    
    private static boolean checkCallProp;
    private static HashMap<String, Object> finalMap;
    private static RecordToken resultRec;
    
    public DetectStateChange(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        checkCallProp = true;
        finalMap = new HashMap<String, Object>();
        resultRec = new RecordToken ();

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.RECORD);
        tempRec=null;
        op_index=1L;
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.RECORD);
        
        inPP = new PortParameter(this, "Input Sensors field");
        inPP.setStringMode(true);
        inPP.setTypeEquals(BaseType.STRING);
        inPP.getPort().setTypeEquals(BaseType.STRING);
        
        inTS = new PortParameter(this, "Input Timestamp field");
        inTS.setStringMode(true);
        inTS.setTypeEquals(BaseType.STRING);
        inTS.getPort().setTypeEquals(BaseType.STRING);
        
        inTopic = new PortParameter(this, "Topic");
        inTopic.setStringMode(true);
        inTopic.setTypeEquals(BaseType.STRING);
        inTopic.getPort().setTypeEquals(BaseType.STRING);
        
        inIndex = new PortParameter(this, "Msg Index");
        inIndex.setStringMode(true);
        inIndex.setTypeEquals(BaseType.STRING);
        inIndex.getPort().setTypeEquals(BaseType.STRING);
        
        
        inMsgRcvTime = new PortParameter(this, "Msg rcv time");
        inMsgRcvTime.setStringMode(true);
        inMsgRcvTime.setTypeEquals(BaseType.STRING);
        inMsgRcvTime.getPort().setTypeEquals(BaseType.STRING);
         
        outEdges = new PortParameter(this, "Output edges");
        outEdges.setStringMode(true);
        outEdges.setTypeEquals(BaseType.STRING);
        outEdges.getPort().setTypeEquals(BaseType.STRING);
              
        outTS = new PortParameter(this, "Output timestamps");
        outTS.setStringMode(true);
        outTS.setTypeEquals(BaseType.STRING);
        outTS.getPort().setTypeEquals(BaseType.STRING);
    }


    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        
		if (checkCallProp)
			callProp(checkCallProp);
        
        if (input.hasToken(0)) {
        	
        	if (tempRec==null) {
    			System.out.println("no record 1");
    			tempRec= new RecordToken();
    			tempRec=(RecordToken) input.get(0);
    			op_index=op_index+1L;
    		}		
    		else {
    			RecordToken newRec= new RecordToken ();
    			newRec=(RecordToken) input.get(0);
    			if (newRec == null) {
    				System.out.println("no record 2");
    			}
    			else {
    				GetTotalStateChange(tempRec,newRec,op_index);
    				if (resultRec == null) {
    					tempRec=newRec;
    					resultRec = new RecordToken ();
        			}else {
        				output.send(0,resultRec);
        				op_index=op_index+1L;
        				tempRec=newRec;
        				resultRec = new RecordToken ();
        			}
    			}
    			
    		}
        			   	
        }

    }
    
    private void GetTotalStateChange (RecordToken record1, RecordToken record2, long index) throws IllegalActionException
    {
        int ppNumber = ArrayinPP.length;
		
		for (int i=0;i<ppNumber;i++)
        {
	        Integer  firstcome = Integer.parseInt(record1.get(ArrayinPP[i]).toString());
	    	Integer  Secondcome = Integer.parseInt(record2.get(ArrayinPP[i]).toString());
	    	
	    	if (firstcome==0 && Secondcome==1) {
	    		finalMap.put(Topic+"_"+ArrayOutEdges[i],record2.get(ArrayinPP[i]));
	    		finalMap.put(Topic+"_"+ArrayOutTS[i],record2.get(TSinput));
	    	}else if (firstcome==1 && Secondcome==0) {
	    		finalMap.put(Topic+"_"+ArrayOutEdges[i],record2.get(ArrayinPP[i]));
	    		finalMap.put(Topic+"_"+ArrayOutTS[i],record2.get(TSinput));
	    	}
        }
		
		if (finalMap.size()>0)
		{
			
			
    		finalMap.put(Topic+"_"+"msg1_index",record1.get(Topic+"_"+Index));
    		finalMap.put(Topic+"_"+"msg1_org_ts",record1.get(TSinput));
    		finalMap.put(Topic+"_"+"msg1_rcv_ts",record1.get(Topic+"_"+MsgRcvTime));
    		finalMap.put(Topic+"_"+"msg2_index",record2.get(Topic+"_"+Index));
    		finalMap.put(Topic+"_"+"msg2_org_ts",record2.get(TSinput));
    		finalMap.put(Topic+"_"+"msg2_rcv_ts",record2.get(Topic+"_"+MsgRcvTime));
    		
			 int finalRecordSize = finalMap.size()+1;
			 
		     String[] labels = new String[finalRecordSize];
		     Token[] values = new Token[finalRecordSize];
		     
		     Iterator it = finalMap.entrySet().iterator();
		     int iterateRecord=0;
		     while (it.hasNext()) {
		         Map.Entry pair = (Map.Entry)it.next();
		         labels[iterateRecord] = pair.getKey().toString();
	                switch (nameOf(pair.getValue())) {
	                    case "StringToken":
	                    {
	                    	values[iterateRecord] = (StringToken) pair.getValue();
	                    }
	                        break;
	                        
	                    case "IntToken":
	                    {
	                    	values[iterateRecord] = (IntToken) pair.getValue();

	                    }
	                        break;
	                        
	                    case "LongToken":
	                    {
	                    	values[iterateRecord] = (LongToken) pair.getValue();

	                    }
	                        break;
	                        
	                    case "DoubleToken":
	                    {
	                    	values[iterateRecord] = (DoubleToken) pair.getValue();

	                    }
	                        break;
	                        
	                    case "FloatToken":
	                    {
	                    	values[iterateRecord] = (FloatToken) pair.getValue();

	                    }
	                        break;
	                        
	                    case "BooleanToken":
	                    {
	                    	values[iterateRecord] = (BooleanToken) pair.getValue();
	                    }
	                        break;

	                    default:
	                    {
	                    }
	                        break;
	                }
		         iterateRecord=iterateRecord+1;
		         it.remove();
		     }
		        
		     labels[finalRecordSize-1] = "k_st_chng_ts";
	         values[finalRecordSize-1] = new LongToken ((Long) System.currentTimeMillis());
	         resultRec = new RecordToken(labels, values);
	         finalMap.clear();
		}else resultRec= null;
    }
	private void callProp (boolean cont) throws IllegalActionException {	     
		ArrayinPP = (((StringToken)inPP.getToken()).stringValue()).trim().split("\\s+");
		ArrayOutEdges= (((StringToken)outEdges.getToken()).stringValue()).trim().split("\\s+");
		ArrayOutTS= (((StringToken)outTS.getToken()).stringValue()).trim().split("\\s+");
		TSinput=((StringToken)inTS.getToken()).stringValue();
		Topic=((StringToken)inTopic.getToken()).stringValue();
		Index=((StringToken)inIndex.getToken()).stringValue();
		MsgRcvTime=((StringToken)inMsgRcvTime.getToken()).stringValue();
		checkCallProp = false;
	}
	private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }
}

