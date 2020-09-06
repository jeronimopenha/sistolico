
package sistolico.base;

import hades.signals.SignalStdLogicVector;
import java.awt.HeadlessException;
import javax.swing.JOptionPane;

public class SisSimObject extends hades.simulator.SimObject {

    private int afuId = 0;
    private boolean start = false;

    /**
     * simple default constructor.
     */
    public SisSimObject() {
        super();

    }

    public void reset(double time) {
    }

    public SignalStdLogicVector connectTo(SisSimObject targetObj, String targetPort) {
        SignalStdLogicVector signal = null;
        if (getEditor().getDesign().getComponent(targetObj.getName()) == null) {
            return signal;
        }
        if (getPort("dout0").getSignal() == null) {
            signal = new SignalStdLogicVector();
            signal.connect(getPort("dout0"));
            signal.connect(targetObj.getPort(targetPort));
            getEditor().getDesign().addSignal(signal);
        } else {
            signal = (SignalStdLogicVector) getPort("dout0").getSignal();
            signal.connect(targetObj.getPort(targetPort));
        }
        return signal;
    }

    /**
     * @return the afuId
     */
    public int getAfuId() {
        return afuId;
    }

    /**
     * @param afuId the afuId to set
     */
    public void setAfuId(int afuId) {
        this.afuId = afuId;
    }

    /**
     * @param afuId String that brings the afuId to set
     */
    public void setAfuId(String afuId) {
        int n;
        try {
            n = Integer.parseInt(afuId);
        } catch (HeadlessException | NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "SisSimObject.setAfuId: illegal argument" + e
                    + "\nusing '0' for afuId instead!", "Error", JOptionPane.ERROR_MESSAGE);
            n = 0; // default width
        }
        SisSimObject.this.setAfuId(n);
    }

    /**
     * @return the start
     */
    public boolean isStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(boolean start) {
        this.start = start;
    }
}