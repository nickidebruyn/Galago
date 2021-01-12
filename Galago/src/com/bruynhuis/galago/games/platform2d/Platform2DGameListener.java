/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.games.platform2d;

import com.jme3.scene.Spatial;

/**
 *
 * @author nidebruyn
 */
public interface Platform2DGameListener {
    
    public void doGameOver();
    
    public void doGameCompleted();
    
    public void doScoreChanged(int score);
    
    public void doCollisionPlayerWithTerrain(Spatial collided, Spatial collider);
    
    public void doCollisionPlayerWithStatic(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithStatic(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithTerrain(Spatial collided, Spatial collider);
    
    public void doCollisionPlayerWithPickup(Spatial collided, Spatial collider);
    
    public void doCollisionPlayerWithEnemy(Spatial collided, Spatial collider);
    
    public void doCollisionPlayerWithBullet(Spatial collided, Spatial collider);
    
    public void doCollisionObstacleWithBullet(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithBullet(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithEnemy(Spatial collided, Spatial collider);
    
    public void doCollisionPlayerWithObstacle(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithObstacle(Spatial collided, Spatial collider);
    
    public void doCollisionEnemyWithPickup(Spatial collided, Spatial collider);
    
    public void doCollisionBulletWithPickup(Spatial collided, Spatial collider);
    
    public void doCollisionTerrainWithBullet(Spatial collided, Spatial collider);
    
}
