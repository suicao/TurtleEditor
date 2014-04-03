package text;


import com.alee.laf.label.WebLabel;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.text.WebEditorPane;
import org.json.simple.JSONObject;

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

/**
 * s
 * Created by khoi on 3/24/14.
 */
public class MyActionHandler {

    //1 đối tượng mới của setting
    SettingClass setting = new SettingClass();
    //1 đối tượng mới của cửa sổ editor
    TextEditor editor;
    //1 đối tượng mới của cửa sổ AutoComplete
    AutoCompleteClass ACSetting;
    //1 đối tượng mới của cửa sổ Find & Replace
    FindReplaceClass findReplace;
    //1 đối tượng Code Hint
    CodeHint codeHint;
    Font font;
    //File tạm để lưu thông tin hoạt động
    File tempDir = new File("temp");
    File tempFile = new File("temp/temp.txt");

    //Constructor
    public MyActionHandler(TextEditor editor) {
        //Nếu chưa có file temp thì tạo mới
        try {
            tempDir.mkdir();
            if (tempFile.createNewFile()) {
                System.out.println("File created");
            } else {
                System.out.print("File already exist");
            }
        } catch (IOException ioe) {
            System.out.println("Error creating new file");
        }

        this.editor = editor;
        ACSetting = new AutoCompleteClass(editor);
        findReplace = new FindReplaceClass(editor);
        codeHint = new CodeHint(editor);
    }

    Action FindReplace = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (findReplace.isVisible()) {
                findReplace.setVisible(false);
                Highlighter docHighligher = editor.editorPane.getHighlighter();
                docHighligher.removeAllHighlights();
            } else {
                findReplace.setVisible(true);
            }
        }
    };


    //Code Hint
    Action CodeHint = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            Point editorLocation = editor.getLocation();
            codeHint.hintMenuInit();
            MenuScroller.setScrollerFor(codeHint,10);
            int pos = editor.editorPane.getSelectionStart();
            codeHint.show(editor, (int) (mouseLocation.getX() - editorLocation.getX()), (int) (mouseLocation.getY() - editorLocation.getY()));
        }
    };
    //Auto Complete
    CaretListener ACCaretListener = new CaretListener() {

        public void caretUpdate(CaretEvent e) {
            findReplace.setCaret(e.getDot());
        }

    };
    Action ACSettingChange = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ACSetting.setVisible(true);
        }

    };

    DocumentListener ACDocListener = new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
            findReplace.exHighlight();
        }

        public void changedUpdate(DocumentEvent e) {

        }

        public void removeUpdate(DocumentEvent e) {
            findReplace.exHighlight();
        }

    };
    Action AutoComplete = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            ACSetting.doComplete();
        }
    };

    //Thay đổi setting, gọi phương thức change file
    Action SettingChange = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            changeFileSetting();
        }
    };
    Action New = new AbstractAction("New") {
        @Override
        public void actionPerformed(ActionEvent e) {

            editor.initEditorPane(editor.DEFAULT_FILE);
            editor.editorPane.setText("");

        }

    };

    //Lưu file
    Action Save = new AbstractAction("Save") {

        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.currentHandler;
            if (currentHandler.fileName.equals(editor.DEFAULT_FILE)) {
                saveFileAs(currentHandler,false);
            } else {
                saveFile(editor.editorPane,currentHandler);
            }
            currentHandler.changed = false;
        }
    };
    Action SaveAs = new AbstractAction("Save as") {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveFileAs(editor.currentHandler,false);
        }
    };


    //Quit program
    Action Quit = new AbstractAction("Quit") {
        public void actionPerformed(ActionEvent e) {
            quit();
        }
    };
    WindowListener ExitListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            quit();
        }
    };
    //Mở file
    Action Open = new AbstractAction("Open") {
        public void actionPerformed(ActionEvent e) {
            JFileChooser dialog = editor.dialog;
            if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                readInFile(dialog.getSelectedFile().getAbsolutePath());
            }


        }
    };

    //Undo, redo
    Action Undo = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.currentHandler;
            try {
                currentHandler.manager.undo();
            } catch (CannotUndoException cue) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Can't undo");
            }
        }
    };
    Action Redo = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            TabHandler currentHandler = editor.currentHandler;
            try {
                currentHandler.manager.redo();
            } catch (CannotRedoException cre) {
                Toolkit.getDefaultToolkit().beep();
                System.out.println("Can't redo");
            }
        }
    };

    //Thoát chương trình
    void quit(){
        for(TabHandler handler: editor.handlerList){
            saveOld(handler);
        }
        System.exit(0);
    }
    //Khi thay đổi setting, lưu file hiện tại vào file temp
    //Thiết lập setting mới cho editor
    //Đọc lại từ temp vao editor
    void changeFileSetting() {
        setting.setVisible(true);
        setting.confirmChange().addActionListener(new ActionListener() {
            @Override
            //Ghi file
            public void actionPerformed(ActionEvent e) {
                for(TabHandler handler :editor.handlerList){
                    WebEditorPane editorPane = handler.editorPane;
                    try {
                        FileWriter w = new FileWriter(tempFile, false);
                        handler.editorPane.write(w);
                        w.close();
                    } catch (IOException ioe) {
                        System.out.println("Write error");
                    }

                    //Thiết lập setting
                    String fontName = String.valueOf(setting.fontList.getSelectedValue());
                    int fontSize = Integer.parseInt(String.valueOf(setting.sizeList.getSelectedValue()));
                    String contentType = String.valueOf(setting.typeList.getSelectedValue());
                    font = new Font(fontName, Font.PLAIN, fontSize);
                    editorPane.setFont(font);
                    editorPane.setContentType(contentType);
                    //Duyệt lại từ file
                    try {
                        FileReader r = new FileReader(tempFile);
                        editorPane.read(r, null);
                        r.close();
                    } catch (IOException ioe) {
                        System.out.println("Read error");
                    }

                    editorPane.getDocument().addUndoableEditListener(editor.currentHandler.manager);
                    setting.setVisible(false);
                    JSONObject obj = editor.setting;
                    obj.remove("font");
                    obj.put("font", fontName);
                    obj.remove("font-size");
                    obj.put("font-size",fontSize);
                    obj.remove("content-type");
                    obj.put("content-type",contentType);
                    try{
                        FileWriter w = new FileWriter("data/setting.json");
                        w.write(obj.toJSONString());
                        w.flush();
                        w.close();
                    }catch (IOException ioe){
                        System.out.println("Cannot save setting");
                    }
                }

            }
        });
        setting.cancelChange().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setting.setVisible(false);
            }
        });
    }



    //Các phương thức đọc và ghi
    void readInFile(String fileName) {
        editor.initEditorPane(fileName);
        WebEditorPane editorPane = editor.editorPane;
        final TabHandler currentHandler = editor.currentHandler;
        try {
            FileReader r = new FileReader(fileName);
            if (fileName.contains(".js")) {
                editorPane.setContentType("text/javascript");
            }
            if (fileName.contains(".htm") || fileName.contains(".html") || fileName.contains(".xml")) {
                editorPane.setContentType("text/xml");
            }
            if (fileName.contains(".java") || fileName.contains(".class")) {
                editorPane.setContentType("text/java");
            }
            if (fileName.contains(".c") || fileName.contains(".cpp")) {
                editorPane.setContentType("text/c");
            }
            if (fileName.contains(".py")) {
                editorPane.setContentType("text/python");
            }

            editorPane.read(r, null);
            r.close();

            currentHandler.changed = false;
            editorPane.getDocument().addUndoableEditListener(currentHandler.manager);
            font = new Font(String.valueOf(setting.fontList.getSelectedValue()), Font.PLAIN, Integer.parseInt(String.valueOf(setting.sizeList.getSelectedValue())));
            editorPane.setFont(font);
            try {
                editorPane.getDocument().insertString(editorPane.getDocument().getLength(), " ", null);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            editorPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    currentHandler.changed = true;
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    currentHandler.changed = true;
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    currentHandler.changed = true;
                }
            });
            editor.files.addElement(fileName);

        } catch (IOException e) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(editor, "TextEditor2 can't find the file called " + fileName);
        }
    }
    void closeCurrentTab(WebTabbedPane tabPane,TabHandler handler,LinkedList<TabHandler> handlerList){
        //Xóa khỏi danh sách đang mở
        if (editor.files.indexOf(handler.fileName)!= -1){
            editor.files.remove(editor.files.indexOf(handler.fileName));
        }
        //Lấy index của tab bị đóng bằng handler
        int index = handlerList.indexOf(handler);
        //Lưu lại index của tab đang mở
        int oldIndex = tabPane.getSelectedIndex();
        //Nếu đã thay đổi thì lưu trước khi đóng
        if(handler.changed){
            tabPane.setSelectedIndex(index);
            saveOld(handler);
        }
        //Xóa tab và handle tương ứng
        try {
            handlerList.remove(index);
            tabPane.remove(index);
        } catch (IndexOutOfBoundsException aibe) {
            System.out.print("No tab to close");
        }
        //Đặt lại tab đang mở
        if(oldIndex > index){
            tabPane.setSelectedIndex(--oldIndex);
        }
        else{
            try{
                tabPane.setSelectedIndex(oldIndex);
            }catch (IndexOutOfBoundsException iobe){
                System.out.print("No tab");
            }
        }
    }
    //Các phương thức lưu file
    void saveFileAs(TabHandler currentHandler, boolean saveBeforeClose) {
        JPanel pnlTab = currentHandler.pnlTab;
        JFileChooser dialog = editor.dialog;
        DefaultListModel files = editor.files;
        if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (files.indexOf(currentHandler.fileName)!= -1){
                files.remove(files.indexOf(currentHandler.fileName));
            }
            currentHandler.fileName = dialog.getSelectedFile().getAbsolutePath();
            saveFile(editor.editorPane,currentHandler);

            if(!saveBeforeClose){
                files.addElement(currentHandler.fileName);
                String fileName = currentHandler.fileName;
                String displayName;
                int i = fileName.lastIndexOf('\\');
                displayName = fileName.substring(i + 1, fileName.length());

                pnlTab.removeAll();
                currentHandler.tabLabel = new WebLabel("<html><body leftmtrrgin=5 topmargin=5 marginwidth=15 marginheight=2>" + displayName + "</body></html>");
                pnlTab.add(currentHandler.tabLabel);
                pnlTab.add(currentHandler.closeBtn);
                pnlTab.revalidate();
                pnlTab.repaint();
            }
        }
    }

    void saveOld(TabHandler currentHandler) {
        if (currentHandler.changed) {
            if (JOptionPane.showConfirmDialog(editor, "Would you like to save " + currentHandler.fileName + " ?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                if(currentHandler.fileName.equals(editor.DEFAULT_FILE)){
                    saveFileAs(currentHandler,true);
                }
                else{
                    saveFile(editor.editorPane,currentHandler);
                }
            }
        }
    }
    void saveFile(WebEditorPane editorPane, TabHandler currentHandler) {
        try {
            FileWriter w = new FileWriter(currentHandler.fileName);
            editorPane.write(w);
            w.close();
            currentHandler.changed = false;
            System.out.println(currentHandler.fileName + " " + currentHandler.changed);
        } catch (IOException e) {
        }
    }

}
