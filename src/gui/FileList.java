package gui;

import com.alee.laf.list.WebList;
import controller.TabHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Created by khoi on 4/5/2014.
 */
class FileList extends WebList implements ListSelectionListener{

    private DefaultListModel files;
    private JScrollPane fileListScroll;
    private MainEditor editor;
    //Getters
    JScrollPane getFileListScroll(){
        return fileListScroll;
    }
    public DefaultListModel getFiles(){
        return files;
    }
    public FileList( MainEditor editor){
        this.editor = editor;
        files = new DefaultListModel();
        files.addElement("Untitled");
        setModel(files);
        fileListScroll = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        addListSelectionListener(this);
    }
    @Override
    public void valueChanged(ListSelectionEvent e) {
        int i = 0;
        for (TabHandler tempHandler : editor.getHandlerList()) {
            if (tempHandler.getFileName().equals(this.getSelectedValue())) {
                editor.getTabPane().setSelectedIndex(i);
                break;
            }
            i++;
        }
    }
}
