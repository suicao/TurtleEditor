package gui;

import com.alee.extended.layout.ToolbarLayout;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.extended.tree.WebFileTree;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tree.TreeSelectionStyle;
import controller.*;
import com.alee.extended.panel.WebCollapsiblePane;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import controller.TabHandler;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.CaretMonitor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import plugin.PluginLoader;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedList;

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

/**
 * Created by khoi on 4/5/2014.
 */
public class MainEditor extends JFrame{
    //ActionHandler
    private ActionHandler action = new ActionHandler(this);
    //PluginLoader
    PluginLoader loader;
    //TabHandler
    private LinkedList<TabHandler> handlerList = new LinkedList<TabHandler>();
    private TabHandler currentHandler;
    //Panel cho layout
    private WebCollapsiblePane westTopPanel;
    private WebCollapsiblePane westBotPanel;
    private WebSplitPane westSplitPane;
    private WebSplitPane mainPane;
    //Caret info hiện trên menu
    WebLabel caretInfo;
    private WebPanel caretInfoPanel;
    //Tab pane chứa editor
    private WebTabbedPane tabPane;
    //Menu
    private Menu menu;
    //Toolbar
    private Toolbar toolbar;
    //Popupmenu chuột phải
    private PopupMenu popupMenu;
    //File list
    FileList fileList;
    //Home Folder
    private WebFileTree homeFileTree;
    private JScrollPane homeFileTreeScroll;
    //Thanh Status
    private WebStatusBar statusBar;
    //File mặc định
    public final String DEFAULT_FILE = "Untitled";
    //Home mặc định
    private String homeDir;
    //Action map
    private ActionMap m;
    protected Action Cut;
    protected Action Copy;
    protected Action Paste;
    protected Action SelectAll;
    //Sử dụng Json lưu settingDialog
    private JSONObject setting;
    private JSONParser parser;

    /**Getter*/
    public String getSetting() {
        return setting.toJSONString();
    }
    public String getSettingFromKey(String key) {
        return setting.get(key).toString();
    }
    ActionHandler getActionHandler(){
        return action;
    }
    public WebTabbedPane getTabPane(){
        return tabPane;
    }
    public DefaultListModel getFileList(){
        return fileList.getFiles();
    }
    public TabHandler getCurrentHandler() {
        return currentHandler;
    }

    public JEditorPane getCurrentEditor() {
        return currentHandler.getEditorPane();
    }

    public LinkedList<TabHandler> getHandlerList() {
        return handlerList;
    }

    public JEditorPane getEditorAtTab(int tabCount) {
        return handlerList.get(tabCount).getEditorPane();
    }

    public TabHandler getHandlerAtTab(int tabCount) {
        return handlerList.get(tabCount);
    }

    public WebStatusBar getStatusBar(){
        return statusBar;
    }
    public Menu getMenu(){
        return menu;
    }

    public WebButton[] getButtonGroup(){
        return toolbar.getButtonGroup();
    }
    /**Setter*/
    public void setEditorHotKey(JEditorPane editorPane, KeyStroke ks, Action newAction, int focus) {
        editorPane.registerKeyboardAction(newAction, ks, focus);
    }
    public void setEditorSetting(JSONObject setting,JEditorPane editorPane){
        String fontName = String.valueOf(setting.get("font"));
        int fontSize = Integer.parseInt(String.valueOf(setting.get("font-size")));
        Font font = new Font(fontName, Font.PLAIN, fontSize);
        editorPane.setFont(font);
    }
    public void setHomeDir(String s){
        this.homeDir = s;
    }

    /**
     * Constructor
     * */
    public MainEditor(){

        toolbar = new Toolbar(this);
        fileList = new FileList(this);
        tabPane = new WebTabbedPane();
        tabPane.setOpaque(false);
        tabPane.setTabbedPaneStyle(TabbedPaneStyle.attached);
        final WebPanel tabPanel = new WebPanel(true, tabPane);
        //Load setting
        parser = new JSONParser();
        try {
            setting = (JSONObject) parser.parse(new FileReader("data/setting.json"));
        } catch (IOException | ParseException ioe) {
            System.out.print("Setting file not found");
        }
        homeDir = (String) setting.get("home");
        /* Bắt đầu đặt layout */

        westTopPanel = new WebCollapsiblePane(new ImageIcon(getClass().getResource("/resources/images/open.png")), "<html><body leftmtrrgin=5 topmargin=5 marginwidth=8 marginheight=4>" +
                "Opened files"
                + "</body></html>");
        westTopPanel.setExpanded(true);
        westTopPanel.add(fileList.getFileListScroll());

        homeDir = (String) setting.get("home");

        westBotPanel = new WebCollapsiblePane(new ImageIcon(getClass().getResource("/resources/images/home.png")), "<html><body leftmtrrgin=5 topmargin=6 marginwidth=8 marginheight=4>" +
                "Home folder" +
                "</body></html>");
        initHomeFileTree();
        westBotPanel.add(homeFileTreeScroll);
        westBotPanel.setExpanded(true);
        //Tạo pane xẻ ngang
        westSplitPane = new WebSplitPane(VERTICAL_SPLIT, westTopPanel, westBotPanel);
        westSplitPane.setOneTouchExpandable(true);
        westSplitPane.setContinuousLayout(false);
        westSplitPane.setDividerLocation(250);
        add(westSplitPane, BorderLayout.WEST);

        //Khởi tạo main pane xẻ dọc
        mainPane = new WebSplitPane(HORIZONTAL_SPLIT, westSplitPane, tabPanel);
        mainPane.setOneTouchExpandable(true);
        mainPane.setContinuousLayout(false);
        mainPane.setDividerLocation(250);
        add(mainPane, BorderLayout.CENTER);

        //Action map hỗ trợ 1 số chức năng cơ bản: cut, copy, paste
        m = (new JEditorPane()).getActionMap();
        Copy = m.get(DefaultEditorKit.copyAction);
        Cut = m.get(DefaultEditorKit.cutAction);
        Paste = m.get(DefaultEditorKit.pasteAction);
        SelectAll = m.get(DefaultEditorKit.selectAllAction);

        //Menu
        menu = new Menu(this);

        //Editor
        initEditorPane(DEFAULT_FILE);
        currentHandler = handlerList.get(0);
        popupMenu = new PopupMenu(this);
        setJMenuBar(menu);
        caretInfoPanel = new WebPanel();
        caretInfoPanel.setOpaque(false);
        caretInfoPanel.add(new WebLabel("Line:Column "));
        caretInfoPanel.add(caretInfo, BorderLayout.EAST);
        menu.add(caretInfoPanel, ToolbarLayout.END);
        // Thanh status
        statusBar = new WebStatusBar();


        add(statusBar, BorderLayout.AFTER_LAST_LINE);

        //Find replace
        statusBar.add(action.findReplace, ToolbarLayout.END);
        action.findReplace.setVisible(true);

        statusBar.add(new WebButtonGroup(toolbar.getButtonGroup()), ToolbarLayout.START);
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int index = tabPane.getSelectedIndex();
                try {
                    caretInfoPanel.removeAll();
                    caretInfoPanel.revalidate();
                    caretInfoPanel.repaint();
                    currentHandler = handlerList.get(index);
                    caretInfo = currentHandler.getCaretInfo();
                    caretInfoPanel.add(new WebLabel("Line:Column "));
                    caretInfoPanel.add(caretInfo, BorderLayout.EAST);

                } catch (IndexOutOfBoundsException aibe) {
                    System.out.println("No tab");
                }
            }
        });
        //Plugin Loader
        loader = new PluginLoader(this);
        setTitle("Turtle Editor 0.3");
        addWindowListener(action.ExitListener);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setVisible(true);
        pack();
    }


    /**Các phương thức*/

    //Khởi tạo tab mới
    public void initEditorPane(final String fileName) {
        //Tạo tabPane mơi
        final JEditorPane editorPane = new JEditorPane();
        //Add vào scroll
        JScrollPane scroll = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        DefaultSyntaxKit.initKit();
        editorPane.setContentType("text/java;utf-8");
        scroll.setComponentPopupMenu(popupMenu);
        editorPane.setBorder(null);

        caretInfo = new WebLabel();
        caretInfo.setMargin(0, 0, 0, 5);
        CaretMonitor caretMonitor = new CaretMonitor(editorPane, caretInfo);

        //Cài đặt auto complete và find replace listener
        editorPane.getDocument().addDocumentListener(action.ACDocListener);
        editorPane.addCaretListener(action.ACCaretListener);

        //Cài đặt phím tắt
        setEditorHotKey(editorPane, KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.CTRL_MASK), action.Undo, JComponent.WHEN_FOCUSED);
        setEditorHotKey(editorPane, KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, InputEvent.CTRL_MASK), action.Redo, JComponent.WHEN_FOCUSED);
        setEditorHotKey(editorPane, KeyStroke.getKeyStroke(
                KeyEvent.VK_F, InputEvent.CTRL_MASK), action.FindReplace, JComponent.WHEN_FOCUSED);
        setEditorHotKey(editorPane, KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_MASK), action.Save, JComponent.WHEN_FOCUSED);
        setEditorHotKey(editorPane, KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), action.AutoComplete, JComponent.WHEN_FOCUSED);
        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(editorPane, e.getX(), e.getY());
                }
            }
        });

        //Cài đặt undo manager
        UndoManager manager = new UndoManager();

        //Nút close tab
        final WebButton closeBtn = new WebButton();
        closeBtn.setIcon(new ImageIcon(getClass().getResource("/resources/images/close.png")));
        closeBtn.setRolloverDecoratedOnly(true);
        closeBtn.setDrawFocus(false);
        closeBtn.setFocusable(false);
        closeBtn.setBorder(null);

        //Tên hiển thị trên tab
        String displayName;

        //Nằm ở cuối đường dẫn, sau kí hiệu '\' cuối cùng
        int i = fileName.lastIndexOf('\\');
        if (i != -1) {
            displayName = fileName.substring(i + 1, fileName.length());
            if (displayName.length() > 12) {
                displayName = displayName.substring(0, 12) + "...";
            }
        } else {
            displayName = fileName;
        }
        WebLabel tabLabel = new WebLabel("<html><body leftmtrrgin=5 topmargin=5 marginwidth=15 marginheight=2>" + displayName + "</body></html>");
        FlowLayout f = new FlowLayout(FlowLayout.CENTER, 1, 0);

        //Tạo panel để chứa label và nút close
        final JPanel pnlTab = new JPanel(f);
        pnlTab.setOpaque(false);
        pnlTab.add(tabLabel);
        pnlTab.add(closeBtn);

        //Khởi tạo handle
        final TabHandler handler = new TabHandler(
                editorPane, manager, fileName, caretInfo,
                closeBtn, tabLabel, pnlTab,
                false);
        handlerList.add(handler);

        //Thêm scroll vào tab
        tabPane.addTab("", scroll);
        tabPane.setSelectedComponent(scroll);
        //Thêm nút close và label
        tabPane.setTabComponentAt(tabPane.getSelectedIndex(), pnlTab);

        //cài đặt sự kiện close tab cho nút
        closeBtn.addActionListener(e->{action.closeCurrentTab(tabPane, handler, handlerList);});
        if (fileName.equals(DEFAULT_FILE)) {
            setEditorSetting(setting,editorPane);
            editorPane.getDocument().addUndoableEditListener(manager);
            editorPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    handler.setChanged(true);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    handler.setChanged(true);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    handler.setChanged(true);
                }
            });
        }
    }
    void changeHome() {
        westBotPanel.remove(homeFileTreeScroll);
        System.out.printf("\nHome dir is" + homeDir);
        initHomeFileTree();
        westBotPanel.validate();
        westBotPanel.add(homeFileTreeScroll, 1);
        westBotPanel.validate();
        westBotPanel.repaint();
        setting.remove("home");
        setting.put("home", homeDir);
        System.out.printf("\n"+setting.get("home"));
        try {
            FileWriter f = new FileWriter("data/setting.json");
            f.write(setting.toJSONString());
            f.flush();
            f.close();
        } catch (IOException e) {
            System.out.print("wtf");
        }

    }
    private void initHomeFileTree() {
        //Đặt thư mục gốc
        homeFileTree = new WebFileTree(homeDir);
        //Setting linh tinh
        homeFileTree.setAutoExpandSelectedNode(false);
        homeFileTree.setShowsRootHandles(true);
        homeFileTree.setBorder(null);
        homeFileTree.setSelectionStyle(TreeSelectionStyle.group);
        homeFileTree.setSelectionMode(JFileChooser.FILES_ONLY);
        //Đặt scroll tabPane để hiện số dòng
        homeFileTreeScroll = new WebScrollPane(homeFileTree);

        //Đặt actionListener cho cây
        homeFileTree.addTreeSelectionListener(e->{
            try {
                if (homeFileTree.getSelectedFile().isFile()) {
                    String selectedFile = homeFileTree.getSelectedFile().getAbsolutePath();
                    action.readInFile(selectedFile);
                }
            } catch (NullPointerException npe) {
                System.out.printf("Nothing selected");
            }
        });
    }

    public static void main(String args[]){
        WebLookAndFeel.install();
        new MainEditor();
    }
}
 
