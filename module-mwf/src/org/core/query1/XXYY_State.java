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
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.ArrayToken;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class XXYY_State extends TypedAtomicActor  {

    TypedIOPort Firstinput;
    TypedIOPort outArrTs;
    TypedIOPort outArrDt;
    
    private int arrSize;
    private int arrloop;
    
    private PortParameter s_ts;
    private String _s_ts;
    private PortParameter s_dt;
    private String _s_dt;
    private Token[] ArrayTS;
    private Token[] ArrayDT;
    
    public XXYY_State(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        Firstinput = new TypedIOPort(this, "Firstinput", true, false);
        Firstinput.setTypeEquals(BaseType.RECORD);

        arrSize=100;
        arrloop=0;
        
        ArrayTS=new Token[arrSize];
        ArrayDT=new Token[arrSize];
        
        
        outArrTs=new TypedIOPort(this, "outArrTs", false, true);

        outArrTs.setTypeEquals(BaseType.DOUBLE);
        outArrTs.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        
        outArrDt=new TypedIOPort(this, "outArrTt", false, true);

        outArrDt.setTypeEquals(BaseType.DOUBLE);
        outArrDt.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        s_ts = new PortParameter(this, "s_ts ");
        s_ts.setStringMode(true);
        s_ts.setTypeEquals(BaseType.STRING);
        s_ts.getPort().setTypeEquals(BaseType.STRING);
        
        s_dt = new PortParameter(this, "s_dt ");
        s_dt.setStringMode(true);
        s_dt.setTypeEquals(BaseType.STRING);
        s_dt.getPort().setTypeEquals(BaseType.STRING);
 
    }
	
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        
        _s_ts = ((StringToken)s_ts.getToken()).stringValue();
        String pts = _s_ts;
        _s_dt = ((StringToken)s_dt.getToken()).stringValue();
        String pdt = _s_dt;
        
        if (Firstinput.hasToken(0)) {
        	RecordToken record = (RecordToken) Firstinput.get(0);
        	
        	if (record.get(pts)!=null)
        	{
				LongToken inputToken = (LongToken) record.get(pts);
            	Long inputValue = Long.valueOf( inputToken.toString().replace("L", ""));
                DoubleToken pptts = new DoubleToken(inputValue.doubleValue());
                
                LongToken inputTokendt = (LongToken) record.get(pdt);
                Long inputValuedt = Long.valueOf( inputTokendt.toString().replace("L", ""));
                DoubleToken pptdt = new DoubleToken(inputValuedt.doubleValue());
                
                outArrTs.broadcast(pptts);
                outArrDt.broadcast(pptdt);
        	}
        }

    }
    
}
