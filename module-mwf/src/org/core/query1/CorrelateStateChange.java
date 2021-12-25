/*
 *
 * vesrion 5-light (2020).
 * '$Author: Ameer B. A. Alaasam $'
 * alaasam.ameer.b@gmail.com
 *
 */
 
package org.ameer.debs2012.query1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
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

public class CorrelateStateChange extends LimitedFiringSource {
	
	TypedIOPort input;
	TypedIOPort OutRec;
    
    private static HashMap<String, Object> dbMap0;
    private static HashMap<String, Object> dbMap1;
    
    private static boolean checkCallProp;
    private static HashMap<String, Object> finalMap;
    private static RecordToken resultRec;
    
    private static PortParameter inTopic;
    private static String Topic;
    
    private static PortParameter xxEdges;
    private static String [] ArrayxxEdges;
    private static PortParameter xxTS;
    private static String [] ArrayxxTS;
    
    private static PortParameter yyEdges;
    private static String [] ArrayyyEdges;
    private static PortParameter yyTS;
    private static String [] ArrayyyTS;
    
    private static PortParameter outDt;
    private static String [] ArrayOutDt; 
    
    private static PortParameter outTS;
    private static String [] ArrayOutTS;
    

	public CorrelateStateChange(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		
		checkCallProp = true;
        finalMap = new HashMap<String, Object>();
        resultRec = new RecordToken ();
		
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.RECORD);
        
		OutRec = new TypedIOPort(this, "OutRec", false, true);
		OutRec.setTypeEquals(BaseType.RECORD);
        
        dbMap0 = new HashMap<String, Object>();
        dbMap1 = new HashMap<String, Object>();
        
        inTopic = new PortParameter(this, "Topic");
        inTopic.setStringMode(true);
        inTopic.setTypeEquals(BaseType.STRING);
        inTopic.getPort().setTypeEquals(BaseType.STRING);
        
        xxEdges = new PortParameter(this, "xx Edges to compare");
        xxEdges.setStringMode(true);
        xxEdges.setTypeEquals(BaseType.STRING);
        xxEdges.getPort().setTypeEquals(BaseType.STRING);
        
        xxTS = new PortParameter(this, "xx Timestamp");
        xxTS.setStringMode(true);
        xxTS.setTypeEquals(BaseType.STRING);
        xxTS.getPort().setTypeEquals(BaseType.STRING);
        
        yyEdges = new PortParameter(this, "yy Edges to compare");
        yyEdges.setStringMode(true);
        yyEdges.setTypeEquals(BaseType.STRING);
        yyEdges.getPort().setTypeEquals(BaseType.STRING);
        
        yyTS = new PortParameter(this, "yy Timestamp");
        yyTS.setStringMode(true);
        yyTS.setTypeEquals(BaseType.STRING);
        yyTS.getPort().setTypeEquals(BaseType.STRING);
        
        outDt = new PortParameter(this, "Output Dt");
        outDt.setStringMode(true);
        outDt.setTypeEquals(BaseType.STRING);
        outDt.getPort().setTypeEquals(BaseType.STRING);
              
        outTS = new PortParameter(this, "Output timestamps");
        outTS.setStringMode(true);
        outTS.setTypeEquals(BaseType.STRING);
        outTS.getPort().setTypeEquals(BaseType.STRING);
        
	}

	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		
		if (checkCallProp)
			callProp(checkCallProp);
		
		if (input.hasToken(0)) {
			RecordToken newRec= new RecordToken ();
			newRec=(RecordToken) input.get(0);
			GetCorrelateState (newRec);
			if (resultRec == null) {
				resultRec = new RecordToken ();
			}else {
				OutRec.send(0,resultRec);
				resultRec = new RecordToken ();
			}
		}
	}

		private void GetCorrelateState (RecordToken inRec) throws IllegalActionException
	    {
		 
		    int EdgesNumber = ArrayxxEdges.length;
		    
		    for (int i=0;i<EdgesNumber;i++)
		    {
		    	
		    	if (inRec.get(Topic+"_"+ArrayxxEdges[i])==null)
		    	{
		    		if (inRec.get(Topic+"_"+ArrayyyEdges[i])!=null)
		    		{
						Integer stateinRec=Integer.parseInt(inRec.get(Topic+"_"+ArrayyyEdges[i]).toString());
						saveToDbMap (stateinRec, "y","","", Topic+"_"+ArrayyyEdges[i], Topic+"_"+ArrayyyTS[i],inRec );
		    		}
		    	}else {	
		    		Integer stateinRec = Integer.parseInt(inRec.get(Topic+"_"+ArrayxxEdges[i]).toString()); // get xx from record
		    		  switch (stateinRec) {
	                     case 0:
	                     {
	                    	 if (checkField(stateinRec,Topic+"_"+ArrayyyEdges[i])) { //while xx 0, so, check if there is an yy in dbMap0 
	                    		 if (inRec.get(Topic+"_"+"msg1_index")==null)
	                    		 {
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxEdges[i],new IntToken(Integer.parseInt(inRec.get(Topic+"_"+ArrayxxEdges[i]).toString())));
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxTS[i],new LongToken(Long.valueOf(inRec.get(Topic+"_"+ArrayxxTS[i]).toString().replace("L", ""))));
		                    		finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyEdges[i],new IntToken(Integer.parseInt(dbMap0.get(Topic+"_"+ArrayyyEdges[i]).toString())));
		                    		finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyTS[i],new LongToken(Long.valueOf(dbMap0.get(Topic+"_"+ArrayyyTS[i]).toString().replace("L", ""))));
		                    		finalMap.put(ArrayOutDt[i]+"_0", new LongToken (returnxxyyDurationv2 (stateinRec,Topic+"_"+ArrayxxTS[i],Topic+"_"+ArrayyyTS[i], inRec)));
		     		    			finalMap.put(ArrayOutTS[i]+"_0",dbMap0.get(Topic+"_"+ArrayyyTS[i]));
	                    		 }else {
	                    			 
	                    			finalMap.put(ArrayOutDt[i]+"_rec_msg1_id",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg1_index").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_msg1_ts",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg1_org_ts").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_msg2_id",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg2_index").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_msg2_ts",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg2_org_ts").toString().replace("L", ""))));
	 	                                  		
	 	                    		finalMap.put(ArrayOutDt[i]+"_db_msg1_id",new LongToken(Long.valueOf( dbMap0.get(Topic+"_"+ArrayyyEdges[i]+"_msg1_index").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_db_msg1_ts",new LongToken(Long.valueOf( dbMap0.get(Topic+"_"+ArrayyyEdges[i]+"_msg1_org_ts").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_db_msg2_id",new LongToken(Long.valueOf( dbMap0.get(Topic+"_"+ArrayyyEdges[i]+"_msg2_index").toString().replace("L", ""))));
	 	                    		finalMap.put(ArrayOutDt[i]+"_db_msg2_ts",new LongToken(Long.valueOf( dbMap0.get(Topic+"_"+ArrayyyEdges[i]+"_msg2_org_ts").toString().replace("L", ""))));	                    			 
	                    		
	 	                      		finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxEdges[i],new IntToken(Integer.parseInt(inRec.get(Topic+"_"+ArrayxxEdges[i]).toString())));
	 	                    		finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxTS[i],new LongToken(Long.valueOf(inRec.get(Topic+"_"+ArrayxxTS[i]).toString().replace("L", ""))));
		                    		finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyEdges[i],new IntToken(Integer.parseInt(dbMap0.get(Topic+"_"+ArrayyyEdges[i]).toString())));
		                    		finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyTS[i],new LongToken(Long.valueOf(dbMap0.get(Topic+"_"+ArrayyyTS[i]).toString().replace("L", ""))));
		                    		finalMap.put(ArrayOutDt[i]+"_0", new LongToken (returnxxyyDurationv2 (stateinRec,Topic+"_"+ArrayxxTS[i],Topic+"_"+ArrayyyTS[i], inRec)));
		     		    			finalMap.put(ArrayOutTS[i]+"_0",dbMap0.get(Topic+"_"+ArrayyyTS[i]));
	                    		 }
	     		    			saveToDbMap (stateinRec, "x",Topic+"_"+ArrayxxEdges[i],Topic+"_"+ArrayxxTS[i], "", "",inRec );
	     		    			if (inRec.get(Topic+"_"+ArrayyyEdges[i])!=null)
	     			    		{     			    			
	     			    			Integer stateYinRec = Integer.parseInt(inRec.get(Topic+"_"+ArrayyyEdges[i]).toString());
									saveToDbMap (stateYinRec, "y","","", Topic+"_"+ArrayyyEdges[i], Topic+"_"+ArrayyyTS[i],inRec );
	     			    		}	                    		 
	                    	 }else {
	                    		 saveToDbMap (stateinRec, "x",Topic+"_"+ArrayxxEdges[i],Topic+"_"+ArrayxxTS[i], "", "",inRec );
	     		    			if (inRec.get(Topic+"_"+ArrayyyEdges[i])!=null)
	     			    		{
									Integer stateYinRec = Integer.parseInt(inRec.get(Topic+"_"+ArrayyyEdges[i]).toString());
	     			    			saveToDbMap (stateYinRec, "y","","", Topic+"_"+ArrayyyEdges[i], Topic+"_"+ArrayyyTS[i],inRec );
	     			    		}
	     		    		}

	                     }
	                         break;
	                         
	                     case 1:
	                     {
	                    	 if (checkField(stateinRec,Topic+"_"+ArrayyyEdges[i])) 
							 {
	                    		 if (inRec.get(Topic+"_"+"msg1_index")==null)
	                    		 {
			                    	finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxEdges[i],new IntToken(Integer.parseInt(inRec.get(Topic+"_"+ArrayxxEdges[i]).toString())));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxTS[i],new LongToken(Long.valueOf(inRec.get(Topic+"_"+ArrayxxTS[i]).toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyEdges[i],new IntToken(Integer.parseInt(dbMap1.get(Topic+"_"+ArrayyyEdges[i]).toString())));
			                    	finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyTS[i],new LongToken(Long.valueOf(dbMap1.get(Topic+"_"+ArrayyyTS[i]).toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_1", new LongToken (returnxxyyDurationv2 (stateinRec,Topic+"_"+ArrayxxTS[i],Topic+"_"+ArrayyyTS[i], inRec)));
		     		    			finalMap.put(ArrayOutTS[i]+"_1",dbMap1.get(Topic+"_"+ArrayyyTS[i]));
	                    		 }else {
	                    			finalMap.put(ArrayOutDt[i]+"_rec_msg1_id",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg1_index").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_msg1_ts",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg1_org_ts").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_msg2_id",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg2_index").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_msg2_ts",new LongToken(Long.valueOf( inRec.get(Topic+"_"+"msg2_org_ts").toString().replace("L", ""))));	
			                    	finalMap.put(ArrayOutDt[i]+"_db_msg1_id",new LongToken(Long.valueOf( dbMap1.get(Topic+"_"+ArrayyyEdges[i]+"_msg1_index").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_db_msg1_ts",new LongToken(Long.valueOf( dbMap1.get(Topic+"_"+ArrayyyEdges[i]+"_msg1_org_ts").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_db_msg2_id",new LongToken(Long.valueOf( dbMap1.get(Topic+"_"+ArrayyyEdges[i]+"_msg2_index").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_db_msg2_ts",new LongToken(Long.valueOf( dbMap1.get(Topic+"_"+ArrayyyEdges[i]+"_msg2_org_ts").toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxEdges[i],new IntToken(Integer.parseInt(inRec.get(Topic+"_"+ArrayxxEdges[i]).toString())));
			                    	finalMap.put(ArrayOutDt[i]+"_rec_"+ArrayxxTS[i],new LongToken(Long.valueOf(inRec.get(Topic+"_"+ArrayxxTS[i]).toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyEdges[i],new IntToken(Integer.parseInt(dbMap1.get(Topic+"_"+ArrayyyEdges[i]).toString())));
			                    	finalMap.put(ArrayOutDt[i]+"_db_"+ArrayyyTS[i],new LongToken(Long.valueOf(dbMap1.get(Topic+"_"+ArrayyyTS[i]).toString().replace("L", ""))));
			                    	finalMap.put(ArrayOutDt[i]+"_1", new LongToken (returnxxyyDurationv2 (stateinRec,Topic+"_"+ArrayxxTS[i],Topic+"_"+ArrayyyTS[i], inRec)));
		     		    			finalMap.put(ArrayOutTS[i]+"_1",dbMap1.get(Topic+"_"+ArrayyyTS[i])); 
	                    		 }
	     		    			saveToDbMap (stateinRec, "x",Topic+"_"+ArrayxxEdges[i],Topic+"_"+ArrayxxTS[i], "", "",inRec );
	     		    			if (inRec.get(Topic+"_"+ArrayyyEdges[i])!=null)
	     			    		{    			    			
	     			    			Integer stateYinRec = Integer.parseInt(inRec.get(Topic+"_"+ArrayyyEdges[i]).toString());
									saveToDbMap (stateYinRec, "y","","", Topic+"_"+ArrayyyEdges[i], Topic+"_"+ArrayyyTS[i],inRec );
	     			    		}
	                    	 }else {
	                    		 saveToDbMap (stateinRec, "x",Topic+"_"+ArrayxxEdges[i],Topic+"_"+ArrayxxTS[i], "", "",inRec );
	     		    			if (inRec.get(Topic+"_"+ArrayyyEdges[i])==null)
	     			    		{
	     			    			
	     			    		}else {
	     			    			Integer stateYinRec = Integer.parseInt(inRec.get(Topic+"_"+ArrayyyEdges[i]).toString());
									saveToDbMap (stateYinRec, "y","","", Topic+"_"+ArrayyyEdges[i], Topic+"_"+ArrayyyTS[i],inRec );
	     			    		}
	     		    		}
	                     }
	                         break;
		    		  }
		    		
		    	}
		    }
			
			if (finalMap.size()>0)
			{
				 int finalRecordSize = finalMap.size();
				 
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
		         resultRec = new RecordToken(labels, values);
		         finalMap.clear();
			}else resultRec= null;
	    }
		
		private boolean checkField (Integer control, String name)
		{
			if (control==0)
			{
				if (dbMap0.get(name) == null)
					return false;
				else return true;
			}else {
				if (dbMap1.get(name) == null)
					return false;
				else return true;				
			}
		}
		
		private Long returnxxyyDurationv2 (Integer control,String xxname,String yyname, RecordToken record)
		{
			Long duration =0L,previousTS=0L,currentTS=0L;
			
			if (control==0)
			{
			currentTS=Long.valueOf( record.get(xxname).toString().replace("L", ""));
			previousTS=Long.valueOf( dbMap0.get(yyname).toString().replace("L", ""));
			}else {
				currentTS=Long.valueOf( record.get(xxname).toString().replace("L", ""));
				previousTS=Long.valueOf( dbMap1.get(yyname).toString().replace("L", ""));
			}
			
			duration=Instant.ofEpochMilli(previousTS)
	        .until(Instant.ofEpochMilli(currentTS),
	                ChronoUnit.MILLIS);
			
			return duration;
		}
		private void saveToDbMap (Integer controlState01, String controlStateXY,String xxname,String xxTSlocal, String yyname, String yyTSlocal,RecordToken record )
		{
			 switch (controlStateXY)
			 {
			 case "x":
             {
            	 if (controlState01==0)
     			{
            		 if (record.get(Topic+"_"+"msg1_index")==null)
            		 {
      		    		dbMap0.put(xxname,record.get(xxname));
      		    		dbMap0.put(xxTSlocal,record.get(xxTSlocal));
            		 }else {
      		    		dbMap0.put(xxname+"_msg1_index",record.get(Topic+"_"+"msg1_index"));
         				dbMap0.put(xxname+"_msg1_org_ts",record.get(Topic+"_"+"msg1_org_ts"));
         				dbMap0.put(xxname+"_msg1_rcv_ts",record.get(Topic+"_"+"msg1_rcv_ts"));
         				dbMap0.put(xxname+"_msg2_index",record.get(Topic+"_"+"msg2_index"));
         				dbMap0.put(xxname+"_msg2_org_ts",record.get(Topic+"_"+"msg2_org_ts"));
         				dbMap0.put(xxname+"_msg2_rcv_ts",record.get(Topic+"_"+"msg2_rcv_ts"));
     		    		dbMap0.put(xxname,record.get(xxname));
     		    		dbMap0.put(xxTSlocal,record.get(xxTSlocal));
            		 }
		 
     			}else {
     				if (record.get(Topic+"_"+"msg1_index")==null)
     				{
    		    		dbMap1.put(xxname,record.get(xxname));
    		    		dbMap1.put(xxTSlocal,record.get(xxTSlocal));
     				}else {
    		    		dbMap1.put(xxname+"_msg1_index",record.get(Topic+"_"+"msg1_index"));
        				dbMap1.put(xxname+"_msg1_org_ts",record.get(Topic+"_"+"msg1_org_ts"));
        				dbMap1.put(xxname+"_msg1_rcv_ts",record.get(Topic+"_"+"msg1_rcv_ts"));
        				dbMap1.put(xxname+"_msg2_index",record.get(Topic+"_"+"msg2_index"));
        				dbMap1.put(xxname+"_msg2_org_ts",record.get(Topic+"_"+"msg2_org_ts"));
        				dbMap1.put(xxname+"_msg2_rcv_ts",record.get(Topic+"_"+"msg2_rcv_ts"));
    		    		dbMap1.put(xxname,record.get(xxname));
    		    		dbMap1.put(xxTSlocal,record.get(xxTSlocal));
     				}
     			}
             }
                 break;
                 
             case "y":
             {
            	 if (controlState01==0)
      			{
            		if( record.get(Topic+"_"+"msg1_index")==null)
            		{
      		    		dbMap0.put(yyname,record.get(yyname));
      		    		dbMap0.put(yyTSlocal,record.get(yyTSlocal));
            		}else {
      		    		dbMap0.put(yyname+"_msg1_index",record.get(Topic+"_"+"msg1_index"));
          				dbMap0.put(yyname+"_msg1_org_ts",record.get(Topic+"_"+"msg1_org_ts"));
          				dbMap0.put(yyname+"_msg1_rcv_ts",record.get(Topic+"_"+"msg1_rcv_ts"));
          				dbMap0.put(yyname+"_msg2_index",record.get(Topic+"_"+"msg2_index"));
          				dbMap0.put(yyname+"_msg2_org_ts",record.get(Topic+"_"+"msg2_org_ts"));
          				dbMap0.put(yyname+"_msg2_rcv_ts",record.get(Topic+"_"+"msg2_rcv_ts"));
      		    		dbMap0.put(yyname,record.get(yyname));
      		    		dbMap0.put(yyTSlocal,record.get(yyTSlocal));
            		}
		 
      			}else {
      				if (record.get(Topic+"_"+"msg1_index")==null)
      				{
      					dbMap1.put(yyname,record.get(yyname));
      		    		dbMap1.put(yyTSlocal,record.get(yyTSlocal));
      				}else {
      		    		dbMap1.put(yyname+"_msg1_index",record.get(Topic+"_"+"msg1_index"));
          				dbMap1.put(yyname+"_msg1_org_ts",record.get(Topic+"_"+"msg1_org_ts"));
          				dbMap1.put(yyname+"_msg1_rcv_ts",record.get(Topic+"_"+"msg1_rcv_ts"));
          				dbMap1.put(yyname+"_msg2_index",record.get(Topic+"_"+"msg2_index"));
          				dbMap1.put(yyname+"_msg2_org_ts",record.get(Topic+"_"+"msg2_org_ts"));
          				dbMap1.put(yyname+"_msg2_rcv_ts",record.get(Topic+"_"+"msg2_rcv_ts"));
      		    		dbMap1.put(yyname,record.get(yyname));
      		    		dbMap1.put(yyTSlocal,record.get(yyTSlocal));
      					
      				}	
      			}

             }
                 break;
                 
             case "xy":
             {
             }
                 break;

			 }
			 
		}
		
			private static String nameOf(Object o) {
		        return o.getClass().getSimpleName();
		    }
	
	private void callProp (boolean cont) throws IllegalActionException {
		ArrayxxEdges= (((StringToken)xxEdges.getToken()).stringValue()).trim().split("\\s+");
		ArrayxxTS=(((StringToken)xxTS.getToken()).stringValue()).trim().split("\\s+");
		
		ArrayyyEdges= (((StringToken)yyEdges.getToken()).stringValue()).trim().split("\\s+");
		ArrayyyTS=(((StringToken)yyTS.getToken()).stringValue()).trim().split("\\s+");
		
		ArrayOutDt= (((StringToken)outDt.getToken()).stringValue()).trim().split("\\s+");
		ArrayOutTS= (((StringToken)outTS.getToken()).stringValue()).trim().split("\\s+");
		
		Topic=((StringToken)inTopic.getToken()).stringValue();
		
		checkCallProp = false;
	}

}
