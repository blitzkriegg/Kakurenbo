package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * The Sensor class is used to sense any collisions within the world.
 * @author Carl
 */
public class Sensor {
    private Node sceneModel;
    /**
     * Initializes a new Sensor object and attaches it to the scene.
     * @param sceneModel the scene used in the game.
     */
    public Sensor(Node sceneModel){ 
     this.sceneModel = sceneModel;
    }
      

  /**
   * Checks if the player has found a ball. 
   * 
   * Returns the new number of count which is the number of balls found.
   * 
   * @param Ball the array of balls. 
   * @param bal the bounding volume.
   * @param Player the player.
   * @param emit a particle.
   * @param count number of balls currently found.
   * @return the value of count after being evaluated.
   */
    
    public int alert(Spatial Ball[], Geometry bal,CharacterControl Player,ParticleEmitter emit,int count){
      boolean a = false;
      int j;
         CollisionResults results = new CollisionResults(),results2 = new CollisionResults();
        BoundingVolume bv = bal.getWorldBound(),bv2 = bal.getWorldBound();
        for(int i=0;i< Ball.length && a==false;i++){
        Ball[i].collideWith(bv, results);
        if (results.size() > 0) {
            a = true;
                bal.removeFromParent();
                Ball[i].removeFromParent();
                emit.removeFromParent();
                Ball[i].setLocalTranslation(100f, 100f, 100f);
         count++;
        }    
   
      }
     sceneModel.collideWith(bv2, results2);
            emit.setLocalTranslation(bal.getLocalTranslation());
       if(results2.size() > 0) {
            emit.removeFromParent();
            bal.removeFromParent();
            
    }
        return count;

}
}
