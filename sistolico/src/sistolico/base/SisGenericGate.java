
package sistolico.base;

import hades.models.StdLogic1164;
import hades.signals.Signal;
import hades.simulator.Port;
import hades.simulator.SimEvent1164;
import hades.simulator.SimObject;
import hades.simulator.Simulatable;
import hades.utils.StringTokenizer;


/** 
 * GenericGate - base class to provide common stuff for logic gates.
 * All Signals are expected to be StdLogic1164 objects.
 * This class provides one timing parameter for the gate propagation delay.
 *
 * @author F.N.Hendrich
 * @version 0.14  31.03.01
 */
public class  SisGenericGate 
       extends  SisSimObject
       implements  Simulatable, 
                   hades.utils.ContextToolTip,
                   java.io.Serializable 
{

  /* the timing parameters of this Nand gate */
  protected double t_delay = 5.0E-9; // 5 ns


  public SisGenericGate() { 
    super();
  }


  public double getDelay() { return t_delay; }

  public void setDelay( double t ) { t_delay = t; }

  public void setDelay( String s ) { 
    try {
      t_delay = Double.valueOf( s ).doubleValue();
    }
    catch( Exception e ) {
      message( "-E- GenericGate.setDelay(): " + e );
      t_delay = 5.0E-9;
      message( "-I- resetting to default value, delay = " + t_delay );
    }
  }


  public void configure() {
    if (debug) message( "-I- starting to configure this " + toString() );
    String[] fields = { "instance name:", "name",
                        "gate delay [sec]:", "delay" }; 

    propertySheet = hades.gui.PropertySheet.getPropertySheet( this, fields );
    // propertySheet.setHelpText(  "Specify instance name and gate delay:" );
    propertySheet.setHelpText(  "" ); // don't show help text area
    propertySheet.setVisible( true );
  }




  /**
   * initialize a GenericGate from a String that contains the integer 
   * version id of this gate and its propagation delay (in seconds), e.g.
   * '1001 0.5E-8'. 
   */
  public boolean initialize( String s ) {
    StringTokenizer st = new StringTokenizer( s );
    int n_tokens = st.countTokens();
    try {
      versionId = Integer.parseInt( st.nextToken() );

      if (n_tokens == 2) {
         t_delay   = Double.valueOf( st.nextToken()).doubleValue();
      }

      if (debug) message( "GenericGate.initialize: " + toString() );
    }
    catch( Exception e ) {
      message( "-E- GenericGate.initialize(): " + e + " " + s );
    }
    return true;
  }


  /**
   * write "versionId" and gate delay "t_delay"
   */
  public void write( java.io.PrintWriter ps ) {
    ps.print( " " + versionId + " " + t_delay );
  }



  /**
   * elaborate: just store a reference to the current simulator
   */
  public void elaborate( Object arg ) {
    if (debug) message( "-I- " + toString() + ".elaborate()" );
    simulator = parent.getSimulator();
  }


  /**
   * utility method to schedule a simulator event at the given simulation time.
   */
  public void scheduleEvent( Signal        signal, 
                             double        time, 
                             StdLogic1164  value,
                             Port          port ) 
  {
    simulator.scheduleEvent(
      SimEvent1164.createNewSimEvent( signal, time, value, port )
    );
  }


  /**
   * utility method to schedule a simulator event, after delay seconds
   * from the current simulation time.
   */
  public void scheduleEventAfter( Signal        signal, 
                                  double        delay,
                                  StdLogic1164  value,
                                  Port          port ) 
  {
    double time = simulator.getSimTime() + delay;
    simulator.scheduleEvent(
      SimEvent1164.createNewSimEvent( signal, time, value, port )
    );
  }

  /**
   * utility method to schedule a simulator event at the given simulation time
   */
  public void 
  scheduleOutputValue( Port port, StdLogic1164 value, double time ) {
    Signal signal = port.getSignal();
    if (signal == null) return;

    simulator.scheduleEvent(
      SimEvent1164.createNewSimEvent( signal, time, value, port )
    );
  }


  /**
   * utility method to schedule a simulator event, after delay seconds
   * from the current simulation time.
   */
  public void 
  scheduleOutputValueAfter( Port port, StdLogic1164 value, double delay ) {
    Signal signal = port.getSignal();
    if (signal == null) return;

    double time = simulator.getSimTime() + delay;
    simulator.scheduleEvent(
      SimEvent1164.createNewSimEvent( signal, time, value, port )
    );
  }



  /**
   * create a copy of the current GenericGate with the same gate delay.
   */
  public SimObject copy() {
    SisGenericGate clone = (SisGenericGate) super.copy();
    clone.setDelay( this.getDelay() );
    return clone;
  }



  /**
   * construct a (short) tool tip message for a logic gate with name,
   * class name, and gate delay.
   */
  public String getToolTip( java.awt.Point position, long millis ) {
    return   getName() + "\n"
           + getClass().getName() + "\n"
           + "delay=" + t_delay;
 
  }

} 