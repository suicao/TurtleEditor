/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 *TextEditor
 *
 *49 AutoCompleteClass autoComplete=new AutoCompleteClass(this);
 *71 editorPane.addKeyListener(autoComplete.ACKeyListener);
 editorPane.getDocument().addDocumentListener(autoComplete.ACDocListener);
 *153 WebButton ACSetting = new WebButton();     
 *239 ACSetting.setIcon(new ImageIcon("images/stock.png"));
 *
 *MyActionHandler
 *153 editor.editorPane.getDocument().addDocumentListener(editor.autoComplete.ACDocListener);
 editor.editorPane.addKeyListener(editor.autoComplete.ACKeyListener);
 editor.autoComplete.changeType(editor.editorPane.getContentType());
 *211 editor.editorPane.getDocument().addDocumentListener(editor.autoComplete.ACDocListener);
 */
package text;

import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Dung 30/3/2014
 */
public class AutoCompleteClass extends JFrame {

    //list 1 chứa các từ khóa gõ nhanh theo kiểu gõ tắt 1 từ rồi thay thế nó bằng cụm khác
    //được định nghĩa trong cửa sổ Auto Complete Setting
    private int keyMaxLen1 = 0;
    private JSONObject listACItem1 = new JSONObject();
    //list 2 chứa các từ khóa gõ nhanh theo kiểu gõ 1 từ rồi thêm 1 từ đi kèm vs nó
    //được định nghĩa trong các file .dat bên ngoài chương trình
    private int keyMaxLen2 = 0;
    private JSONObject listACItem2 = new JSONObject();
    //file chứa dữ liệu của kiểu gõ nhanh 1
    private File file1;
    //file chứa dữ liệu của kiểu gõ nhanh 2
    private File file2;

    //editor
    private TextEditor editor;
    //bảng chứa thông tin listACItem1
    private JTable myTable=new JTable();
    //mảng chứa các content type là kiểu của document
    private String[] types = new String[]{"text/xml", "text/java", "text/c"};
    //mảng tương ứng chứa các file dữ liệu tương ứng vs từng kiểu document
    private String[] fileOfType = new String[]{"ACDataXML.json", "ACDataJava.json", "ACDataC.json"};

    AutoCompleteClass(TextEditor e) {
        editor = e;

        //thiết lập mặc định là text/xml
        file1 = new File("data/ACData.json");
        file2 = new File("data/ACDataXML.json");
        //đọc file
        listACItem1 = loadACItemList(file1);
        keyMaxLen1 = keyMaxLen(listACItem1);
        listACItem2 = loadACItemList(file2);
        keyMaxLen2 = keyMaxLen(listACItem2);

        //tạo cửa sổ setting
        JPanel content = new JPanel();

        //tạo bảng
        initTableModel();

        WebScrollPane scroll = new WebScrollPane(myTable);

        //tao nut
        WebButton btnOK = new WebButton("OK");
        WebButton btnCancel = new WebButton("Cancel");
        WebButtonGroup btnGroup = new WebButtonGroup(true, btnOK, btnCancel);

        WebButton btnAdd = new WebButton("Add");
        final WebButton btnRemove = new WebButton("Remove");
        btnRemove.setEnabled(false);
        WebButton btnRemoveAll = new WebButton("Remove All");
        WebButtonGroup btnRemoveGroup = new WebButtonGroup(true, btnAdd, btnRemove, btnRemoveAll);

        JPanel botPanel = new JPanel();
        botPanel.setLayout(new BorderLayout());

        botPanel.add(btnRemoveGroup, BorderLayout.CENTER);
        botPanel.add(btnGroup, BorderLayout.EAST);

        //add vao contentPane
        content.setLayout(new BorderLayout());
        content.add(scroll, BorderLayout.CENTER);
        content.add(botPanel, BorderLayout.SOUTH);

        ListSelectionModel selectionModel = myTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btnRemove.setEnabled(true);
            }

        });

        btnAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object[] newRow = {myTable.getRowCount() + 1, "", ""};
                DefaultTableModel model = (DefaultTableModel) myTable.getModel();
                model.addRow(newRow);
            }

        });

        btnRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int[] selectRows = myTable.getSelectedRows();
                DefaultTableModel model = (DefaultTableModel) myTable.getModel();
                for (int i : selectRows) {
                    model.removeRow(i);
                }
                model.fireTableDataChanged();
                btnRemove.setEnabled(false);
            }

        });

        btnRemoveAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) myTable.getModel();
                model.getDataVector().removeAllElements();
                model.fireTableDataChanged();

                btnRemove.setEnabled(false);
            }

        });

        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                //cap nhat du lieu
                DefaultTableModel model = (DefaultTableModel) myTable.getModel();
                if (listACItem1 == null) {
                    listACItem1 = new JSONObject();
                } else {
                    listACItem1.clear();
                }
                int n = myTable.getRowCount();
                for (int i = 0; i < n; i++) {
                    listACItem1.put(myTable.getValueAt(i, 1), myTable.getValueAt(i, 2));
                }
                //luu ra file
                saveACItemList(listACItem1, file1);
                keyMaxLen1 = keyMaxLen(listACItem1);

            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        setSize(new Dimension(650, 400));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2,
                (screenSize.height - getHeight()) / 2);
        //setResizable(false);
        setContentPane(content);

    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            initTableModel();
        }
    }

    private void initTableModel() {
        String[] columns = {"STT", "Key", "Value"};
        Object[][] data = null;
        if (listACItem1 != null) {
            int size = listACItem1.size();
            data = new Object[size][3];

            Set<String> keys = listACItem1.keySet();
            int count = 0;
            for (String key : keys) {
                data[count] = new Object[]{count + 1, key, listACItem1.get(key)};
                count++;
            }

        }
        DefaultTableModel model = new DefaultTableModel(data, columns);
        myTable.setModel(model);
        myTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        myTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        myTable.getColumnModel().getColumn(2).setPreferredWidth(360);
        myTable.setRowHeight(25);

    }



    /*
     * hàm chính thực hiện auto complete
     * xét 2 trường hợp ứng vs 2 kiểu gõ tắt tương ứng dữ liệu trong listACItem1 vs  ListACItem2
     */
    public void doComplete() {

        Document doc = editor.editorPane.getDocument();
        //lấy vị trí con trỏ
        int offset=editor.editorPane.getCaretPosition();
        //kiểm tra gõ tắt kiểu 1
        try {
            //sẽ lấy từ document 1 xâu có độ dài bias tính từ offset sang trái
            //so sánh vs dữ liệu, nếu trùng thì tiến hành sửa
            //bias = min( độ dài hiện có của document và độ dài lớn nhất của tập các từ khóa trong dữ liệu)
            int bias = (keyMaxLen1 < doc.getLength()) ? keyMaxLen1 : doc.getLength();
            //láy ra string
            String str = doc.getText(offset - bias, bias);
            Set<String> keys = listACItem1.keySet();
            for (String key : keys) {
                if (isMatch(str, key)) {
                    int len = key.length();
                    doc.remove(offset - len, len);
                    doc.insertString(offset-len, (String) listACItem1.get(key), null);

                    return; //nếu tìm thấy thì thoát khỏi hàm, không cần kiểm tra điều kiện 2
                }
            }

        } catch (Exception e) {
            System.out.println("wtf");
        }

        //kiểm tra gõ tắt kiểu 2
        try {
            //cách lấy string tương tự trường hợp 1
            int bias = (keyMaxLen2 < doc.getLength()) ? keyMaxLen2 : doc.getLength();
            String str = doc.getText(offset - bias, bias);

            Set<String> keys = listACItem2.keySet();
            for (String key : keys) {
                if (key.equals(str)) {
                    doc.insertString(offset, (String) listACItem2.get(key), null);
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("wtf");
        }

    }

    /*
     * hàm trả đọc file dữ liệu vào list
     * trả về độ dài lớn nhất của các từ khóa, phục vụ cho hàm doComplete()
     * tham số list: là listACItem1 hoặc listACItem2
     * file là file1 hoặc file2
     */
    private JSONObject loadACItemList(File file) {
        JSONObject list;

        try {
            JSONParser parser = new JSONParser();
            FileReader fr = new FileReader(file);
            list = (JSONObject) parser.parse(fr);
            fr.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }

        return list;

    }

    private int keyMaxLen(JSONObject list) {
        if (list != null) {
            int len = 0;
            int maxLen = 0;
            Set<String> keys = list.keySet();
            for (String key : keys) {
                len = key.length();
                if (maxLen < len) {
                    maxLen = len;
                }
            }

            return maxLen;
        } else {
            return 0;
        }
    }
    /*
     * hàm ghi listACItem1 sau khi thay đổi trong bảng Auto Complete Setting
     */

    private void saveACItemList(JSONObject list, File file) {
        try {
            PrintWriter wrt = new PrintWriter(file);
            list.writeJSONString(wrt);
            //wrt.flush();
            wrt.close();
        } catch (Exception ex) {
            System.out.println("Loi ghi file "+ex.toString());
        }
    }

    /*
     * hàm thay đổi kiểu document ( thay đổi listACItem2 ) 
     * được gọi bởi MyActionHandler->changeFileSetting
     * type chứa xâu thể hiện kiểu của document
     */
    public void changeType(String type) {
        //tim ten type
        int n = types.length;
        int i = 0;
        for (i = 0; i < n; i++) {
            if (type.equals(types[i])) {
                break;
            }
        }
        listACItem2.clear();
        if (i < n) {
            file2 = new File(fileOfType[i]);
            listACItem2 = loadACItemList(file2);
            keyMaxLen2 = keyMaxLen(listACItem2);
        }

    }
    private boolean isMatch(String str1, String str2) {
        int n1 = str1.length();

        int n2 = str2.length();
        if (n1 < n2) {
            return false;
        }

        int i = n2 - 1;
        int j = n1 - 1;
        while (i >= 0) {
            if (str1.charAt(j--) != str2.charAt(i--)) {
                return false;
            }
        }
        return true;

    }
}
