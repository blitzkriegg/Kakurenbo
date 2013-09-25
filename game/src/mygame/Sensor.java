/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;

/**
 *
 * @author Carl
 */
public class Sensor {
    private  Spatial Ball[];
    private  Geometry bal;
    private CharacterControl Player;
  
    
    public Sensor(Spatial Ball[], Geometry bal,CharacterControl Player){
        this.bal = bal;
        this.Player =Player;
        this.Ball = Ball;    
    }


  
    
    private void alert(int ndx){
           CollisionResults results = new CollisionResults();
        BoundingVolume bv = geom1.getWorldBound();
        golem.collideWith(bv, results);

        if (results.size() > 0) {
            geom1.getMaterial().setColor("Color", ColorRGBA.Red);
        }else{
            geom1.getMaterial().setColor("Color", ColorRGBA.Blue);
        }
        
    }
       
    public void updateLocations(Vector3f playerLocation, Vector3f objLocations[]){
        float distance;
        this.playerLocation = playerLocation;
        System.arraycopy(objLocations, 0, this.objLocations, 0, objLocations.length); 
        for(int i = 0; i < objLocations.length; i++) {
            distance =FastMath.sqrt(FastMath.sqr(objLocations[i].x-playerLocation.x)
                    + FastMath.sqr(objLocations[i].y-playerLocation.y) 
                    + FastMath.sqr(objLocations[i].z-playerLocation.z));
            if(distance < 50){    // change the 50 to any value para sa distance
                alert(i);
            }            
        }
    }

    
    
}
