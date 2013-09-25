package mygame;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimEventListener;
import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.scene.Mesh;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.lessvoid.nifty.Nifty;
import tutorial.MyStartScreen;


/** Sample 3 - how to load an OBJ model, and OgreXML model, 
 * a material/texture, or text. */
public class Kakurenbu extends SimpleApplication implements ActionListener{
    private static Box box;
    
    private int health;
    private MyStartScreen startScreen;
    private Node sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f(0,0,0);
    private boolean left = false, right = false, up = false, down = false;
    private Sensor Sense; 
    
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 768);
        Kakurenbu app = new Kakurenbu();
        app.setShowSettings(false); // splashscreen
        app.setSettings(settings);
        app.start();
    }
    private static final Sphere sphere;
    private Material stone_mat;
    private RigidBodyControl ball_phy[];
    private RigidBodyControl ball_ph;
    private AnimChannel animationChannel;
    private AnimChannel attackChannel;
    private AnimControl animationControl;
    static {
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
    }
    private CharacterControl character;
    private Spatial model;
    private ChaseCamera chaseCam;
    private Mesh floor;
    private Material floor_mat;
    private RigidBodyControl floor_phy;
    private Geometry  bal;
    private  Spatial ball[];

    @Override
    public void simpleInitApp() {
 
        setDisplayFps(false);
        setDisplayStatView(false);
 
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        ball = new Spatial[5];
        ball_phy = new RigidBodyControl[5];
        
        for(int i =0 ;i<5;i++) {
            ball_phy[i] = new RigidBodyControl(15f);     
            ball[i]= assetManager.loadModel("Models/kakurenbu ball/kakurenbu ball.j3o");
            ball[i].scale(0.05f, 0.05f, 0.05f);
            ball[i].setLocalTranslation(60.0f, 5.0f, -20.0f);
            ball[i].addControl(ball_phy[i]);
            bulletAppState.getPhysicsSpace().add(ball_phy[i]);
            rootNode.attachChild(ball[i]);
        }
        /** Set up Physics */
      
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        setUpKeys();
        setUpLight();

        // We load the scene from the zip file and adjust its size.
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = (Node) assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);
  

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
            CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
   
          


        // We attach the scene and the player to the rootNode and the physics space,
        // to make them appear in the game world.
   
        initMaterials();
        initCrossHairs();
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
        
        startScreen = new MyStartScreen();
        stateManager.attach(startScreen);

        /**
         * Åctivate the Nifty-JME integration: 
        */
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
            assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/tutorial/screen3.xml", "start", startScreen);
        //nifty.setDebugOptionPanelColors(true);
    
        flyCam.setDragToRotate(true); // you need the mouse for clicking now
    }
    
    private void setUpLight() {
    // We add light so we see the scene
    AmbientLight al = new AmbientLight();
    al.setColor(ColorRGBA.White.mult(1.3f));
    rootNode.addLight(al);

    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White);
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
    rootNode.addLight(dl);
  }

  /** We over-write some navigational key mappings here, so we can
   * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("shoot",new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "shoot"); 
    }

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
        left = value;
        } 
        else if (binding.equals("Right")) {
            right = value;
        } 
        else if (binding.equals("Up")) {
            up = value;
        } 
        else if (binding.equals("Down")) {
            down = value;
        } 
        else if (binding.equals("Jump")) {
            player.jump();
        }
        else if(binding.equals("shoot") && !value){  
          bal = makeCannonBall();   
        }
    }
 

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
  @Override
  public void simpleUpdate(float tpf) {
    Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
    Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
    walkDirection.set(0, 0, 0);
    if (left)  { walkDirection.addLocal(camLeft); }
    if (right) { walkDirection.addLocal(camLeft.negate()); }
    if (up)    { walkDirection.addLocal(camDir); }
    if (down)  { walkDirection.addLocal(camDir.negate()); }
    player.setWalkDirection(walkDirection);
    cam.setLocation(player.getPhysicsLocation()); 
  }
  
  public void initMaterials() {
    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
    key2.setGenerateMips(true);
    Texture tex2 = assetManager.loadTexture(key2);
    stone_mat.setTexture("ColorMap", tex2);
  }


   public Geometry makeCannonBall() {
    /** Create a cannon ball geometry and attach to scene graph. */
    Geometry ball_geo = new Geometry("cannon ball", sphere);

    ball_geo.setMaterial(stone_mat);
     ball_geo.getMaterial().setColor("Color", ColorRGBA.Blue);
    rootNode.attachChild(ball_geo);
    /** Position the cannon ball  */
    ball_geo.setLocalTranslation(cam.getLocation());
    /** Make the ball physcial with a mass > 0.0f */
    ball_ph = new RigidBodyControl(.25f);
    /** Add physical ball to physics space. */
    ball_geo.addControl(ball_ph);
    bulletAppState.getPhysicsSpace().add(ball_ph);
    /** Accelerate the physcial ball to shoot it. */
    ball_ph.setLinearVelocity(cam.getDirection().mult(100));
    
    return ball_geo;

  }

  /** A plus sign used as crosshairs to help the player with aiming.*/
  protected void initCrossHairs() {
    guiNode.detachAllChildren();
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
      settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
    guiNode.attachChild(ch);
  }

}

