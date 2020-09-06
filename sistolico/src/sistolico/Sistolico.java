/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistolico;

import hades.gui.Editor;
import java.util.Timer;
import java.util.TimerTask;
import jfig.utils.SetupManager;
import sistolico.util.Util;

/**
 *
 * @author jeronimo
 */
public class Sistolico {

    private Editor mainEditor;
    private Timer supervisor;

    public Sistolico() {
        SetupManager.loadGlobalProperties("hades/.hadesrc");
        SetupManager.setProperty("Hades.Editor.AutoStartSimulation", "true");
        SetupManager.setProperty("Hades.LayerTable.DisplayInstanceBorder", "true");
        SetupManager.setProperty("Hades.LayerTable.DisplayInstanceLabels", "false");
        SetupManager.setProperty("Hades.LayerTable.DisplayClassLabels", "false");
        SetupManager.setProperty("Hades.LayerTable.DisplayPortSymbols", "false");
        SetupManager.setProperty("Hades.LayerTable.DisplayPortLabels", "false");
        SetupManager.setProperty("Hades.LayerTable.DisplayBusPortSymbols", "true");
        SetupManager.setProperty("Hades.LayerTable.RtlibAnimation", "true");
        SetupManager.setProperty("Hades.Editor.EnableToolTips", "true");
        SetupManager.setProperty("Hades.Editor.PopupMenuResource", "/sistolico/base/PopupMenu.txt");

        mainEditor = new Editor(true);
        Util.setEditor(mainEditor);

        //supervisor = new Timer();
        //supervisor.schedule(new TimerTaskHanler(), 1000, 1000);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Sistolico sistolico = new Sistolico();
    }

}
