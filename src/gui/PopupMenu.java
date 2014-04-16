package gui;

import com.alee.extended.window.WebPopOver;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.managers.hotkey.Hotkey;

/**
 * Created by khoi on 4/5/2014.
 */
class PopupMenu extends WebPopupMenu {
    MainEditor editor;
    public PopupMenu(MainEditor editor){
        this.editor = editor;
        final WebMenuItem copy = new WebMenuItem("Copy", Hotkey.CTRL_C);
        copy.addActionListener(editor.Copy);
        final WebMenuItem cut = new WebMenuItem("Cut", Hotkey.CTRL_X);
        cut.addActionListener(editor.Cut);
        final WebMenuItem paste = new WebMenuItem("Paste", Hotkey.CTRL_V);
        paste.addActionListener(editor.Paste);
        final WebMenuItem selectAll = new WebMenuItem("Select all", Hotkey.CTRL_A);
        selectAll.addActionListener(editor.SelectAll);
        final WebMenuItem fandR = new WebMenuItem("Find and Replace", Hotkey.CTRL_F);
        fandR.addActionListener(editor.getActionHandler().FindReplace);
        add(copy);
        add(cut);
        add(paste);
        add(selectAll);
        add(fandR);
    }
}
