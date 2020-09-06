
package sistolico.base;

import hades.models.Const1164;
import hades.models.PortStdLogicVector;
import hades.models.StdLogic1164;
import hades.models.StdLogicVector;
import hades.signals.Signal;
import hades.simulator.Port;
import hades.simulator.SimEvent;
import hades.simulator.SimEvent1164;
import hades.simulator.SimKernel;
import hades.simulator.SimObject;
import hades.simulator.Simulatable;
import hades.simulator.Wakeable;
import hades.simulator.WakeupEvent;
import hades.symbols.ColorSource;
import hades.symbols.Color_DIN_IEC_62;
import hades.symbols.ColoredValueLabel;
import hades.symbols.Symbol;
import hades.symbols.TextSource;
import hades.utils.StringTokenizer;
import java.util.Enumeration;
import jfig.utils.SetupManager;

/**
 * GenericRtlibObject - the utility base class for HADES RTLIB models, that is,
 * the "Rechnerbaukasten" models.
 * <p>
 * This class supplies a generic configuration dialog with object name, input
 * bus width (n_bits), default output value, default delay.
 * <p>
 * Signals are expected to be SignalStdLogicVector objects, typically with
 * n_bits bits.
 * <p>
 * This class also implements the default behaviour for a Wakeup-event, which is
 * to update (and repaint) the value label in the component's Symbol.
 * <p>
 * Finally, note that this class implements the assign() method defined in the
 * hades.simulator.Assignable interface. This default implementation expects a
 * (binary, decimal, hex-formatted) string with the new output value, schedules
 * the corresponding event on our vectorOutputPort, and a WakeupEvent which is
 * used to update our internal "vector" value at the corresponding simulation
 * time. Subclasses are free to implement Assignable if this implementation does
 * the right thing.
 *
 * *
 * @author F.N.Hendrich
 * @version 0.2 22.04.97
 *
 * @author Jeronimo Costa Penha - jeronimopenha@gmail.com
 * @author Ricardo Santos Ferreira - cacauvicosa@gmail.com
 * @version 2.0
 */
public class SisGenericRtlibObject
        extends SisSimObject
        implements Simulatable, Wakeable,
        java.io.Serializable {

    protected int n_bits = 16;
    protected StdLogicVector vector,
            vector_UUU, vector_XXX, vector_ZZZ,
            vector_000, vector_111;
    protected PortStdLogicVector vectorOutputPort;

    protected double delay;
    protected double defaultdelay = 10E-9;

    // Graphics stuff
    protected boolean enableAnimationFlag;
    protected ColoredValueLabel valueLabel;
    protected FlexibleLabelFormatter labelFormatter; // initially null

    /**
     * construct a generic Rtlib object.
     *
     * This method inializes the basic SisSimObject with our Port arrays, the
     * default StdLogicVector contants for the 'UUI...U', 'XXX...X'. '000',
     * '111', and disables animation.
     */
    public SisGenericRtlibObject() {
        super();

        delay = defaultdelay;

        constructStandardValues();
        constructPorts();

        enableAnimationFlag = SetupManager.getBoolean(
                "Hades.LayerTable.RtlibAnimation", false);
    }

    protected void constructPorts() {
        ports = null;
        vectorOutputPort = null;
    }

    protected void constructStandardValues() {
        vector_UUU = new StdLogicVector(n_bits, Const1164.__U);
        vector_XXX = new StdLogicVector(n_bits, Const1164.__X);
        vector_ZZZ = new StdLogicVector(n_bits, Const1164.__Z);
        vector_000 = new StdLogicVector(n_bits, Const1164.__0);
        vector_111 = new StdLogicVector(n_bits, Const1164.__1);
        vector = vector_UUU.copy();
    }

    public boolean getEnableAnimationFlag() {
        return enableAnimationFlag;
    }

    public void setEnableAnimationFlag(boolean b) {
        enableAnimationFlag = b;
    }

    public void setEnableAnimationFlag(String s) {
        try {
            setEnableAnimationFlag(s.trim().toLowerCase().startsWith("t"));
        } catch (Exception e) {
            message("-E- illegal value for setEnableAnimationFlag, using false");
            setEnableAnimationFlag(false);
        }
    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double _delay) {
        if (_delay < 0) {
            delay = defaultdelay;
        } else {
            delay = _delay;
        }
    }

    public void setDelay(String s) {
        try {
            delay = new Double(s).doubleValue();
        } catch (Exception e) {
            message("-E- Illegal number format in String '" + s + "'");
            message("-w- Using default value: " + defaultdelay);
            delay = defaultdelay;
        }
    }

    public int getWidth() {
        return n_bits;
    }

    public void setWidth(int _n) {
        if (isConnected()) {
            message("-E- Cannot change the width of an connected RTLIB object!");
            return;
        } else if ((n_bits < 1) || (n_bits > StdLogicVector.MAX_BITS)) {
            message("-E- Bus width out of range [1.."
                    + StdLogicVector.MAX_BITS
                    + "], using 16 instead!");
            n_bits = 16;
        } else {
            n_bits = _n;
        }
        constructStandardValues();
        constructPorts();
        updateSymbol();
    }

    public void setWidth(String s) {
        int n;
        try {
            n = Integer.parseInt(s);
        } catch (Exception e) {
            message("-E- GenericRtlibObject.setWidth: illegal argument" + e);
            message("-E- using a width of 16 bits instead!");
            n = 16; // default width
        }
        setWidth(n);
    }

    public long getValue() {
        return vector.getValue();
    }

    public void setValue(long _value) {
        vector = new StdLogicVector(n_bits, _value);
        updateSymbol();
        scheduleVectorOutputPort();
    }

    public void setValue(String s) {
        vector = vector.copy();  // we need a clone here, because parse()
        vector.parse(s);       // modifies the original StdLogicVector
        updateSymbol();
        scheduleVectorOutputPort();
        // should update the config dialog, if problems occur
    }

    /**
     * return a String representation of the current "vector" value using the
     * selected number formatting (dec/hex/bin/...). This method is also used
     * for the PropertySheet representation.
     */
    public String getFormattedValue() {
        if (labelFormatter != null) {
            return labelFormatter.getText();
        } else {
            return "" + getValue();
        }
    }

    public void setFormattedValue(String s) {
        setValue(s);
    }

    protected void scheduleVectorOutputPort() {
        if (vectorOutputPort != null) { // propagate new value via vectorOutputPort

            StdLogicVector clone = vector.copy();

            //scheduleAfter(0.0, vectorOutputPort, clone);
        }
    }

    public void setNumberFormat(String s) {
        if (labelFormatter == null) {
            return; // no ValueLabel on this object
        }
        try {
            labelFormatter.setNumberFormat(Integer.parseInt(s.trim()));
        } catch (Exception e) {
            message("-W- Illegal number format '" + s + "', using HEX instead.");
            labelFormatter.setNumberFormat(HEX);
        }
    }

    public int getNumberFormat() {
        if (labelFormatter != null) {
            return labelFormatter.getNumberFormat();
        } else {
            return HEX;
        }
    }

    public boolean isConnected() {
        if (ports == null) {
            return false;
        } else {
            for (int i = 0; i < ports.length; i++) {
                if (ports[i].getSignal() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void configure() {
        if (debug) {
            message("-I- starting to configure this " + toString());
        }
        String[] fields = {"instance name:", "name",
            "number of bits [1 .. 62]:", "width",
            "output value [0..1ZX_b]:", "formattedValue",
            "output delay [sec]:", "delay",
            "enable animation: [T/F]", "enableAnimationFlag",
            "format[0=dec, 1=hex, 2=bin]:", "numberFormat",};

        propertySheet = hades.gui.PropertySheet.getPropertySheet(this, fields);
        propertySheet.show(); // try to keep IBM JDK118 happy
        propertySheet.setHelpText(
                "Specify instance name, bus width, delay\n"
                + "and the output value. Recognized formats:\n"
                + "binary: 0100ZXH0_b,\n"
                + "hex:    abcd_h\n"
                + "decimal: 4711\n"
        );
        propertySheet.show();
    }

    public void setSymbol(Symbol s) {
        this.symbol = s;

        symbol.setInstanceLabel(getName());
        initValueLabel();
        updateSymbol();
    }

    protected void initValueLabel() {
        try {
            if (symbol == null) {
                return;
            }

            jfig.objects.FigObject tmp = null;
            for (Enumeration e = symbol.elements(); e.hasMoreElements();) {
                tmp = (jfig.objects.FigObject) e.nextElement();
                if (tmp instanceof ColoredValueLabel) {
                    labelFormatter = new FlexibleLabelFormatter();
                    valueLabel = (ColoredValueLabel) tmp;
                    valueLabel.setTextSource((TextSource) labelFormatter);
                    valueLabel.setColorSource((ColorSource) new DINColorSource());
                }
            }
        } catch (Exception e) {
            message("-E- " + toString() + ".initValueLabel(): " + e);
            e.printStackTrace();
        }
    }

    public class DINColorSource implements ColorSource {

        public java.awt.Color getColor() {
            return Color_DIN_IEC_62.getColor(vector.getValue());
        }
    }


    /*
   * getText(): utility method to get and format the current state / 
   * output value of this RTLIB object.
   * The default implementation in GenericRtlibObject returns a
   * HEX-formatted String of the output vector's value.
     */
    public class HexValueLabelFormatter implements TextSource {

        public HexValueLabelFormatter() {
            super();
        }

        public String getText() {
            return vector.toHexString();
        }
    }

    public class DecimalValueLabelFormatter implements TextSource {

        public DecimalValueLabelFormatter() {
            super();
        }

        public String getText() {
            return vector.toDecString();
        }
    }

    public class BinaryValueLabelFormatter implements TextSource {

        public BinaryValueLabelFormatter() {
            super();
        }

        public String getText() {
            return vector.toBinString();
        }
    }

    public static final int DECIMAL = 0;
    public static final int HEX = 1;
    public static final int BINARY = 2;

    public class FlexibleLabelFormatter implements TextSource {

        private int numberFormat;

        public FlexibleLabelFormatter() {
            super();
            //numberFormat = DECIMAL;
            numberFormat = HEX;  // start with HEX numbers
        }

        public int getNumberFormat() {
            return numberFormat;
        }

        public void setNumberFormat(int i) {
            numberFormat = i;
        }

        public String getText() {
            String sValue;
            switch (numberFormat) {
                case BINARY:
                    sValue = vector.toBinString();
                    break;
                case DECIMAL:
                    sValue = vector.toDecString();
                    break;
                case HEX:
                    sValue = vector.toHexString();
                    break;
                default:
                    sValue = vector.toHexString();
            }
            return sValue;
        }
    }

    public void updateSymbol() {
        if (debug) {
            message("-I- " + toString() + ".updateSymbol: " + vector);
        }

        if (!enableAnimationFlag) {
            return;
        }
        if (symbol == null || !symbol.isVisible()) {
            return;
        }

        //int    intValue = (int) vector.getValue();
        //Color  color    = Color_DIN_IEC_62.getColor( intValue );
        //if (valueLabel != null) valueLabel.setColor( color );
        if (symbol.painter == null) {
            return;
        }
        symbol.painter.paint(symbol, 50 /*msec*/);
    }

    /**
     * elaborate(): This method is called by the simulation engine to initialize
     * this RTLIB object for simulation.
     * <p>
     * GenericRtlibObject just calls updateSymbol() to initialize its graphical
     * representation. Probably, most RTLIB classes won't need to override this
     * method.
     */
    public void elaborate(Object arg) {
        if (debug) {
            System.err.println(toString() + ".elaborate()");
        }

        simulator = parent.getSimulator();
        updateSymbol();
    }

    /**
     * wakeup(): Called by the simulator as a reaction to our own
     * scheduleWakeup()-calls. For RTLIB components, a wakeup() is normally used
     * to update the value label on its graphical symbol. A WakeupEvent for this
     * purpose should have either 'null' or the current 'this' object as its
     * payload.
     * <p>
     * A second use is to update our internal 'vector' variable at a specified
     * simulation time, which is needed to implement the assign() method from
     * interface hades.simulator.Assignable. A WakeupEvent for this purpose is
     * expected to hold a StdLogicVector object (with the 'value' from the
     * assign call) as its payload.
     */
    public void wakeup(Object arg) {
        if (debug) {
            System.err.println(toString() + ".wakeup()");
        }
        try {
            WakeupEvent evt = (WakeupEvent) arg;
            Object tmp = evt.getArg();

            if (tmp instanceof StdLogicVector) { // called via assign: update vector
                StdLogicVector slv = (StdLogicVector) tmp;
                vector = slv.copy();
            } else { // 'traditional' wakeup: do nothing here, just update the symbol
                ;
            }
        } catch (Exception e) {
            System.err.println("-E- " + toString() + ".wakeup: " + arg);
        }
        if (enableAnimationFlag) {
            updateSymbol();
        }
    }

    /**
     * scheduleAfter: an utility method to schedule a SimEvent on the Signal
     * connected to Port "port" with "value" at time "simTime+t_delay". This
     * method checks for NullPointers and may safely be called, even if no
     * simulator is available or if no Signal is connected to port.
     */
    public void scheduleAfter(double t_delay, Port port, Object value) {
        if (value == null) {
            return;
        }
        if (port == null) {
            return;
        }

        SimKernel simulator = getSimulator();
        Signal signal = port.getSignal();

        if (simulator == null) {
            return;
        }
        if (signal == null) {
            return;
        }

        simulator.scheduleEvent(
                new SimEvent(signal, simulator.getSimTime() + t_delay, value, port)
        );
    }

    public void scheduleAfter(double t_delay, Port port, StdLogic1164 value) {
        if (value == null) {
            return;
        }
        if (port == null) {
            return;
        }

        SimKernel simulator = getSimulator();
        Signal signal = port.getSignal();

        if (simulator == null) {
            return;
        }
        if (signal == null) {
            return;
        }

        simulator.scheduleEvent(
                SimEvent1164.createNewSimEvent(
                        signal, simulator.getSimTime() + t_delay, value, port)
        );
    }

    public void wakeupAfter(double t_delay) {
        SimKernel simulator = getSimulator();
        if (simulator == null) {
            return;
        }

        simulator.scheduleWakeup(this, simulator.getSimTime() + t_delay, this);
    }

    /**
     * a default implementation of the assign() method defined in the
     * hades.simulator.Assignable interface. We parse and decode the 'value'
     * string, which is expected to hold a binary-, decimal-, or hex-formatted
     * StdLogicVector value. We then generate and schedule a corresponding
     * simulation event on our outputVectorPort port pin. Finally, we schedule a
     * WakeupEvent for ourselves, which we use to update our internal 'vector'
     * value at the simulation time specified by the call. As we put the decoded
     * StdLogicVector as the payload argument into the WakeupEvent, our wakeup()
     * method is able to distinguish the new (as of 17.07.2002) WakeupEvent from
     * the WakeupEvents used just for updating our (dynamic) graphical symbol.
     */
    public void assign(String value, double simTime) {
        try {
            Signal signal = null;
            StdLogicVector tmp = new StdLogicVector(n_bits);
            tmp.parse(value);

            if ((signal = vectorOutputPort.getSignal()) != null) {
                simulator.scheduleEvent(
                        new SimEvent(signal, simTime, tmp, vectorOutputPort));
            }
            if (visible) {
                simulator.scheduleWakeup(this, simTime, tmp);
            }
        } catch (Exception e) {
            System.err.println("-E- " + toString() + ".assign: " + value);
        }
    }

    /**
     * initialize a RTLIB object from a String which contains the integer
     * version id, the width of this vector, and optionally a String of the
     * desired start value (e.g. "1001 8 01ZXW001" for an 8 bit vector), and
     * optionally the default delay.
     */
    public boolean initialize(String s) {
        StringTokenizer st = new StringTokenizer(s);
        int n_tokens = st.countTokens();
        try {
            if (n_tokens == 0) {
                versionId = 1001;
                n_bits = 16;
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 1) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = 16;
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 2) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = Integer.parseInt(st.nextToken());
                constructStandardValues();
                constructPorts();
            } else if (n_tokens == 3) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = Integer.parseInt(st.nextToken());
                constructStandardValues();
                constructPorts();

                String sv = st.nextToken();
                setValue(sv);
            } else if (n_tokens == 4) {
                versionId = Integer.parseInt(st.nextToken());
                n_bits = Integer.parseInt(st.nextToken());
                constructStandardValues();
                constructPorts();

                setValue(st.nextToken());
                setDelay(st.nextToken());
            } else {
                throw new Exception("invalid number of arguments");
            }
        } catch (Exception e) {
            message("-E- " + toString() + ".initialize(): " + e + " " + s);
            e.printStackTrace();
        }
        return true;
    }

    /**
     * write the following data to PrintWriter ps: our versionId, the width
     * (n_bits), the default output value (vector), and the default delay.
     * Hopefully, many RTLIB classes won't need to override write().
     */
    public void write(java.io.PrintWriter ps) {
        ps.print(" " + versionId
                + " " + n_bits
                + " " + vector.toBinString()
                + " " + delay);
    }

    /**
     * copy(): This function is used to create a clone of this RTLIB object,
     * including the values for width (n_bits), current value (vector),
     * propagation delay, and version ID.
     * <p>
     */
    /*public SimObject copy() {
        SisGenericRtlibObject tmp = null;
        try {
            tmp = (SisGenericRtlibObject) this.getClass().newInstance();
            tmp.setEditor(this.getEditor());
            tmp.setVisible(this.isVisible());
            tmp.setName(this.getName());
            tmp.setClassLoader(this.getClassLoader());

            tmp.setWidth(this.getWidth());
            tmp.setValue(this.getValue());
            tmp.setDelay(this.getDelay());
            tmp.setVersionId(this.getVersionId());
            tmp.setAfuId(this.getAfuId());
            tmp.setStart(this.isStart());
            return (SisSimObject) tmp;
        } catch (Exception e) {
            message("-E- Internal error in GenericRtlibObject.copy(): " + e);
            e.printStackTrace();
            jfig.utils.ExceptionTracer.trace(e);
            return null;
        }
    }*/
    /**
     * copy(): This function is used to create a clone of this RTLIB object,
     * including the values for width (n_bits), current value (vector),
     * propagation delay, and version ID.
     * <p>
     */
    @Override
    public SisSimObject copy() {
        SisGenericRtlibObject tmp = null;
        try {
            tmp = this.getClass().newInstance();
            tmp.setEditor(this.getEditor());
            tmp.setVisible(this.isVisible());
            tmp.setName(this.getName());
            tmp.setClassLoader(this.getClassLoader());
            tmp.setDelay(this.getDelay());
            tmp.setVersionId(this.getVersionId());
            tmp.setAfuId(this.getAfuId());
            tmp.setStart(this.isStart());
            return (SisSimObject) tmp;
        } catch (IllegalAccessException | InstantiationException e) {
            message("-E- Internal error in GenericRtlibObject.copy(): " + e);
            jfig.utils.ExceptionTracer.trace(e);
            return null;
        }
    }

    /**
     * construct a (short) tool tip message.
     */
    public String getToolTip(java.awt.Point position, long millis) {
        return getName() + "\n"
                + getClass().getName() + "\n"
                + "Start = " + isStart() + "\n"
                + "AFU ID = " + getAfuId() + "\n"
                + "value<" + (n_bits - 1) + ":0>= " + vector.toBinString() + "\n"
                + vector.toHexString() + " / " + vector.toDecString();
    }

    public String toString() {
        return getClass().getName() + ": " + getFullName();
    }

}