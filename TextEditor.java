package text;

import com.alee.extended.layout.ToolbarLayout;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.extended.panel.WebCollapsiblePane;
import com.alee.extended.statusbar.WebMemoryBar;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.extended.tree.WebFileTree;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.list.WebList;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.TabbedPaneStyle;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.laf.text.WebEditorPane;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.laf.tree.TreeSelectionStyle;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

import static com.alee.laf.splitpane.WebSplitPane.VERTICAL_SPLIT;
import static javax.swing.JSplitPane.HORIZONTAL_SPLIT;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.*;

import org.json.simple.*;
import org.json.simple.parser.*;


import javax.swing.*;

import javax.swing.event.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;


public class TextEditor extends WebFrame {
    //Tab lưu các file đang mở
    WebTabbedPane tabPane;

    //Tạo một đối tượng của lớp MyActionHandler và AutoComplete
    //Dùng chính đối tượng này làm tham số
    //Để chúng liên kết với nhau
    private MyActionHandler action = new MyActionHandler(this);
    //Quản lý các file bằng 1 Handler
    LinkedList<TabHandler> handlerList = new LinkedList<TabHandler>();
    TabHandler currentHandler;
    //Cửa số chọn thư mục cho home folder
    private JFileChooser chooser;
    //Chọn file
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    //Menu
    WebMenuBar menu;

    //Caret infomations
    WebPanel caretInfoPanel;
    WebLabel caretInfo;
    //Cửa sổ editor
    WebEditorPane editorPane;
    JScrollPane scroll;

    //Panels
    WebCollapsiblePane westTopPanel;
    WebCollapsiblePane westBotPanel;
    WebSplitPane westSplitPane;
    WebSplitPane mainPane;

    //List file đang mở
    WebList fileList;
    JScrollPane fileListScroll;
    DefaultListModel files;
    //Status bar
    WebStatusBar statusBar;
    //Action map hỗ trợ 1 số chức năng cơ bản: cut, copy, paste
    ActionMap m;
    Action Cut;
    Action Copy;
    Action Paste;
    Action SelectAll;
    //Chức năng chuột phải
    WebPopupMenu popupMenu;
    JFrame popupFrame;
    //Cây thư mục
    private WebFileTree homeFileTree;
    private WebScrollPane homeFileTreeScroll;

    //Thư mục home mặc định
    private String homeDir;
    //File mặc định
    final String DEFAULT_FILE = "Untitled";
    //Sử dụng Json lưu setting
    JSONObject setting;
    JSONParser parser;

    public TextEditor() {
        setPreferredSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        //Fancy tabbed tabPane
        tabPane = new WebTabbedPane();
        tabPane.setOpaque(false);
        tabPane.setTabbedPaneStyle(TabbedPaneStyle.attached);
        final WebPanel tabPanel = new WebPanel(true, tabPane);
        tabPanel.setDrawFocus(false);
        //Load setting
        parser = new JSONParser();
        try {
            setting = (JSONObject) parser.parse(new FileReader("data/setting.json"));
        } catch (IOException|ParseException ioe) {
            System.out.print("Setting file not found");
        }

        //Popup menu
        initPopupMenu();

        //Khởi tạo opened files pane

        files = new DefaultListModel();
        files.addElement("Untitled");
        fileList = new WebList(files);

        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int i = 0;
                for (TabHandler tempHandler : handlerList) {
                    if (tempHandler.fileName.equals(fileList.getSelectedValue())) {
                        tabPane.setSelectedIndex(i);
                        break;
                    }
                    i++;
                }

            }
        });
        fileListScroll = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        westTopPanel = new WebCollapsiblePane(new ImageIcon(getClass().getResource("/images/open.png")), "<html><body leftmtrrgin=5 topmargin=5 marginwidth=8 marginheight=4>" +
                "Opened files"
                + "</body></html>");
        westTopPanel.setExpanded(true);
        westTopPanel.add(fileListScroll);
        westTopPanel.setDrawFocus(false);
        //Khởi tạo file tree pane
        homeDir = (String) setting.get("home");

        initHomeFileTree();
        westBotPanel = new WebCollapsiblePane(new ImageIcon(getClass().getResource("/images/home.png")), "<html><body leftmtrrgin=5 topmargin=6 marginwidth=8 marginheight=4>" +
                "Home folder" +
                "</body></html>");
        westBotPanel.add(homeFileTreeScroll);
        westBotPanel.setExpanded(true);
        westBotPanel.setDrawFocus(false);
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
        //Menu
        initMenu();
        //Editor
        initEditorPane("Untitled");
        currentHandler = handlerList.get(0);
        //Action map hỗ trợ 1 số chức năng cơ bản: cut, copy, paste
        m = editorPane.getActionMap();
        Copy = m.get(DefaultEditorKit.copyAction);
        Cut = m.get(DefaultEditorKit.cutAction);
        Paste = m.get(DefaultEditorKit.pasteAction);
        SelectAll = m.get(DefaultEditorKit.selectAllAction);

        // Thanh status
        statusBar = new WebStatusBar();


        //Thanh memory/ bộ nhớ đã dùng
        WebMemoryBar memoryBar = new WebMemoryBar();
        memoryBar.setPreferredWidth(memoryBar.getPreferredSize().width + 20);
        statusBar.add(memoryBar, ToolbarLayout.END);
        memoryBar.setVisible(false);
        add(statusBar, BorderLayout.AFTER_LAST_LINE);

        //Find replace
        statusBar.add(action.findReplace, ToolbarLayout.END);
        action.findReplace.setVisible(true);

        //Toolbar
        initToolbar();
        tabPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int index = tabPane.getSelectedIndex();
                try {
                    caretInfoPanel.removeAll();
                    caretInfoPanel.revalidate();
                    caretInfoPanel.repaint();
                    scroll = (JScrollPane) tabPane.getComponentAt(index);
                    currentHandler = handlerList.get(index);
                    editorPane = currentHandler.editorPane;
                    caretInfo = currentHandler.caretInfo;
                    caretInfoPanel.add(new WebLabel("Line:Column "));
                    caretInfoPanel.add(caretInfo, BorderLayout.EAST);

                } catch (ArrayIndexOutOfBoundsException aibe) {
                    System.out.println("No tab");
                }
            }
        });


        setTitle("Turtle Editor 0.2beta");
        addWindowListener(action.ExitListener);
        setVisible(true);
        pack();
    }


    /*--------------*/
    void initPopupMenu() {
        popupMenu = new WebPopupMenu();
        popupFrame = new JFrame();
        final WebMenuItem copy = new WebMenuItem("Copy", Hotkey.CTRL_C);
        copy.addActionListener(Copy);
        final WebMenuItem cut = new WebMenuItem("Cut", Hotkey.CTRL_X);
        cut.addActionListener(Cut);
        final WebMenuItem paste = new WebMenuItem("Paste", Hotkey.CTRL_V);
        paste.addActionListener(Paste);
        final WebMenuItem selectAll = new WebMenuItem("Select all", Hotkey.CTRL_A);
        selectAll.addActionListener(SelectAll);
        final WebMenuItem fandR = new WebMenuItem("Find and Replace", Hotkey.CTRL_F);
        fandR.addActionListener(action.FindReplace);
        popupMenu.add(copy);
        popupMenu.add(cut);
        popupMenu.add(paste);
        popupMenu.add(selectAll);
        popupMenu.add(fandR);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                copy.setEnabled(true);
                cut.setEnabled(true);
                paste.setEnabled(true);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        popupFrame.add(popupMenu);
    }

    /*----------------*/
    void initMenu() {
        //Menu bar
        menu = new WebMenuBar();
        menu.setUndecorated(false);
        setJMenuBar(menu);
        //Các menu trên thanh menu:
        WebMenu edit = new WebMenu("Edit", new ImageIcon(getClass().getResource("/images/edit.png")));
        WebMenu file = new WebMenu("File", new ImageIcon(getClass().getResource("/images/file.png")));
        WebMenu help = new WebMenu("Help", new ImageIcon(getClass().getResource("/images/help.png")));
        WebMenu format = new WebMenu("Format", new ImageIcon(getClass().getResource("/images/format.png")));
        WebMenu hinter = new WebMenu("Hinter", new ImageIcon(getClass().getResource("/images/hinter.png")));

        //Thêm vào thanh menu
        menu.add(file);
        menu.add(edit);
        menu.add(format);
        menu.add(hinter);
        menu.add(help);

        // File menu in menu bar
        WebMenuItem newFile, open, save, saveAs, close, closeAll, quit;
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
                action.closeCurrentTab(tabPane, currentHandler, handlerList);
            }
        });
        closeAll = new WebMenuItem("Close all tab", Hotkey.ALT_W);
        closeAll.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    TabHandler handler = handlerList.getFirst();
                    while (handlerList.getLast() != handler) {
                        action.closeCurrentTab(tabPane, handlerList.getLast(), handlerList);
                    }
                    action.closeCurrentTab(tabPane, handler, handlerList);
                } catch (NoSuchElementException nsee) {
                    System.out.println("No tab to close");
                }


            }
        });
        quit = new WebMenuItem("Quit", Hotkey.ALT_F4);
        quit.addActionListener(action.Quit);

        file.add(newFile);
        file.add(open);
        file.addSeparator();
        file.add(save);
        file.add(saveAs);
        file.add(close);
        file.add(closeAll);
        file.add(quit);

        // Edit menu in menu bar

        WebMenuItem undo, redo, cut, copy, paste,
                deleteLine, duplicateLine, gotoLine,
                find, findNext, findPrev, replace, replaceAll;

        WebMenu findMenu, lineMenu;

        undo = new WebMenuItem("Undo", Hotkey.CTRL_Z);
        undo.addActionListener(action.Undo);

        redo = new WebMenuItem("Redo", Hotkey.CTRL_Y);
        redo.addActionListener(action.Redo);

        //Các chức năng khác
        cut = new WebMenuItem("Cut", Hotkey.CTRL_X);
        cut.addActionListener(Cut);

        copy = new WebMenuItem("Copy", Hotkey.CTRL_C);
        copy.addActionListener(Copy);

        paste = new WebMenuItem("Paste", Hotkey.CTRL_V);
        paste.addActionListener(Paste);

        findMenu = new WebMenu("Find and Replace");

        find = new WebMenuItem("Find and Replace", Hotkey.CTRL_F);
        find.addActionListener(action.FindReplace);

        findNext = new WebMenuItem("Find next", Hotkey.ALT_F);
        findNext.addActionListener(action.findReplace.Next);

        findPrev = new WebMenuItem("Find previous", Hotkey.ALT_G);
        findPrev.addActionListener(action.findReplace.Previous);

        replace = new WebMenuItem("Replace", Hotkey.ALT_H);
        replace.addActionListener(action.findReplace.Replace);

        replaceAll = new WebMenuItem("Replace all", Hotkey.ALT_J);
        replaceAll.addActionListener(action.findReplace.ReplaceAll);

        findMenu.add(find);
        findMenu.add(findNext);
        findMenu.add(findPrev);
        findMenu.add(replace);
        findMenu.add(replaceAll);


        lineMenu = new WebMenu("Line");
        deleteLine = new WebMenuItem("Delete line", Hotkey.ALT_E);
        deleteLine.addActionListener(new DeleteLinesAction());

        duplicateLine = new WebMenuItem("Duplicate line");
        duplicateLine.addActionListener(new DuplicateLinesAction());

        gotoLine = new WebMenuItem("Go to line", Hotkey.CTRL_G);
        gotoLine.addActionListener(new GotoLineAction());
        lineMenu.add(deleteLine);
        lineMenu.add(duplicateLine);
        lineMenu.add(gotoLine);

        edit.add(undo);
        edit.add(redo);
        edit.addSeparator();
        edit.add(cut);
        edit.add(copy);
        edit.add(paste);
        edit.addSeparator();
        edit.add(findMenu);
        edit.add(lineMenu);


        //Format menu in menu bar
        WebMenuItem setting, autoComplete;
        setting = new WebMenuItem("Setting", Hotkey.CTRL_T);
        setting.addActionListener(action.SettingChange);
        format.add(setting);

        autoComplete = new WebMenuItem("Auto Correct Setting", Hotkey.ALT_A);
        autoComplete.addActionListener(action.ACSettingChange);
        format.add(autoComplete);

        //Hinter menu;
        WebMenuItem hint;
        hint = new WebMenuItem("Code Hint", Hotkey.CTRL_SPACE);
        hint.addActionListener(action.CodeHint);
        hinter.add(hint);

        //Label cho caret monitor
        caretInfoPanel = new WebPanel();
        caretInfoPanel.setOpaque(false);
        caretInfoPanel.add(new WebLabel("Line:Column "));
        menu.add(caretInfoPanel, ToolbarLayout.END);
    }


    void initToolbar() {
        //Thanh công cụ(toolbar)
        WebToolBar toolbar = new WebToolBar(WebToolBar.VERTICAL);
        toolbar.setToolbarStyle(ToolbarStyle.attached);
        toolbar.setFloatable(false);
        toolbar.setSpacing(3);

        //      open
        WebButton open = new WebButton();
        //Đặt chú thích khi rê chuột đến
        TooltipManager.setTooltip(open, "Open", TooltipWay.up, 0);

        open.addActionListener(action.Open);
        //      save
        WebButton save = new WebButton();
        TooltipManager.setTooltip(save, "Save", TooltipWay.up, 0);
        save.addActionListener(action.Save);
        //      new
        WebButton newFile = new WebButton();
        TooltipManager.setTooltip(newFile, "New File", TooltipWay.up, 0);
        newFile.addActionListener(action.New);
        //      setting
        WebButton setting = new WebButton();
        TooltipManager.setTooltip(setting, "Setting", TooltipWay.up, 0);
        setting.addActionListener(action.SettingChange);
        //      home change
        WebButton home = new WebButton();
        TooltipManager.setTooltip(home, "Change home folder", TooltipWay.up, 0);
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser = new JFileChooser("Choose new Home directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    homeDir = chooser.getSelectedFile().getAbsolutePath();
                } else {
                    System.out.println("No Selection ");
                }
                changeHome();
            }
        });
        WebButton about = new WebButton();
        TooltipManager.setTooltip(about, "About us", TooltipWay.up, 0);
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initEditorPane(DEFAULT_FILE);
            }
        });

        //Thêm các nút vào toolbar

        WebButton buttons[] = {home, open, newFile, save, setting, about};
        statusBar.add(new WebButtonGroup(buttons), ToolbarLayout.START);
        for (WebButton button : buttons) {
            button.setRolloverDecoratedOnly(false);
            button.setDrawFocus(true);
        }

        //Đặt icon

        newFile.setIcon(new ImageIcon(getClass().getResource("/images/new.png")));
        setting.setIcon(new ImageIcon(getClass().getResource("/images/setting.png")));
        open.setIcon(new ImageIcon(getClass().getResource("/images/open.png")));
        save.setIcon(new ImageIcon(getClass().getResource("/images/save.png")));
        home.setIcon(new ImageIcon(getClass().getResource("/images/home.png")));
        about.setIcon(new ImageIcon(getClass().getResource("/images/about.png")));
    }

    void initHomeFileTree() {
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
        homeFileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                if (homeFileTree.getSelectedFile().isFile()) {
                    String selectedFile = homeFileTree.getSelectedFile().getAbsolutePath();
                    action.readInFile(selectedFile);
                }
            }
        });
    }

    //Phương thức để thay đổi thư mục home
    void changeHome() {
        westBotPanel.remove(homeFileTreeScroll);
        initHomeFileTree();
        westBotPanel.validate();
        westBotPanel.add(homeFileTreeScroll, 1);
        westBotPanel.validate();
        westBotPanel.repaint();
        setting.remove("home");
        setting.put("home", homeDir);
        try {
            FileWriter f = new FileWriter("data/setting.json");
            f.write(setting.toJSONString());
            f.flush();
            f.close();
        } catch (IOException e) {
            System.out.print("wtf");
        }

    }


    void initEditorPane(final String fileName) {
        //Tạo tabPane mơi
        editorPane = new WebEditorPane();
        //Add vào scroll
        scroll = new JScrollPane(editorPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        DefaultSyntaxKit.initKit();
        scroll.setComponentPopupMenu(popupMenu);
        //Đặt setting mặc định
        editorPane.setBorder(null);
        String contentType = String.valueOf(setting.get("content-type"));
        String encoding = String.valueOf(setting.get("encoding"));
        editorPane.setContentType(contentType + ";" + encoding);
        int fontSize = Integer.parseInt(String.valueOf(setting.get("font-size")));
        editorPane.setFont(new Font((String) setting.get("font"), Font.PLAIN, fontSize));

        caretInfo = new WebLabel();
        caretInfo.setMargin(0, 0, 0, 5);
        CaretMonitor caretMonitor = new CaretMonitor(editorPane, caretInfo);

        caretInfoPanel.add(caretInfo,BorderLayout.EAST);
        //Cài đặt auto complete và find replace listener
        editorPane.getDocument().addDocumentListener(action.ACDocListener);
        editorPane.addCaretListener(action.ACCaretListener);

        //Cài đặt phím tắt
        editorPane.registerKeyboardAction(action.Undo, KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);
        editorPane.registerKeyboardAction(action.Redo, KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);

        editorPane.registerKeyboardAction(action.FindReplace, KeyStroke.getKeyStroke(
                KeyEvent.VK_F, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);

        editorPane.registerKeyboardAction(action.Save, KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);

        editorPane.registerKeyboardAction(action.CodeHint, KeyStroke.getKeyStroke(
                KeyEvent.VK_SPACE, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);

        editorPane.registerKeyboardAction(action.AutoComplete, KeyStroke.getKeyStroke(
                KeyEvent.VK_E, InputEvent.CTRL_MASK), JComponent.WHEN_FOCUSED);

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
        closeBtn.setIcon(new ImageIcon(getClass().getResource("/images/close.png")));
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
        final TabHandler handler = new TabHandler(editorPane, manager, fileName, caretInfo, closeBtn, tabLabel, pnlTab, false);
        handlerList.add(handler);

        //Thêm scroll vào tab
        tabPane.addTab("", scroll);
        tabPane.setSelectedComponent(scroll);

        //Thêm nút close và label
        tabPane.setTabComponentAt(tabPane.getSelectedIndex(), pnlTab);

        //cài đặt sự kiện close tab cho nút
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.closeCurrentTab(tabPane, handler, handlerList);
            }
        });
        if (fileName.equals(DEFAULT_FILE)) {
            editorPane.getDocument().addUndoableEditListener(manager);
            editorPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    handler.changed = true;
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    handler.changed = true;
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    handler.changed = true;
                }
            });
        }
    }

    public static void main(String[] arg) {

        WebLookAndFeel.install();
        new TextEditor();
    }
}
