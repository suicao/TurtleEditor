package text;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebEditorPane;

import javax.swing.undo.UndoManager;
import javax.swing.*;

/**
 * Created by khoi on 3/29/2014.
 */
public class TabHandler {
    UndoManager manager;
    String fileName;
    WebButton closeBtn;
    Boolean changed;
    WebLabel tabLabel;
    JPanel pnlTab;
    WebEditorPane editorPane;
    WebLabel caretInfo;
    public TabHandler(WebEditorPane editorPane,UndoManager manager,String fileName, WebLabel caretInfo,
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
