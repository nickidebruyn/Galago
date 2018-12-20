/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.galago.example.platformer2d.ui;

import com.bruynhuis.galago.ui.button.TouchButton;
import com.bruynhuis.galago.ui.effect.TouchEffect;
import com.bruynhuis.galago.ui.panel.Panel;

/**
 *
 * @author Nidebruyn
 */
public class ButtonThick extends TouchButton {
    
    private static float scale = 0.6f;

    public ButtonThick(Panel panel, String id, String text) {
        super(panel, id, "Interface/button-thick.png", 284*scale, 116*scale, true);
        setText(text);
        setFontSize(42*scale);
        addEffect(new TouchEffect(this));

    }
    
}
