
package sistolico.base;

import hades.models.Const1164;
import hades.models.PortStdLogic1164;
import hades.models.StdLogic1164;
import hades.signals.Signal;
import hades.simulator.Port;
import hades.simulator.SimEvent1164;
import hades.simulator.SimKernel;
import hades.simulator.Simulatable;
import hades.simulator.Wakeable;
import hades.symbols.BboxRectangle;
import hades.symbols.Circle;
import hades.symbols.ClassLabel;
import hades.symbols.InstanceLabel;
import hades.symbols.Polyline;
import hades.symbols.PortLabel;
import hades.symbols.PortSymbol;
import hades.symbols.Symbol;
import hades.utils.StringTokenizer;
import jfig.objects.FigAttribs;

/**
 * ClockGen - a SimObject subclass that models a clock generator with arbitrary
 * frequency, duty cycle and phase. Default is a symmetric 1 Hz clock signal.
 * Signals are expected to be StdLogic1164 objects.<p>
 *
 * <pre>
 *                   +----+     +----+     +----+
 *  +----------------+    +-----+    +-----+    +-----+ ....
 *  |                |          |          |          |
 *  |  offset        |  period  |    |     |
 *                                dutycycle
 * </pre>
 *
 * @author F.N.Hendrich
 * @version 0.2 22.04.97
 *
 * @author Jeronimo Costa Penha - jeronimopenha@gmail.com
 * @author Ricardo Santos Ferreira - cacauvicosa@gmail.com
 * @version 2.0
 */
public class Clock
        extends SisGenericGate
        implements Simulatable, Wakeable, java.io.Serializable {

    private PortStdLogic1164 portClk;
    private StdLogic1164 outputU, output0, output1;

    /* the timing parameters of this clock generator */
    private double period = 1.0, // 1sec period,    i.e. 1Hz
            dutycycle = 0.5, // 50% dutycycle, i.e. symmetric
            offset = 0.0;     // no initial delay
    private int nCycles = 0;

    private boolean running = true;
    private Circle circleOnOff;

    private boolean debug = false;

    /**
     * ClockGen: create a ClockGen with default parameters (1Hz symmetric
     * clock).
     */
    public Clock() {
        super();
        portClk = new PortStdLogic1164(this, "clk", Port.OUT, null);
        ports = new Port[1];
        ports[0] = portClk;

        outputU = Const1164.__U;
        output0 = Const1164.__0;
        output1 = Const1164.__1;
    }

    @Override
    public void setSymbol(Symbol s) {
        symbol = s;
        symbol.setInstanceLabel(name);
        initDisplay();
    }

    public double getPeriod() {
        return period;
    }

    public double getOffset() {
        return offset;
    }

    public double getDutycycle() {
        return dutycycle;
    }

    public void setPeriod(double d) {
        period = d;
    }

    public void setOffset(double d) {
        offset = d;
    }

    public void setDutycycle(double d) {
        dutycycle = d;
        if ((dutycycle < 0.0) || (dutycycle > 1.0)) {
            message("-W- ClockGen: illegal value for 'dutycycle': " + dutycycle);
            message("-W- must be between 0.0 (0%) and 1.0 (100%), using 0.5!");
            dutycycle = 0.5;
        }
    }

    public void setPeriod(String s) {
        period = parse(s);
    }

    public void setOffset(String s) {
        offset = parse(s);
    }

    public void setDutycycle(String s) {
        setDutycycle(parse(s));
    }

    public double parse(String s) {
        double d;
        try {
            d = Double.valueOf(s);
        } catch (NumberFormatException e) {
            d = 0.0;
        }
        return d;
    }

    /**
     * initialize a ClockGen'erator from a String, which contains its integer
     * version id, and the double values for clock period (in seconds),
     * dutycycle (fraction 0.0 .. 1.0), and offset (in seconds), e.g. 1001
     * 1.0E-9 0.5 0.3E-9 for a 1nsec clock.
     */
    @Override
    public boolean initialize(String s) {
        StringTokenizer st = new StringTokenizer(s);
        int n_tokens = st.countTokens();
        try {
            versionId = Integer.parseInt(st.nextToken());

            period = Double.valueOf(st.nextToken());
            dutycycle = Double.valueOf(st.nextToken());
            offset = Double.valueOf(st.nextToken());

            if (debug) {
                message("ClockGen.initialize: " + toString());
            }
        } catch (NumberFormatException e) {
            message("-E- ClockGen.initialize(): " + e + " " + s);
        }
        return true;
    }

    /**
     * write: write versionId, period [seconds], dutycycle [ratio], and initial
     * offset [seconds].
     */
    @Override
    public void write(java.io.PrintWriter ps) {
        ps.print(" " + versionId + " " + period + " "
                + dutycycle + " " + offset);
    }

    /**
     * configure: display a dialog window to specify the ClockGen instance name
     * and the period, initial offset, and duty cycle timing parameters.
     */
    @Override
    public void configure() {
        String[] fields = {"instance name:", "name",
            "period [sec]:", "period",
            "duty cycle [0.0 .. 1.0]:", "dutycycle",
            "offset [sec]:", "offset"
        };

        propertySheet = hades.gui.PropertySheet.getPropertySheet(this, fields);
        propertySheet.setHelpText(
                "ClockGenerator timing parameters:\n"
                + "________---_____---_____---____ ... \n"
                + "^       ^  ^    ^  ^    ^  ^        \n"
                + "|offset |period |period |period ... \n"
                + "        |dc|    |dc|    |dc|    ... \n"
                + "all times are given in seconds ");
        propertySheet.setVisible(true);
    }

    /**
     * start or stop this ClockGen'erator
     * @param me
     */
    @Override
    public void mousePressed(java.awt.event.MouseEvent me) {
        SimKernel simulator = parent.getSimulator();

        if (running) { // stop this ClockGen
            if (debug) {
                message("...stopping " + toString());
            }
            running = false;
            if (simulator != null) {
                simulator.deleteAllEventsFromSource(this);
            }
        } else { // start/restart
            if (debug) {
                message("...restarting " + toString());
            }
            running = true;
            elaborate(null);
        }
        // show current state
        showState();
    }

    public void changeState() {
        SimKernel simulator = parent.getSimulator();

        running = false;
        if (simulator != null) {
            simulator.deleteAllEventsFromSource(this);
        }

        running = true;
        elaborate(null);

        // show current state
        showState();
    }

    private void initDisplay() {
        circleOnOff = new Circle();
        circleOnOff.initialize("600 1200 300 300");
        getSymbol().addMember(circleOnOff);
        showState();
    }

    private void showState() {
        if (getSymbol() == null) {
            return;
        }

        FigAttribs attr = circleOnOff.getAttributes();
        attr.fillStyle = attr.SOLID_FILL;
        if (running) {
            attr.fillColor = output1.getColor();
        } else {
            attr.fillColor = output0.getColor();
        }
        circleOnOff.setAttributes(attr);
        if (circleOnOff.painter != null) {
            circleOnOff.painter.paint(circleOnOff);
        }
    }

    /**
     * elaborate: store a reference to our simulator, then schedule the events
     * for the initial "U" phase, the first "1" phase, and the first wakeup.
     */
    @Override
    public void elaborate(Object arg) {
        if (debug) {
            message(toString() + ".elaborate()" + arg);
        }

        nCycles = 0;

        simulator = parent.getSimulator();
        if (simulator == null) {
            if (debug) {
                message(toString() + "elaborate(): SIMULATOR IS NULL!");
            }
        } else {
            // target, time, source: schedule first wakeup at current time
            // plus our 'offset' time
            double time = simulator.getSimTime();
            simulator.scheduleWakeup(this, time + offset, this);
            if (offset > 0.0) {
                schedule(outputU, time);
            }
            schedule(output1, time + offset);
        }
    }

    /**
     * evaluate: as the ClockGen has no input signals, this method should never
     * be called.
     */
    @Override
    public void evaluate(Object arg) {
        message("-E- " + toString() + ".evaluate()");
        message("-E- Don't call evaluate() on a ClockGen");
    }

    /**
     * wakeup(): Called by the simulator as a reaction to our own
     * scheduleWakeup()-calls. One period of the clock has expired, calculate
     * new values.
     *
     * @param arg - unsued argument
     */
    @Override
    public void wakeup(Object arg) {

        if (running) {
            nCycles++;                        // on wakeup, the previous cycle is done

            double time = simulator.getSimTime();

            schedule(output0, time + dutycycle * period);
            schedule(output1, time + period);
            simulator.scheduleWakeup(this, time + period, this);
        }

    }

    /**
     * internal utility method
     */
    private void schedule(StdLogic1164 value, double time) {
        Signal signal_Y = portClk.getSignal();
        if (signal_Y == null) {
            if (debug) {
                message("ClockGen.evaluate: signal is null...");
            }
        } else {
            simulator.scheduleEvent(
                    SimEvent1164.createNewSimEvent(signal_Y, time, value, portClk));
        }
    }

    /**
     * construct a tool tip message (with the timing parameters)
     */
    @Override
    public String getToolTip(java.awt.Point position, long millis) {
        return getName() + "\n"
                + getClass().getName() + "\n"
                + "period= " + period + " [sec] "
                + "dutycycle= " + (100 * dutycycle) + " [%]\n"
                + "offset= " + offset + " [sec] "
                + "n_cycles= " + nCycles;
    }

    /**
     * toString: generate a short description including class name, full
     * instance name, and the timing parameters.
     */
    @Override
    public String toString() {
        return "ClockGen: " + getFullName() + "[timing: " + period + ","
                + dutycycle + "," + offset + "]";
    }

    /**
     * Method responsible for indicating to the simulator that the component's
     * symbol will be constructed dynamically by the constructDynamicSymbol()
     * method, or will be read from a file of the same name as the ".sym"
     * extension.
     *
     * @return TRUE means that the symbol will be built dynamically.
     */
    @Override
    public boolean needsDynamicSymbol() {
        return true;
    }

    /**
     * Method responsible for dynamically constructing the component symbol.
     */
    @Override
    public void constructDynamicSymbol() {
        this.symbol = new Symbol();
        this.symbol.setParent(this);

        BboxRectangle bbr = new BboxRectangle();
        bbr.initialize("0 0 2400 2400");
        this.symbol.addMember(bbr);

        Polyline polyline;

        polyline = new Polyline();
        polyline.initialize("6 0 0 2100 0 2400 1200 2100 2400 0 2400 0 0");
        this.symbol.addMember(polyline);

        polyline = new Polyline();
        polyline.initialize("8 1200 1650 1425 1650 1425 750 1650 750 1650 1650 1875 1650 1875 750 2100 750");
        this.symbol.addMember(polyline);

        PortSymbol portSymbol = new PortSymbol();
        portSymbol.initialize("2400 1200 " + portClk.getName());
        this.symbol.addMember(portSymbol);

        PortLabel portLabel = new PortLabel();
        portLabel.initialize("2300 1300 3 " + portClk.getName());
        this.symbol.addMember(portLabel);

        ClassLabel classLabel = new ClassLabel();
        classLabel.initialize("200 480 Clock");
        this.symbol.addMember(classLabel);

        InstanceLabel instanceLabel = new InstanceLabel();
        instanceLabel.initialize("200 2300 clk0");
        this.symbol.addMember(instanceLabel);
    }
}
