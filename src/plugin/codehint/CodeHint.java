package plugin.codehint;

import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.managers.hotkey.Hotkey;
import gui.MainEditor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import plugin.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by khoi on 4/6/2014.
 */
public class CodeHint extends WebPopupMenu implements Pluggable,ActionListener {
    MainEditor editor;
    JSONObject hintData;
    JSONParser parser;
    JSONArray keywords, classes;
    WebMenuBar menu;
    WebMenu hinter;
    //Constructor
    public CodeHint(MainEditor editor) {
        this.editor = editor;
        menu = editor.getMenu();
        load();
    }

    @Override
    public void load() {
        //Tạo menu cho plugin trên thanh menu
        hinter = new WebMenu("Hinter", new ImageIcon(getClass().getResource("/resources/images/hinter.png")));
        menu.add(hinter);
        //Tạo menu con
        WebMenuItem trigger = new WebMenuItem("Code Hint", Hotkey.ALT_I);
        hinter.add(trigger);
        parser = new JSONParser();
        try {
            hintData = (JSONObject) parser.parse(new FileReader("data/keyword.json"));

            keywords = (JSONArray) hintData.get("keywords");
            classes = (JSONArray) hintData.get("classes");
        } catch (IOException ioe) {
            System.out.print("Key word file not found");
        } catch (ParseException pe) {
        }
        //Action Listener để hiện từ khóa mỗi khi click hoặc nhấn tổ hợp phím tắt
        //Action Listener gọi phương thức run
        trigger.addActionListener(this);
    }

    @Override
    public void unload() {
        menu.remove(hinter);
    }

    /**Phương thức hiện tìm word, duyệt các từ khóa thỏa mãn và in ra
     * cho người dùng lựa chọn*/

     @Override
    public void run() {
        removeAll();
        final JEditorPane editorPane = editor.getCurrentEditor();
        final int caretPosition = editorPane.getCaretPosition();
        final Document doc = editorPane.getDocument();
        String word = "";
        int j = 1;
        while (true) {
            try {
                word = doc.getText(caretPosition - j, j);
            } catch (BadLocationException ble) {
            }
            try {
                if (Character.isWhitespace(word.charAt(0)) || containsSpecialChar(word)) {
                    //Xóa kí tự ở đầu
                    word = word.substring(1);
                    j--;
                    break;
                } else if (j == caretPosition) {
                    break;
                }
            } catch (IndexOutOfBoundsException iobe) {
                j--;
                break;
            }
            j++;
        }

        //Xác định offset
        final int offset = j;
        Iterator<?> key = keywords.iterator();
        if (!word.isEmpty()) {
            int firstChar = word.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                key = classes.iterator();
            }
            while (key.hasNext()) {
                String kw = String.valueOf(key.next());
                final WebMenuItem temp = new WebMenuItem(kw);

                //Nếu keywords chứa word mới thêm vào menu
                if (isValidHint(kw, word)) {
                    if (kw.length() > word.length()) {
                        temp.setMnemonic(kw.charAt(word.length()));
                    }
                    add(temp);
                    //thay thế word bằng keywords
                    temp.addActionListener(e -> {
                        try {
                            doc.remove(caretPosition - offset, offset);
                            doc.insertString(editorPane.getCaretPosition(), temp.getText(), null);

                        } catch (BadLocationException ble) {
                        }
                    });
                }

            }

        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        Point editorLocation = editor.getLocation();
        run();
        MenuScroller.setScrollerFor(this, 10);
        this.show(editor, (int) (mouseLocation.getX() - editorLocation.getX()), (int) (mouseLocation.getY() - editorLocation.getY()));
    }
    //Some helper functions
    boolean isValidHint(String key, String word) {
        int minLength = key.length() < word.length() ? key.length() : word.length();
        for (int i = 0; i < minLength; i++) {
            if (key.charAt(i) != word.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    boolean containsSpecialChar(String word) {
        return word.contains("(") || word.contains("}") || word.contains(".")
                || word.contains(";") || word.contains(",");
    }
}


