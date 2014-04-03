package text;

import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.Highlight;

/**
 *
 * @author Dung 30/3/2014
 */
public class FindReplaceClass extends WebPanel {

    //editor Đối tượng cửa sổ chương trình
    private TextEditor editor;

    //highlight color Painter để highlight các từ sau khi tìm kiếm
    //normalPainter highlight các từ khóa tìm được
    //selectedPainter highlight 1 từ hiện tại con trỏ đang trỏ tới
    private DefaultHighlightPainter normalPainter = new DefaultHighlightPainter(Color.green);
    private DefaultHighlightPainter selectedPainter = new DefaultHighlightPainter(Color.red);

    //chứa vị trí con trỏ chuột trong document
    //phục vụ việc xử lý highlight các từ
    private int caret = 0;

    //2 textfield chứa từ tìm kiếm và từ thay thế
    private JTextField findText = new JTextField();
    private JTextField replaceText = new JTextField();
    private boolean isIgnoreCase = false;
    /*
     * @param e : editor của chương trình chính
     * hàm tạo
     * khởi tạo panel FindReplaceClass chứa findText, replaceText, các nút button, gán sự kiện cho button
     */
    private GridBagConstraints gbc;
    private GridBagLayout gbl;
    public FindReplaceClass(TextEditor e) {
        //gán đối tượng editor
        editor = e;

        //khoi tao findPane
        JLabel lb1 = new JLabel("<html><body leftmtrrgin=5 topmargin=5 marginwidth=5 marginheight=2>" + "Find" + "</body></html>");

        JButton btnFindNext = new JButton(new ImageIcon(getClass().getResource("/images/next.png")));
        TooltipManager.setTooltip(btnFindNext, "Find Next", TooltipWay.up, 0);
        JButton btnFindPrev = new JButton(new ImageIcon(getClass().getResource("/images/prev.png")));
        TooltipManager.setTooltip(btnFindPrev, "Find Previous", TooltipWay.up, 0);

        //khoi tao replacePane
        JLabel lb2 = new JLabel("<html><body leftmtrrgin=5 topmargin=5 marginwidth=8 marginheight=2>" + "Replace" + "</body></html>");

        JButton btnReplace = new JButton(new ImageIcon(getClass().getResource("/images/replace.png")));
        TooltipManager.setTooltip(btnReplace, "Replace", TooltipWay.up, 0);

        JButton btnReplaceAll = new JButton(new ImageIcon(getClass().getResource("/images/replace-all.png")));
        TooltipManager.setTooltip(btnReplaceAll, "Replace All", TooltipWay.up, 0);

        replaceText.setColumns(16);
        findText.setColumns(16);

        //Khởi tạo layout
        gbl = new GridBagLayout();
        setLayout(gbl);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lb1, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        add(findText,gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        add(new WebButtonGroup(btnFindNext,btnFindPrev),gbc);


        gbc.gridx = 3;
        gbc.gridy = 1;
        add(lb2,gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        add(replaceText,gbc);

        gbc.gridx = 5;
        gbc.gridy = 1;
        add(new WebButtonGroup(btnReplace,btnReplaceAll),gbc);
        gbc.gridx = 6;
        gbc.gridy = 1;
        //các tùy chọn
        WebToggleButton ignoreCase = new WebToggleButton(new ImageIcon(getClass().getResource("/images/case.png")));
        ignoreCase.setSelected(false);
        TooltipManager.setTooltip(ignoreCase, "Ignore case toggle", TooltipWay.up, 0);
        add(ignoreCase,gbc);
        setUndecorated(false);
        setDrawSides ( true, true, true, true );
        //thêm sự kiện
        btnFindNext.addActionListener(Next);
        btnFindPrev.addActionListener(Previous);
        btnReplace.addActionListener(Replace);
        btnReplaceAll.addActionListener(ReplaceAll);

        ignoreCase.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(isIgnoreCase){
                    isIgnoreCase=false;
                }else{
                    isIgnoreCase=true;
                }
            }

        });

        findText.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    findText.setBackground(Color.white);
                    //tìm kiếm các từ và highlight các từ đó
                    find();
                    //trỏ tới từ tiếp theo
                    moveHighlight(1);
                }
            }
        });

        replaceText.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    //thay thế từ tại con trỏ hiện thời, nếu xảy ra lỗi thì bỏ qua
                    if (replace()) {
                        //trỏ tới từ kế tiếp
                        moveHighlight(1);
                    }
                }
            }
        });
    }

    /*
     * sự kiện mỗi khi click vào các nút lệnh trên panel
     * các nút có tên, phân loại sự kiện theo tên của nút được click
     */
    Action Next = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            find();
            //trỏ tới từ tiếp theo
            moveHighlight(1);
        }
    };
    Action Previous = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            moveHighlight(-1);
        }
    };
    Action Replace = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(replace()){
                moveHighlight(1);
            }
        }
    };
    Action ReplaceAll = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            find();
            replaceAll();
        }
    };

    /*
     * hàm thay thế từ trỏ bởi con trỏ hiện tại
     * trả về false nếu lỗi
     */
    private boolean replace() {

        String strToReplace = replaceText.getText();
        //kiểm tra xâu thay thế
        if (strToReplace.equals("")) {
            return false;
        }

        Document doc = editor.editorPane.getDocument();
        Highlighter docHighlighter = editor.editorPane.getHighlighter();

        //mảng chứa các từ được highlight trong document
        //đây cũng là mảng chứa thông tin vị trí các từ trong document được tìm kiếm
        Highlight[] hili = docHighlighter.getHighlights();
        //lấyy phần tử cuối cũng chính là phần tử được trỏ bởi con trỏ hiện thời
        int n = hili.length - 1;

        try {
            //xóa nó khỏi document
            doc.remove(hili[n].getStartOffset(), hili[n].getEndOffset() - hili[n].getStartOffset());
            //thay thế
            doc.insertString(hili[n].getStartOffset(), strToReplace, null);
            //xóa nó khỏi mảng highlight
            docHighlighter.removeHighlight(hili[n]);

        } catch (Exception ex) {
            System.out.println(ex.toString());
            //xảy ra lỗi thì trả về false
            return false;
        }

        //nếu không có lỗi thì trả về true
        return true;
    }

    /*
     * hàm thay thế toàn bộ các từ được tìm thấy
     */
    private void replaceAll() {
        String strToReplace = replaceText.getText();
        if (strToReplace.equals("")) {
            //nếu là xâu rỗng thì thoát khỏi hàm
            return;
        }

        Document doc = editor.editorPane.getDocument();
        Highlighter docHighlighter;
        docHighlighter = editor.editorPane.getHighlighter();

        Highlight[] hili = docHighlighter.getHighlights();

        try {
            //duyệt toàn bộ mảng highlight cũng chính là mảng chứa thông tin các từ tìm thấy
            //thay thế các từ đó rồi xóa khỏi mảng
            for (Highlight h : hili) {
                doc.remove(h.getStartOffset(), h.getEndOffset() - h.getStartOffset());
                doc.insertString(h.getStartOffset(), strToReplace, null);
            }
            docHighlighter.removeAllHighlights();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /*
     * hàm tìm kiếm đưa ra mảng chứa thông tin vị trí tất cả các từ tìm được
     * trả về số từ tìm được
     */
    private int find() {
        String strToFind = findText.getText();
        //count là số từ tìm được
        int count = 0;
        if (strToFind.equals("")) {
            //bôi màu findText màu đỏ để thể hiện lỗi
            findText.setBackground(Color.red);
            System.out.println("xâu tìm kiếm rỗng");
            return 0;
        }


        try {
            Document doc = editor.editorPane.getDocument();
            Highlighter docHighlighter = editor.editorPane.getHighlighter();
            docHighlighter.removeAllHighlights();

            String text = doc.getText(0, doc.getLength());
            //ignore case hay không
            if(isIgnoreCase){
                text=text.toUpperCase();
                strToFind=strToFind.toUpperCase();
            }
            //tim tu khoa
            int i = 0;
            while (i >= 0) {
                int length = strToFind.length();
                i = text.indexOf(strToFind, i);
                System.out.println("tim " + i);
                if (i >= 0) {
                    docHighlighter.addHighlight(i, i + length, normalPainter);
                    i++;
                    count++;
                }

            }
            //docHighlighter.addHighlight(0, 1, new DefaultHighlightPainter(Color.white));
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

        if (count == 0) {
            //bôi màu findText màu đỏ để thể hiện lỗi
            findText.setBackground(Color.red);
        }

        return count;
    }

    /*
     * hàm di chuyển con trỏ highlight dọc theo dãy từ đã tìm được
     * vec = -1 hoặc 1 chỉ hướng di chuyển
     * caret thay đổi bởi MyActionHandler->ACCaretListener gán cho editor
     */
    private void moveHighlight(int vec) {
        Document doc = editor.editorPane.getDocument();
        Highlighter docHighlighter;
        docHighlighter = editor.editorPane.getHighlighter();

        Highlight[] hili = docHighlighter.getHighlights();

        if (hili.length == 0) {
            return;
        }

        try {
            //xoa phan tu con tro hien tai tro toi
            Highlight current=hili[hili.length-1];
            docHighlighter.addHighlight(current.getStartOffset(), current.getEndOffset(), normalPainter);
            docHighlighter.removeHighlight(current);

            Highlight htemp = null;
            //d là khoảng cách đại số giữa caret đến vị trí từ đang xét
            //phải tìm từ có khoảng cách đến caret min và dương( sau khi nhân vs vec)
            int d = 1000000000;
            hili = docHighlighter.getHighlights();
            for (Highlight h:hili) {
                int t = (h.getStartOffset() - caret) * vec;
                boolean isType=(h.getPainter().equals(normalPainter));
                if (isType &&(t >= 0) && (d > t)) {
                    htemp = h;
                    d = t;
                }

            }

            //nếu không tìm thấy reset lại caret
            if (htemp == null) {
                if (vec > 0) {
                    caret = 0;
                } else {
                    caret = doc.getLength();
                }
                return;
            }

            //highlight từ tiếp theo tìm được
            docHighlighter.removeHighlight(htemp);
            docHighlighter.addHighlight(htemp.getStartOffset(),htemp.getEndOffset(),selectedPainter);

            //di chuyển màn hình tới từ tìm được
            editor.editorPane.setCaretPosition(htemp.getStartOffset());
            //thêm vào caret để đảm bảo lần tiếp theo sẽ tìm đến từ tiếp theo
            this.caret = htemp.getStartOffset() + vec;


        } catch (Exception ex) {
            System.out.println(ex.toString());
        }

    }

    /*
     * hàm xử lý khi người dùng thay đổi nội dung của document trong khi đang highlight các từ tìm được
     * xóa highlight từ nào bị thay đổi
     * được gọi từ MyActionHandler->ACDocListener
     */
    public void exHighlight() {

        Highlighter docHighlighter = editor.editorPane.getHighlighter();

        Highlight[] hili = docHighlighter.getHighlights();

        try {
            //duyệt toàn bộ các từ được highlight
            for (Highlight h : hili) {
                if (h.getStartOffset() <= caret && h.getEndOffset() >= caret) {
                    docHighlighter.removeHighlight(h);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    /*
     * hàm public thay đổi caret
     * được gọi từ MyActionHandler->ACCaretListener
     */
    public void setCaret(int caret) {
        this.caret = caret;
    }

}
   
