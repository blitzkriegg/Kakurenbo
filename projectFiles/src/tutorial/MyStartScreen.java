package tutorial;

import com.jme3.input.FlyByCamera;
import com.jme3.renderer.Camera;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * The MyStartScreen class is used to as a holder for the game GUI. 
 */
public class MyStartScreen extends AbstractAppState implements ScreenController  {
  
  private FlyByCamera flyCam;
          
  private Nifty nifty;
  private Application app;
  private Screen screen;
    
  /** custom methods */
  public MyStartScreen() {
    /** You custom constructor, can accept arguments */
  }
/**
 * Navigates away from the current screen and changes to the game screen.
 * @param nextScreen the name of the screen
 */
  public void startGame(String nextScreen) {
    nifty.gotoScreen(nextScreen);  // switch to another screen
  }

  /**
   * Quits the game
   */
  public void quitGame() {
    app.stop();
  }

  public String getPlayerName() {
    return System.getProperty("user.name");
  }

  /** Nifty GUI ScreenControl methods */
  public void bind(Nifty nifty, Screen screen) {
    this.nifty = nifty;
    this.screen = screen;
  }

  /**
   * Specifies what to do on the start screen.
   */
  public void onStartScreen() {
      
      
  }

  public void onEndScreen() {
  }

  /** jME3 AppState methods */
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    this.app = app;
  }

  @Override
  public void update(float tpf) {
    if (screen.getScreenId().equals("hud")) {
      Element niftyElement = nifty.getCurrentScreen().findElementByName("score");
      // Display the time-per-frame -- this field could also display the score etc...
      niftyElement.getRenderer(TextRenderer.class).setText((int)(tpf*100000) + ""); 
    }
  }
}
