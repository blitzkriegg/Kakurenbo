package mygame;

import com.jme3.font.BitmapText;

/**
 * The StopWatch class is used as a timer.
 * @author Carl
 */
public class StopWatch {
    
    private long second;
    private  long minute;
    private long time;

    /**
     * The default constructor initializes the time to be 3 minutes.
     */
    public StopWatch(){
        second = 60;
        minute = 2;
        time = 0;
    }
    
    /**
     * Returns the number of seconds remaining in the stopwatch. 
     * @return the value of seconds.
     */
  public long getSecond(){
      return second;
  }  
  
  /**
   * Returns the number of minutes remaining in the stopwatch.
   * @return the value of minutes.
   */
  public long getMinute(){
      return minute;
  }
  
  /**
   * Adjusts the time remaining in the HUD.
   * @param hudText2 the HUD where the timer is going to be placed.
   * @param time the time when an action was done.
   * @return 1-if time has reached more than 3 minutes. 
   * @return 0-if time has not reached 3 minutes.
   */
  public int getTime(BitmapText hudText2,long time){
    this.time = time;
    if(time != 0){
        long milli = System.currentTimeMillis();
        hudText2.setLocalTranslation(955, 720, 1000);
        minute = 2 - ((milli-time)/ 60000) % 60;
        second = 60 - ((milli - time)/1000)%60;
    
        if(minute >= 0) {
            if(second >= 10) {
                if(second == 60){
                    hudText2.setText(minute+":" + "00" );        
                }
                else if(second < 60){
                    hudText2.setText(minute+":" + second );
                }
            }
            else {
                hudText2.setText(minute+":" + "0"+second );  
            }
        }
        else if(minute < 0){
            return 1;
        }
    }
  return 0;
  }
}