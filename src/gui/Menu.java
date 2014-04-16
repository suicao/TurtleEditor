package gui;
import controller.*;
import com.alee.laf.menu.*;
import com.alee.managers.hotkey.Hotkey;
import jsyntaxpane.actions.DeleteLinesAction;
import jsyntaxpane.actions.DuplicateLinesAction;
import jsyntaxpane.actions.GotoLineAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by khoi on 4/5/2014.
 */
class Menu extends WebMenuBar {
    protected MainEditor editor;
    private ActionHandler action;

    /**Constructor*/
    public Menu(final MainEditor editor){
        this.editor = editor;
        action= editor.getActionHandler();
        //Menu bar
        setUndecorated(false);
        //Các menu trên thanh menu:
        WebMenu edit = new EditMenu("Edit", new ImageIcon(getClass().getResource("/resources/images/edit.png")));
        WebMenu file = new FileMenu("File", new ImageIcon(getClass().getResource("/resources/images/file.png")));
        WebMenu help = new HelpMenu("Help", new ImageIcon(getClass().getResource("/resources/images/help.png")));
        WebMenu format = new FormatMenu("Format", new ImageIcon(getClass().getResource("/resources/images/format.png")));


        //Thêm vào thanh menu
        add(file);
        add(edit);
        add(format);
        add(help);
    }

    private class FileMenu extends WebMenu {
        private FileMenu(String name, ImageIcon icon){
            setText(name);
            setIcon(icon);
            // File menu in menu bar
            final WebMenuItem newFile,open, save, saveAs, close, closeAll, quit;
            newFile = new WebMenuItem("New file", Hotkey.CTRL_N);
            newFile.addActionListener(action.New);

            open = new WebMenuItem("Open", Hotkey.CTRL_O);
            open.addActionListener(action.Open);

            save = new WebMenuItem("Save", Hotkey.CTRL_S);
            save.addActionListener(action.Save);

            saveAs = new WebMenuItem("Save as", Hotkey.CTRL_SHIFT_S);
            saveAs.addActionListener(action.SaveAs);

            close = new WebMenuItem("Close tab", Hotkey.CTRL_W);
            close.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.closeCurrentTab(editor.getTabPane(),editor.getCurrentHandler(),editor.getHandlerList());
                }
            });
            closeAll = new WebMenuItem("Close all tab", Hotkey.ALT_W);
            closeAll.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.closeAll(editor.getTabPane(),editor.getHandlerList());
                }
            });
            quit = new WebMenuItem("Quit", Hotkey.ALT_F4);
            quit.addActionListener(action.Quit);

            add(newFile);
            add(open);
            addSeparator();
            add(save);
            add(saveAs);
            add(close);
            add(closeAll);
            add(quit);
        }
    }


    private class EditMenu extends WebMenu{
        private class LineMenu extends WebMenu{
            private LineMenu(String name){
                setText(name);
                WebMenuItem deleteLine,duplicateLine,gotoLine;
                deleteLine = new WebMenuItem("Delete line", Hotkey.CTRL_E);
                deleteLine.addActionListener(new DeleteLinesAction());

                duplicateLine = new WebMenuItem("Duplicate line");
                duplicateLine.addActionListener(new DuplicateLinesAction());

                gotoLine = new WebMenuItem("Go to line", Hotkey.CTRL_G);
                gotoLine.addActionListener(new GotoLineAction());
                add(deleteLine);
                add(duplicateLine);
                add(gotoLine);
            }
        }
        private class FindMenu extends WebMenu{
            private FindMenu(String name){
                setText(name);
                WebMenuItem find,findNext,findPrev,replace,replaceAll;
                find = new WebMenuItem("Find and Replace", Hotkey.CTRL_F);
                find.addActionListener(action.FindReplace);

                findNext = new WebMenuItem("Find next", Hotkey.ALT_F);
                findNext.addActionListener(action.findReplace.getNext());

                findPrev = new WebMenuItem("Find previous", Hotkey.ALT_G);
                findPrev.addActionListener(action.findReplace.getPrevious());

                replace = new WebMenuItem("Replace", Hotkey.ALT_H);
                replace.addActionListener(action.findReplace.getReplace());

                replaceAll = new WebMenuItem("Replace all", Hotkey.ALT_J);
                replaceAll.addActionListener(action.findReplace.getReplaceAll());

                add(find);
                add(findNext);
                add(findPrev);
                add(replace);
                add(replaceAll);
            }
        }

        /**Constructor*/
        private EditMenu(String name,ImageIcon icon){
            setText(name);
            setIcon(icon);
            // Edit menu in menu bar
            WebMenuItem undo, redo, cut, copy, paste;

            WebMenu findMenu, lineMenu;

            undo = new WebMenuItem("Undo", Hotkey.CTRL_Z);
            undo.addActionListener(action.Undo);

            redo = new WebMenuItem("Redo", Hotkey.CTRL_Y);
            redo.addActionListener(action.Redo);

            //Các chức năng khác
            cut = new WebMenuItem("Cut", Hotkey.CTRL_X);
            cut.addActionListener(editor.Cut);

            copy = new WebMenuItem("Copy", Hotkey.CTRL_C);
            copy.addActionListener(editor.Copy);

            paste = new WebMenuItem("Paste", Hotkey.CTRL_V);
            paste.addActionListener(editor.Paste);

            findMenu = new FindMenu("Find and Replace");

            lineMenu = new LineMenu("Line");

            add(undo);
            add(redo);
            addSeparator();
            add(cut);
            add(copy);
            add(paste);
            addSeparator();
            add(findMenu);
            add(lineMenu);
        }

    }
    private class HelpMenu extends WebMenu {
        private HelpMenu(String name,ImageIcon icon){
            setText(name);
            setIcon(icon);
            WebMenuItem about = new WebMenuItem("About us",new ImageIcon(getClass().getResource("/resources/images/about.png")));
            about.addActionListener(action.About);
            add(about);
        }
    }
    private class FormatMenu extends WebMenu{
        private FormatMenu(String name, ImageIcon icon){
            setText(name);
            setIcon( icon);
            //Format menu in menu bar
            WebMenuItem setting, autoComplete;
            setting = new WebMenuItem("Setting", Hotkey.CTRL_T);
            setting.addActionListener(action.SettingChange);
            add(setting);

            autoComplete = new WebMenuItem("Auto Correct Setting", Hotkey.ALT_A);
            autoComplete.addActionListener(action.ACSettingChange);
            add(autoComplete);
        }
    }
}
