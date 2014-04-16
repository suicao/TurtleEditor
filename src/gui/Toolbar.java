package gui;
import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import com.alee.utils.swing.DialogOptions;
import controller.*;
import com.alee.laf.button.WebButton;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by khoi on 4/5/2014.
 */
class Toolbar extends WebToolBar {
    MainEditor editor;
    ActionHandler action;
    WebButton buttons[];
    WebButton[] getButtonGroup(){
        return buttons;
    }
    public Toolbar(final MainEditor editor){
        this.editor = editor;
        this.action = editor.getActionHandler();
        //Thanh công cụ(toolbar)
        setToolbarStyle(ToolbarStyle.attached);
        setFloatable(false);
        setSpacing(3);
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
        //      settingDialog
        WebButton setting = new WebButton();
        TooltipManager.setTooltip(setting, "Setting", TooltipWay.up, 0);
        setting.addActionListener(action.SettingChange);
        //      home change
        WebButton home = new WebButton();
        TooltipManager.setTooltip(home, "Change home folder", TooltipWay.up, 0);
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebDirectoryChooser chooser = new WebDirectoryChooser(editor.getOwner(),"Choose new Home directory");
                chooser.setVisible(true);
                if (chooser.getResult() == DialogOptions.OK_OPTION) {
                    editor.setHomeDir(chooser.getSelectedDirectory().getAbsolutePath());

                } else {
                    System.out.println("No Selection ");
                }
                editor.changeHome();
            }
        });
        WebButton about = new WebButton();
        TooltipManager.setTooltip(about, "About us", TooltipWay.up, 0);
        about.addActionListener(action.About);

        //Thêm các nút vào toolbar
        WebButton buttons[] = {home, open, newFile, save, setting, about};
        this.buttons = buttons;
        for (WebButton button : buttons) {
            button.setRolloverDecoratedOnly(false);
            button.setDrawFocus(true);
        }
        //Đặt icon

        newFile.setIcon(new ImageIcon(getClass().getResource("/resources/images/new.png")));
        setting.setIcon(new ImageIcon(getClass().getResource("/resources/images/setting.png")));
        open.setIcon(new ImageIcon(getClass().getResource("/resources/images/open.png")));
        save.setIcon(new ImageIcon(getClass().getResource("/resources/images/save.png")));
        home.setIcon(new ImageIcon(getClass().getResource("/resources/images/home.png")));
        about.setIcon(new ImageIcon(getClass().getResource("/resources/images/about.png")));
    }
}
