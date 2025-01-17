/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.galago.editor;

import com.bruynhuis.galago.app.Base3DApplication;
import com.bruynhuis.galago.resource.EffectManager;
import com.bruynhuis.galago.resource.FontManager;
import com.bruynhuis.galago.resource.ModelManager;
import com.bruynhuis.galago.resource.ScreenManager;
import com.bruynhuis.galago.resource.SoundManager;
import com.bruynhuis.galago.resource.TextureManager;
import com.bruynhuis.galago.util.ColorUtils;
import com.galago.editor.screens.EditorScreen;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author NideBruyn
 */
public class MainApplication extends Base3DApplication {

    public static void main(String[] args) {

        try {
//            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            javax.swing.UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        new MainApplication();
    }

    public MainApplication() {
        super("Scene Editor", 1920, 1080, "galago-editor.save", "Interface/Fonts/SourceCodePro.fnt", null, true);
    }

    @Override
    protected void preInitApp() {
        BACKGROUND_COLOR = ColorUtils.rgb(44, 62, 80);
                
    }

    @Override
    protected void postInitApp() {
        registerInputMappings("camera-action", new MouseButtonTrigger(1));
        registerInputMappings("ctrl", new KeyTrigger(keyInput.KEY_LCONTROL));
        registerInputMappings("delete", new KeyTrigger(keyInput.KEY_DELETE));
        registerInputMappings("up_arrow", new KeyTrigger(keyInput.KEY_UP));
        registerInputMappings("down_arrow", new KeyTrigger(keyInput.KEY_DOWN));
        registerInputMappings("shift", new KeyTrigger(keyInput.KEY_LSHIFT));
        registerInputMappings("copy", new KeyTrigger(keyInput.KEY_C));
        registerInputMappings("paste", new KeyTrigger(keyInput.KEY_V));
        
        showScreen(EditorScreen.NAME);

//        showStats();
//        System.out.println(getContext().getRenderer().getCaps());
    }

    @Override
    protected boolean isPhysicsEnabled() {
        return true;
    }
//
//    @Override
//    protected void initPhysics() {
//        //Don't load if it already exist
//        if (bulletAppState != null) {
//            return;
//        }
//        /**
//         * Set up Physics
//         */
//        bulletAppState = new BulletAppState(new Vector3f(-100, 0, -100), new Vector3f(100, 100, 100));
//        stateManager.attach(bulletAppState);
////        bulletAppState.getPhysicsSpace().setAccuracy(1f/80f);
////        bulletAppState.getPhysicsSpace().setMaxSubSteps(2);
//    }

    @Override
    protected void initScreens(ScreenManager screenManager) {
        screenManager.loadScreen(EditorScreen.NAME, new EditorScreen());
//        screenManager.loadScreen(WorldEditorScreen.NAME, new WorldEditorScreen());
//        screenManager.loadScreen(JoystickConfigScreen.NAME, new JoystickConfigScreen());

    }

    @Override
    public void initModelManager(ModelManager modelManager) {
    }

    @Override
    protected void initSound(SoundManager soundManager) {
    }

    @Override
    protected void initEffect(EffectManager effectManager) {
    }

    @Override
    protected void initTextures(TextureManager textureManager) {
    }

    @Override
    protected void initFonts(FontManager fontManager) {
    }

}
