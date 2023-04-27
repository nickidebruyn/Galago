/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.app;

import aurelienribon.tweenengine.Tween;
import com.bruynhuis.galago.control.tween.RigidbodyAccessor;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

/**
 * If you wish to make a full 3D game then this call must be extended.
 * You also need to tell the class if you intend to use physics.
 * The physics engine used will be jBullet.
 * 
 * @author nidebruyn
 */
public abstract class Base3DApplication extends BaseApplication {
    
    protected BulletAppState bulletAppState;
    protected float frustumSize = 10;

    public Base3DApplication(String title, float width, float height, String gameSaveFileName, String gameFont, String splashImage, boolean resizable) {
        super(title, width, height, gameSaveFileName, gameFont, splashImage, resizable);
    }
    
    public Base3DApplication(String title, float width, float height, String gameSaveFileName, String gameFont, String splashImage, boolean resizable, float widthSample, float heightSample) {
        super(title, width, height, gameSaveFileName, gameFont, splashImage, resizable, widthSample, heightSample);
    }

    @Override
    public void simpleInitApp() {
        if (isPhysicsEnabled()) {
            Tween.registerAccessor(RigidBodyControl.class, new RigidbodyAccessor());
        }
        
        super.simpleInitApp(); 
    }
    
    @Override
    protected void initPhysics() {        
        //Don't load if it already exist
        if (bulletAppState != null) {
            return;
        }
        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
//        bulletAppState.getPhysicsSpace().setAccuracy(1f/80f);
//        bulletAppState.getPhysicsSpace().setMaxSubSteps(2);
    }
    
    public BulletAppState getBulletAppState() {
        return bulletAppState;
    }

    @Override
    public void showDebuging() {
        if (bulletAppState != null) {
            bulletAppState.setDebugEnabled(true);
        }
    }

    public void setOrthographicProjection(float frustumSize) {
        this.frustumSize = frustumSize;
        cam.setParallelProjection(true);
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustum(-500, 500, -aspect * frustumSize, aspect * frustumSize, frustumSize, -frustumSize);
        cam.setLocation(new Vector3f(0, 0, 0));
    }
}
