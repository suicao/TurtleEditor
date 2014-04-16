package controller;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;

import javax.swing.*;
import javax.swing.undo.UndoManager;

/**
 * Created by khoi on 4/5/2014.
 */
public class TabHandler {
    private UndoManager manager;
    private String fileName;
    private WebButton closeBtn;
    private Boolean changed;
    private WebLabel tabLabel;
    private JPanel pnlTab;
    JEditorPane editorPane;
    private WebLabel caretInfo;

    //Getters
    UndoManager getUndoManager(){
        return manager;
    }
    public String getFileName(){
        return String.valueOf(fileName);
    }
    public WebLabel getCaretInfo(){
        return caretInfo;
    }
    public JEditorPane getEditorPane(){
        return editorPane;
    }
    boolean isChanged(){
        if(changed){
            return true;
        }
        return false;
    }

    //Setters
    public void setChanged(boolean b){
        changed = b;
    }
    public void setFileName(String s){
        fileName = s;
    }
    public void setTabLabel(String s){
        pnlTab.removeAll();
        tabLabel = new WebLabel(s);
        pnlTab.add(tabLabel);
        pnlTab.add(closeBtn);
        pnlTab.revalidate();
        pnlTab.repaint();
    }
    public TabHandler(JEditorPane editorPane,UndoManager manager,String fileName, WebLabel caretInfo,
                      WebButton closeBtn, WebLabel tabLabel, JPanel pnlTab,Boolean changed){
        this.caretInfo = caretInfo;
        this.editorPane = editorPane;
        this.manager = manager;
        this.fileName =fileName;
        this.closeBtn = closeBtn;
        this.changed =changed;
        this.tabLabel = tabLabel;
        this.pnlTab = pnlTab;
    }
    public TabHandler(){

    }
}
