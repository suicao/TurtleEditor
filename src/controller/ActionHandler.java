package controller;

/**
 * Created by khoi on 4/5/2014.
 */

import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.tabbedpane.WebTabbedPane;
import gui.*;
import gui.SettingDialog;
import org.json.simple.JSONObject;
import gui.AutoCompleteClass;
import org.json.simple.JSONValue;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Highlighter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionHandler {
    SettingDialog settingDialog;
    MainEditor editor;
    //1 đối tượng mới của cửa sổ AutoComplete
    AutoCompleteClass ACSetting;
    //1 đối tượng mới của cửa sổ Find & Replace
    public FindReplaceClass findReplace;
    //File tạm để lưu thông tin hoạt động
    File tempDir = new File("temp");
    File tempFile = new File("temp/temp.turtle");
    //Getters



    //Constructor
    public ActionHandler(MainEditor editor){
        settingDialog = new SettingDialog();
        ACSetting = new AutoCompleteClass(editor);
        findReplace = new FindReplaceClass(editor);
//        codeHint = new CodeHint(editor);
        this.editor = editor;
    }



    public Action FindReplace = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (findReplace.isVisible()) {
                findReplace.setVisible(false);
                Highlighter docHighligher = editor.getCurrentEditor().getHighlighter();
                docHighligher.removeAllHighlights();
            } else {
                findReplace.setVisible(true);
            }
        }
    };

    //Auto Complete
    public CaretListener ACCaretListener = new CaretListener() {

        public void caretUpdate(CaretEvent e) {
            findReplace.setCaret(e.getDot());
        }

    };
    public Action ACSettingChange = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ACSetting.setVisible(true);
        }

    };

    public DocumentListener ACDocListener = new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
            findReplace.exHighlight();
        }

        public void changedUpdate(DocumentEvent e) {

        }

        public void removeUpdate(DocumentEvent e) {
            findReplace.exHighlight();
        }

    };
    public Action AutoComplete = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ACSetting.doComplete();
        }
    };

    //Thay đổi settingDialog, gọi phương thức change file
    public Action SettingChange = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            changeFileSetting();
        }
    };
    public Action New = new AbstractAction("New") {
        @Override
        public void actionPerformed(ActionEvent e) {

            editor.initEditorPane(editor.DEFAULT_FILE);
            editor.getCurrentEditor().setText("");

        }

    };
    //Lưu file
    public Action Save = new AbstractAction("Save") {

        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.getCurrentHandler();
            if (currentHandler.getFileName().equals(editor.DEFAULT_FILE)) {
                saveFileAs(currentHandler, false);
            } else {
                saveFile(editor.getCurrentEditor(), currentHandler);
            }
            currentHandler.setChanged(false);
        }
    };
    public Action SaveAs = new AbstractAction("Save as") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveFileAs(editor.getCurrentHandler(), false);
        }
    };


    //Quit program
    public Action Quit = new AbstractAction("Quit") {
        public void actionPerformed(ActionEvent e) {
            quit(editor.getHandlerList());
        }
    };
    public WindowListener ExitListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            quit(editor.getHandlerList());
        }
    };
    //Mở file
    public Action Open = new AbstractAction("Open") {
        public void actionPerformed(ActionEvent e) {
            JFileChooser dialog = new WebFileChooser("user.dir");
            if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                readInFile(dialog.getSelectedFile().getAbsolutePath());
            }
        }
    };

    //Undo, redo
    public Action Undo = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.getCurrentHandler();
            try {
                currentHandler.getUndoManager().undo();
            } catch (CannotUndoException cue) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Can't undo");
            }
        }
    };
    public Action Redo = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.getCurrentHandler();
            try {
                currentHandler.getUndoManager().redo();
            } catch (CannotRedoException cre) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Can't redo");
            }
        }
    };
    //Gioi thieu
    public Action About = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            try {
                showAboutUs();
            } catch (IOException ex) {
                Logger.getLogger(ActionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

    //Thoát chương trình
    void quit(LinkedList<TabHandler> handerList) {
        for (TabHandler handler : handerList) {
            if(!(saveOld(handler))){
                return;
            }
        }
        System.exit(0);
    }
    //Khi thay đổi settingDialog, lưu file hiện tại vào file temp
    //Thiết lập settingDialog mới cho editor
    //Đọc lại từ temp vao editor
    void changeFileSetting() {
        settingDialog.setVisible(true);
        settingDialog.getOK().addActionListener(new ActionListener() {
            @Override
            //Ghi file
            public void actionPerformed(ActionEvent e) {
                LinkedList<TabHandler> handlerList = editor.getHandlerList();
                //Thiết lập settingDialog
                String fontName = String.valueOf(settingDialog.getFontList().getSelectedValue());
                int fontSize = Integer.parseInt(String.valueOf(settingDialog.getSizeList().getSelectedValue()));
                String contentType = String.valueOf(settingDialog.getTypeList().getSelectedValue());
                JSONObject newSetting = (JSONObject)JSONValue.parse(editor.getSetting());
                newSetting.remove("font");
                newSetting.put("font", fontName);
                newSetting.remove("font-size");
                newSetting.put("font-size", fontSize);
                newSetting.remove("content-type");
                newSetting.put("content-type", contentType);
                try {
                    FileWriter w = new FileWriter("data/setting.json");
                    w.write(newSetting.toJSONString());
                    w.flush();
                    w.close();
                } catch (IOException ioe) {
                    System.out.println("Cannot save setting");
                }

                for (TabHandler handler : handlerList) {
                    JEditorPane editorPane = handler.editorPane;
                    if(handler == editor.getCurrentHandler()){
                        try {
                            FileWriter w = new FileWriter(tempFile, false);
                            editorPane.write(w);
                            w.close();
                        } catch (IOException ioe) {
                            System.out.println("Write error");
                        }
                        editorPane.setContentType(contentType);
                        //Duyệt lại từ file
                        try {
                            FileReader r = new FileReader(tempFile);
                            editorPane.read(r, null);
                            r.close();
                        } catch (IOException ioe) {
                            System.out.println("Read error");
                        }
                    }
                    editor.setEditorSetting(newSetting, editorPane);
                    editorPane.getDocument().addUndoableEditListener(editor.getCurrentHandler().getUndoManager());
                    editorPane.getDocument().addDocumentListener(ACDocListener);
                    ACSetting.changeType((String) settingDialog.getTypeList().getSelectedValue());
                    settingDialog.setVisible(false);
                }
            }
        });
        settingDialog.getCancel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settingDialog.setVisible(false);
            }
        });
    }


    //Các phương thức đọc và ghi
    public void readInFile(String fileName) {
        editor.initEditorPane(fileName);
        JEditorPane editorPane = editor.getCurrentEditor();
        final TabHandler currentHandler = editor.getCurrentHandler();
        try {
            String type = "";
            FileReader r = new FileReader(fileName);
            if (fileName.contains(".js")||fileName.contains(".json")) {
                type = "text/javascript";
            }
            else if (fileName.contains(".htm") || fileName.contains(".html") || fileName.contains(".xml")) {
                type = "text/xml";
            }

            else if (fileName.contains(".c") || fileName.contains(".cpp")) {
                type = "text/c";
            }
            else if (fileName.contains(".py")) {
                type = "text/python";
            }
            else {
                type = "text/java";
            }
            editorPane.setContentType(type);
            editor.setEditorSetting((JSONObject)JSONValue.parse(editor.getSetting()),editorPane);
            editorPane.read(r, null);
            r.close();

            currentHandler.setChanged(false);
            editorPane.getDocument().addUndoableEditListener(currentHandler.getUndoManager());
            try {
                editorPane.getDocument().insertString(editorPane.getDocument().getLength(), " ", null);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            editorPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    currentHandler.setChanged(true);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    currentHandler.setChanged(true);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    currentHandler.setChanged(true);
                }
            });
            editor.getFileList().addElement(fileName);
            editorPane.getDocument().addDocumentListener(ACDocListener);
            ACSetting.changeType(type);

        } catch (IOException e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(editor, "TextEditor2 can't find the file called " + fileName);
        }
    }
    public void closeAll(WebTabbedPane tabPane,LinkedList<TabHandler> handlerList){
        try {
            for(TabHandler handler : handlerList){
                int index = handlerList.indexOf(handler);
                tabPane.setSelectedIndex(index);
                if(!saveOld(handler)) {
                    return;
                }
            }
        } catch (NoSuchElementException nsee) {
            System.out.printf("\nNo tab to close");
        }
        while (handlerList.size()>0){
            handlerList.removeFirst();
            tabPane.remove(0);
        }
        editor.getFileList().removeAllElements();
    }
    public void closeCurrentTab(WebTabbedPane tabPane, TabHandler handler, LinkedList<TabHandler> handlerList) {
        //Xóa khỏi danh sách đang mở
        if (editor.getFileList().indexOf(handler.getFileName()) != -1) {
            editor.getFileList().remove(editor.getFileList().indexOf(handler.getFileName()));
        }
        //Lấy index của tab bị đóng bằng handler
        int index = handlerList.indexOf(handler);
        //Lưu lại index của tab đang mở
        int oldIndex = tabPane.getSelectedIndex();
        //Nếu đã thay đổi thì lưu trước khi đóng
        boolean canceled=false;
        if (handler.isChanged()) {
            tabPane.setSelectedIndex(index);
            canceled = !(saveOld(handler));
        }
        //Nếu không chọn cancel
        if(!canceled){
            //Xóa tab và handle tương ứng
            try {
                handlerList.remove(index);
                tabPane.remove(index);
            } catch (IndexOutOfBoundsException aibe) {
                System.out.printf("\nNo tab to close from MAH");
            }
        }
        //Đặt lại tab đang mở
        if (oldIndex > index) {
            tabPane.setSelectedIndex(--oldIndex);
        } else {
            try {
                tabPane.setSelectedIndex(oldIndex);
            } catch (IndexOutOfBoundsException iobe) {
                System.out.print("No tab");
            }
        }

    }

    //Các phương thức lưu file
    void saveFileAs(TabHandler currentHandler, boolean saveBeforeClose) {
        JFileChooser dialog = new WebFileChooser("user.dir");
        DefaultListModel files = editor.getFileList();
        if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (files.indexOf(currentHandler.getFileName()) != -1) {
                files.remove(files.indexOf(currentHandler.getFileName()));
            }
            currentHandler.setFileName(dialog.getSelectedFile().getAbsolutePath());
            saveFile(editor.getCurrentEditor(), currentHandler);

            if (!saveBeforeClose) {
                files.addElement(currentHandler.getFileName());
                String fileName = currentHandler.getFileName();
                String displayName;
                int i = fileName.lastIndexOf('\\');
                displayName = fileName.substring(i + 1, fileName.length());
                currentHandler.setTabLabel("<html><body leftmtrrgin=5 topmargin=5 marginwidth=15 marginheight=2>" + displayName + "</body></html>");
            }
        }
    }

    //Trả về true nếu chọn yes/no, trả về false nếu chọn cancel
    boolean saveOld(TabHandler currentHandler) {
        if (currentHandler.isChanged()) {
            int option = JOptionPane.showConfirmDialog(editor, "Would you like to save " + currentHandler.getFileName() + " ?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                if (currentHandler.getFileName().equals(editor.DEFAULT_FILE)) {
                    saveFileAs(currentHandler, true);
                } else {
                    saveFile(editor.getCurrentEditor(), currentHandler);
                }
            }
            else if(option == JOptionPane.CANCEL_OPTION){
                return false;
            }
        }
        return true;
    }

    void saveFile(JEditorPane editorPane, TabHandler currentHandler) {
        try {
            FileWriter w = new FileWriter(currentHandler.getFileName());
            editorPane.write(w);
            w.close();
            currentHandler.setChanged(false);
        } catch (IOException e) {
            System.out.printf("\nCannot save file");
        }
    }
    void showAboutUs() throws IOException {
        JEditorPane aboutPane = new JEditorPane();
        java.net.URL helpURL = ActionHandler.class.getResource("/resources/help/turtle.html");
        if (helpURL != null) {
            try {
                aboutPane.setPage(helpURL);
            } catch (IOException e) {
                System.err.println("Attempted to read a bad URL: " + helpURL);
            }
        } else {
            System.err.println("Couldn't find file: Turtle.html");
        }
        aboutPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(aboutPane);

        scrollPane.setPreferredSize(new Dimension(500, 500));

        final ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/home.png"));
        JOptionPane.showMessageDialog(editor, scrollPane, "Turtle Editor Help And Support", JOptionPane.INFORMATION_MESSAGE, icon);

    }
}
