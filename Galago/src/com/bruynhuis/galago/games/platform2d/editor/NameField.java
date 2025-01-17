/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.games.platform2d.editor;

import com.bruynhuis.galago.app.BaseApplication;
import com.bruynhuis.galago.ui.field.TextField;
import com.bruynhuis.galago.ui.listener.FocusListener;
import com.bruynhuis.galago.ui.panel.Panel;
import java.util.Properties;

/**
 *
 * @author NideBruyn
 */
public class NameField extends TextField {

    public NameField(Panel panel) {
        super(panel, "namefield", "Resources/textfield.png", 300, 50, true);
        setText("");
        setFontSize(28);
        setMaxLength(30);
        addFocusListener(new FocusListener() {

            public void doFocus(String id) {
                Properties p = new Properties();
                p.setProperty(BaseApplication.NAME, getText());
                window.getApplication().fireKeyboardInputListener(p, NameField.this);
            }

            @Override
            public void doBlur(String id) {
                Properties p = new Properties();
                p.setProperty(BaseApplication.NAME, getText());
                window.getApplication().fireKeyboardInputListener(p, NameField.this);
            }
        });
        
    }
    
}
