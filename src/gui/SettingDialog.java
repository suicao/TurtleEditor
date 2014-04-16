package gui;

import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.WebAccordion;
import com.alee.extended.panel.WebAccordionStyle;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.list.WebList;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;

import javax.swing.*;
import java.awt.*;
public class SettingDialog extends WebDialog{
    //Menu nén accorion
    private WebAccordion accordion;

    //Các danh sách font, cữ chữ, kiểu cú pháp có thể lựa chọn
    private WebList fontList, sizeList, typeList;

    //Nút bấm
    private WebButton ok = new WebButton(new ImageIcon(getClass().getResource("/resources/images/ok.png")));
    private WebButton cancel = new WebButton(new ImageIcon(getClass().getResource("/resources/images/cancel.png")));

    private WebButtonGroup group = new WebButtonGroup(true, ok,cancel);
    private WebPanel panel= new GroupPanel();

    //Layout sử dụng, tự tìm hiểu thêm
    private GridBagLayout gbl;
    private GridBagConstraints gbc;

    //Xử lý các nút ok, cancel
    //Định nghĩa trong MyActionHandler
    public WebButton getOK(){
        return ok;
    }
    public WebButton getCancel(){
        return cancel;
    }
    //Getters
    public WebList getFontList(){
        return fontList;
    }
    public WebList getSizeList(){
        return sizeList;
    }
    public WebList getTypeList(){
        return typeList;
    }
    public SettingDialog(){

        setResizable(false);
        setTitle("Settings");

        ok.setDrawFocus(true);
        ok.setRolloverDecoratedOnly(false);
        cancel.setDrawFocus(true);
        cancel.setRolloverDecoratedOnly(false);

        gbl = new GridBagLayout();
        panel.setLayout(gbl);
        gbc = new GridBagConstraints();

        accordion = new WebAccordion(WebAccordionStyle.accordionStyle);
        accordion.setMultiplySelectionAllowed(false);

        //Tạo list font, sizes và types
        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        WebScrollPane fontScroll,sizeScroll,typeScroll;
        fontList = new WebList(fonts);
        fontList.setPreferredWidth(150);
        fontList.setSelectedIndex(295);
        fontScroll = new WebScrollPane(fontList);

        String[] sizes = {"8","10","12","14","16","18","20","24","28","32","48","72"};
        sizeList = new WebList(sizes);
        sizeList.setSelectedIndex(3);
        fontList.setPreferredWidth(250);
        sizeScroll = new WebScrollPane(sizeList);

        String[] types = {"text/c", "text/bash", "text/java", "text/groovy","text/javascript","text/python","text/ruby","text/xml","text/sql"};
        typeList = new WebList(types);
        typeList.setSelectedIndex(2);
        typeScroll = new WebScrollPane(typeList);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        //Thêm vào accordion
        accordion.addPane ( null, "<html><body leftmtrrgin=5 topmargin=5 marginwidth=5 marginheight=2>" + "Fonts" + "</body></html>", fontScroll );
        accordion.addPane ( null, "<html><body leftmtrrgin=5 topmargin=5 marginwidth=5 marginheight=2>" + "Font sizes" + "</body></html>", sizeScroll );
        accordion.addPane ( null, "<html><body leftmtrrgin=5 topmargin=5 marginwidth=5 marginheight=2>" + "Syntax types" + "</body></html>", typeScroll);
        panel.add(accordion,gbc);

        //Di chuyển xuống, chèn nút
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5,220,5,0);
        panel.add(group,gbc);

        getContentPane().add(panel);
        pack();
    }
}
 
