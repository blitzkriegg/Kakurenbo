package mygame;


import tutorial.MyStartScreen;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.audio.AudioNode;
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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.util.Random;

/**
 * The Kakurenbu class is basically the game class.
 * @author Carl
 */
public class Kakurenbu extends SimpleApplication implements ActionListener{
    private ParticleEmitter emit;
    private static Box box;
    private Node sceneModel;
    private BulletAppState bulletAppState;
    private BulletAppState bulletAppState2;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f(0,0,0);
    private boolean left = false, right = false, up = false, down = false;
    private Sensor Sense; 
    private int count;
    private long time;
    
    static Kakurenbu app = new Kakurenbu();

    /**
     * The main method is where the app is run.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024, 718);
        
        app.setShowSettings(false); 
        app.setSettings(settings);
        app.start();
    }
    private static final Sphere sphere;
    private Material stone_mat;
    private RigidBodyControl ball_phy[];
    private RigidBodyControl ball_ph;
    static {
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
    } 
    private CharacterControl character;
    private Spatial model;
    private Geometry  bal;
    private  Spatial ball[];
    private MyStartScreen startScreen, endScreen;
    private float angle;
    private AudioNode audio_gun;
    private AudioNode audio_nature;
    private AudioNode audio;
    private BitmapText hudText;
    private boolean a,b,c =false;
    private BitmapText hudText2;
    private long second;
    private StopWatch Stop;
    
    /**
     * Initializes the assets(scenes, models, etc.) of the game.
     */
    @Override
    public void simpleInitApp() {      
        setDisplayFps(false);
        setDisplayStatView(false);
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        b = false;
        Stop = new StopWatch();
        initBalls();
        setUpKeys();
        setUpLight();
        initScene();
        initPlayer();
        initMaterials();
        initCrossHairs();
        initAudio() ;
        initAudioGun();
        initStartScreen();
        initHUD();
        initHUD2(); 
        
    }
    /**
     * This method is used to initialize the lighting of the game.
     */
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
     * 
    * @param binding the String equivalent of a key binding
    * @param value whether there is an action done
    * @param tpf We use the tpf variable ("time per frame") to time this action depending on the current frames per second rate. To check any actions done in the said duration.
    * 
    */
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
            flyCam.setDragToRotate(false);
            player.jump();
            if(b == false) {
                time = System.currentTimeMillis();
                b=true; 
            } 
        }   
        else if(binding.equals("shoot") && !value && count!=5){  
            audio_gun.playInstance();  
            bal = makeCannonBall();   
            if(bal!=null) {
                initEmit(); 
                if(a==true) {
                    count++;
                }
            }
             
        }
    
    }
 

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   * 
   * @param tpf We use the tpf variable ("time per frame") to time this action depending on the current frames per second rate. Generally, it makes the actions run smoothly on fast or slow machines.
   * 
   */
  @Override
    public void simpleUpdate(float tpf) {
        int i;
        hudText.setText("Numbers of balls found: "+count+"/5");   
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.negate()); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (down)  { walkDirection.addLocal(camDir.negate()); }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        if(bal != null){
            count =  Sense.alert(ball,bal,player,emit,count);
        }
       
        if(count ==5 && c==false){
            sceneModel.detachAllChildren();
            guiNode.detachAllChildren();
            rootNode.detachAllChildren();
            audio.stop();
            if(count ==5 && c==false) {
                c=true;
                endScreen();
            }
        }
        if(c==false) {
            i=Stop.getTime(hudText2,time);
            if(i==1&&b==true){
                endScreen();
            }
        }
    }
  
    /**
     * This method will invoke the end game screen(UI).
     */
    public void endScreen(){
        endScreen = new MyStartScreen();
        stateManager.attach(endScreen);
     
        /**
         * Ã…ctivate the Nifty-JME integration: 
        */
        app.start();
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
            assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/tutorial/endScreen.xml", "end", endScreen);
        hudText2 = new BitmapText(guiFont, false);          
        hudText2.setSize(23);      // font size
        hudText2.setColor(ColorRGBA.White);                             // font color
        String finText;
        if (count==5){
            finText="Game Clear!";
        }
        else{
            finText="Game Over!";
        }
        long minute=2-Stop.getMinute();
        long second=60-Stop.getSecond();
        hudText2.setText("     "+finText+   "\n"
                       + "         Results       \n" 
                       + "    Balls Found: "+count+"\n"
                       + " Elapsed Time:"+minute+":"+second);                       // the text
        hudText2.setLocalTranslation(hudText2.getLineWidth()+240,400, 1000);// position
        guiNode.attachChild(hudText2);
        
        //nifty.setDebugOptionPanelColors(true);
    
        rootNode.attachChild(sceneModel);
        flyCam.setDragToRotate(true);    
   }
    
    /**
     * This method initializes the textures needed for the game.(scene texture)
     */
    public void initMaterials() {
        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);
    }
    
    /**
     * Create a cannon ball geometry and attach to scene graph.
     * @return returns the newly made ball. 
     */
    public Geometry makeCannonBall() {
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
        ball_ph.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        ball_ph.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        /** Accelerate the physcial ball to shoot it. */
        ball_ph.setLinearVelocity(cam.getDirection().mult(50));
        ball_ph.setAngularFactor(0);
        return ball_geo;
    }
    
    
    /** 
     * A plus sign used as crosshairs to help the player with aiming.
     */
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
    
    private void initAudio() {
        /** Initializes the background music. */
        audio = new AudioNode(assetManager,"Sounds/1.ogg", false);
        audio.setPositional(false);
        audio.setLooping(true);
        audio.setVolume(2);
        rootNode.attachChild(audio);
        audio.play();
    }
    
    private void initAudioGun() {
        /** gun shot sound is to be triggered by a mouse click. */
        audio_gun = new AudioNode(assetManager, "Sound/Effects/Gun.wav", false);
        audio_gun.setPositional(false);
        audio_gun.setLooping(false);
        audio_gun.setVolume(2);
        rootNode.attachChild(audio_gun);
    }
    
    /**
     * Initializes a particle to stay in the scene to indicate a ball was found.
     */
    private void initEmit() {
        emit = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 300);
        emit.setGravity(0, 0, 0);
        emit.setVelocityVariation(1);
        emit.setLowLife(1);
        emit.setHighLife(1);
        emit.setInitialVelocity(new Vector3f(0, .5f, 0));
        emit.setImagesX(15);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
        emit.setMaterial(mat);
        rootNode.attachChild(emit);
    }
    
    /**
     * Initializes the player.
     */
    private void initPlayer() {
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        player.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        player.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
        // We attach the scene and the player to the rootNode and the physics space,
        // to make them appear in the game world.
    }
    
    /**
     * Initializes the Starting Screen(UI) when the game is executed.
     */
    private void initStartScreen() {
        startScreen = new MyStartScreen();
        stateManager.attach(startScreen);
     
        /**
         * Ã…ctivate the Nifty-JME integration: 
        */
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
            assetManager, inputManager, audioRenderer, guiViewPort);
        Nifty nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);
        nifty.fromXml("Interface/tutorial/startScreen.xml", "start", startScreen);
        
        //nifty.setDebugOptionPanelColors(true);
    
        
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
        startScreen.onStartScreen();
        flyCam.setDragToRotate(true);
    }
    
    /**
     * Initializes the scene(background) of the game.
     */
    private void initScene() {
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(100);
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = (Node) assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);
        CollisionShape sceneShape =CollisionShapeFactory.createMeshShape((Node) sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        Sense = new Sensor(sceneModel);
    }
    
    /**
     * Initializes the balls that the player must find within the world.
     */
    private void initBalls() {
        ball = new Spatial[5];
        ball_phy = new RigidBodyControl[5];
        count =0;
        Random rand;
        float sig1=1;
        rand = new Random();
        for(int i =0 ;i<5;i++) {
           
            if (!rand.nextBoolean()){
                    sig1=sig1*-1;
            }
           
            ball_phy[i] = new RigidBodyControl(1f);     
            ball[i]= assetManager.loadModel("Models/kakurenbu ball/kakurenbu ball.j3o");
            ball[i].scale(0.05f, 0.05f, 0.05f);
           
            Material mat_lit = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat_lit.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
            mat_lit.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
            mat_lit.setBoolean("UseMaterialColors",true);    
            mat_lit.setColor("Specular",ColorRGBA.White);
            mat_lit.setColor("Diffuse",ColorRGBA.White);
            mat_lit.setFloat("Shininess", 5f); // [0,128]
            ball[i].setMaterial(mat_lit);
            
            ball[i].setLocalTranslation(rand.nextFloat()*300,100f,(float)rand.nextFloat()*100*sig1);
            ball[i].addControl(ball_phy[i]);
   
            ball_phy[i].setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
            bulletAppState.getPhysicsSpace().add(ball_phy[i]);
  
            rootNode.attachChild(ball[i]);
        }
    }
    
    /**
     * Initializes the HUD as the ball counter.
     */
    private void initHUD() {
        hudText = new BitmapText(guiFont, false);          
        hudText.setSize(20);                                            // font size
        hudText.setColor(ColorRGBA.White);                              // font color
        hudText.setText("Numbers of balls found: 0/5");                 // the text
        hudText.setLocalTranslation(0, hudText.getLineHeight(), 1000);  // position
        guiNode.attachChild(hudText);
    }
    
    /**
     * Initializes the HUD in the starting screen.
     */
    private void initHUD2() {
        hudText2 = new BitmapText(guiFont, false);          
        hudText2.setSize(28);      // font size
        hudText2.setColor(ColorRGBA.White);                             // font color
        hudText2.setText("PRESS SPACE TO BEGIN");                       // the text
        hudText2.setLocalTranslation(hudText2.getLineWidth()+65, 350, 1000);// position
        guiNode.attachChild(hudText2);
    }
}

