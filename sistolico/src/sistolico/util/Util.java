package sistolico.util;
//import add.dataflow.base.AddSimObject;
//import add.dataflow.base.ClkConnector;
//import add.dataflow.base.Clock;

import hades.gui.Editor;
import hades.gui.ObjectCanvas;
import hades.models.Design;
import hades.signals.Signal;
import hades.signals.SignalStdLogic1164;
import hades.signals.SignalStdLogicVector;
import hades.simulator.Port;
import hades.symbols.SymbolManager;
import java.awt.Point;
import java.util.Enumeration;

/**
 * Class responsible for providing useful routines for the project.<br>
 *
 * @author Jeronimo Costa Penha - jeronimopenha@gmail.com
 * @version 1.0
 */
public class Util {
    //private static int posX = 2400;
    private static Editor editor;

    /**
     * @param aEditor the editor to set
     */
    public static void setEditor(Editor aEditor) {
        editor = aEditor;
    }

    /*   public static void Check() {
        checkClkComponent();
        checkClkConnectorComponent();
    }

    public static Clock checkClkComponent() {

        Clock clk = null;
        boolean needsRedraw = false;
        boolean foundClock = false;
        try {
            for (Enumeration<SisSimObject> e = getEditor().getDesign().getComponents(); e.hasMoreElements();) {
                SisSimObject obj = e.nextElement();

                if (obj instanceof Clock) {
                    if (obj.getName().equals("CLOCK")) {
                        clk = (Clock) obj;
                        foundClock = true;
                    } else if (!obj.getName().equals("CLOCK")) {
                        Design design = getEditor().getDesign();
                        design.deleteComponent(obj);
                        getEditor().setDesign(design);
                        needsRedraw = true;
                    }
                }
            }

            if (!foundClock) {
                clk = createClkComponent();
                needsRedraw = true;
            }

            if (needsRedraw) {
                editorRedraw();
            }
        } catch (Exception e) {
        }
        return clk;
    }

    public static void checkClkConnectorComponent() {

        boolean needsRedraw = false;
        boolean foundClkConnector = false;
        try {
            for (Enumeration<AddSimObject> e = getEditor().getDesign().getComponents(); e.hasMoreElements();) {
                AddSimObject obj = e.nextElement();

                if (obj instanceof ClkConnector) {
                    if (obj.getName().equals("CLKCONNECTOR")) {
                        foundClkConnector = true;
                    } else if (!obj.getName().equals("CLKCONNECTOR")) {
                        Design design = getEditor().getDesign();
                        design.deleteComponent(obj);
                        getEditor().setDesign(design);
                        needsRedraw = true;
                    }
                }
            }

            if (!foundClkConnector) {
                createRstComponent();
                needsRedraw = true;
            }

            if (needsRedraw) {
                editorRedraw();
            }
        } catch (Exception e) {

        }
    }

    public static void connectClkWire() {
        Signal signal;
        SignalStdLogic1164 clkWire;
        AddSimObject obj;

        boolean clkOk = false;

        for (Enumeration<Signal> e = getEditor().getDesign().getSignals(); e.hasMoreElements();) {
            signal = e.nextElement();
            if (signal instanceof SignalStdLogic1164) {
                if (signal.getName().equals("clk_wire")) {
                    clkOk = true;
                    break;
                }
            }
        }

        if (!clkOk) {
            clkWire = new SignalStdLogic1164("clk_wire");
            getEditor().getDesign().addSignal(clkWire);
        }

        for (Enumeration<AddSimObject> e = getEditor().getDesign().getComponents(); e.hasMoreElements();) {
            obj = e.nextElement();
            if (obj instanceof ClkConnector) {
                continue;
            }
            Port[] ports = obj.getPorts();
            for (int i = 0; i < obj.getPorts().length; i++) {
                if (ports[i].getSignal() == null) {
                    if (ports[i].getName().equals("clk")) {
                        getEditor().getDesign().getSignal("clk_wire").connect(obj.getPort("clk"));
                    }
                }
            }
        }

        //Clock clk = (Clock) getEditor().getDesign().getComponent("CLOCK");
        //clk.changeState();
    }

    public static void disconnectClkWire() {
        Signal signal;

        if (getEditor() != null) {

            if (getEditor().getDesign().getSignals() == null) {
                return;
            }
        }

        for (Enumeration<Signal> e = getEditor().getDesign().getSignals(); e.hasMoreElements();) {
            signal = e.nextElement();
            if (signal instanceof SignalStdLogic1164) {
                if (signal.getName().equals("clk_wire")) {
                    signal.disconnectAll();
                    getEditor().getDesign().deleteSignal(signal);
                    break;
                }
            }
        }
    }

    public static Signal connectComponents(AddSimObject component1, AddSimObject component2, String port1, String port2) {
        SignalStdLogicVector signal;

        //check if ports exists
        if (component1.getPort(port1) == null) {
            return null;
        } else if (component2.getPort(port2) == null) {
            return null;
        }
        //check if the secont port is connected
        if (component2.getPort(port2).getSignal() != null) {
            return null;
        }
        //check if the first port is connected
        if (component1.getPort(port1).getSignal() != null) {
            signal = (SignalStdLogicVector) component1.getPort(port1).getSignal();
            signal.connect(component2.getPort(port2));
        } else {
            signal = new SignalStdLogicVector();
            signal.connect(component1.getPort(port1));
            signal.connect(component2.getPort(port2));
            getEditor().getDesign().addSignal(signal);
        }
        return signal;
    }

    public static Signal connectComponent(SignalStdLogicVector signal, AddSimObject component, String port) {

        //check if signal is not null
        if (signal == null) {
            return null;
        }

        //check if the component port is connected
        if (component.getPort(port).getSignal() != null) {
            return null;
        }

        signal.connect(component.getPort(port));
        return signal;
    }

    public static Clock createClkComponent() {
        Clock clk = new Clock();
        clk.setName("CLOCK");
        createComponent(clk, 2400, 600);
        return clk;
    }

    public static void createRstComponent() {
        ClkConnector clkConnector = new ClkConnector();
        clkConnector.setName("CLKCONNECTOR");
        createComponent(clkConnector, 2400, 4800);
    }

    public static void createComponent(AddSimObject obj) {
        createComponent(obj, posX, 6000);
        posX += 2400;
    }

    public static void createComponent(AddSimObject obj, int posX, int posY) {
        ObjectCanvas canvas;
        Point newPos;

        canvas = getEditor().getObjectCanvas();

        newPos = new Point(posX, posY);
        obj.setEditor(getEditor());
        obj.setVisible(true);
        obj.setSymbol(SymbolManager.getSymbolManager().getSymbol(obj));

        getEditor().getDesign().addComponent(obj);
        obj.getSymbol().setTrafo(canvas.getTrafo());
        obj.getSymbol().move(newPos.x, newPos.y);
        getEditor().insertIntoObjectList(obj.getSymbol());
    }

    public static void editorRedraw() {
        getEditor().getObjectCanvas().doFullRedraw();
    }

    public static int createConfValue(int immediate, int componentId) {
        return (immediate << 8) | componentId;
    }

    /**
     * @return the editor
     */
 /*public static Editor getEditor() {
        return editor;
    }

/*/
}
