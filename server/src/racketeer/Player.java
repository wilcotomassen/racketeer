package racketeer;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PVector;
import racketeer.kinect.Camera;

public class Player {
	
	private Camera camera;
	private int kinectUserId = -1;
	
	public final static int TRACKING_JOINT = SimpleOpenNI.SKEL_TORSO;
	
	public Player(Camera camera) {
		this.camera = camera;
	}
	
	public PVector getWorldPos() {
		return camera.getRealWorldPosition(kinectUserId, TRACKING_JOINT);
	}
	
	public boolean isAvailable() {
		return (kinectUserId != -1);
	}
	
	public int getKinectUserId() {
		return kinectUserId;
	}
	
	public void assignKinectUserId(int userId) {
		kinectUserId = userId;
	}
	
	public void revokeKinectUserId() {
		kinectUserId = -1;
	}
	
	/**
	 * Get angle in degrees from player A to player B in screen coordinates from [0, 360]
	 * 12 o'clock is 0/360, 6 o'clock is 180
	 * 
	 * @param playerA
	 * @param playerB
	 * @return
	 */
	public float getAngleToOtherPlayer(Player player) {
		
		
		// Get player positions
		PVector playerPosA = getWorldPos();
		PVector playerPosB = player.getWorldPos();
		
		return ServerApplication.getAngle(playerPosA, playerPosB);
//		
//		
//		System.out.println(playerPosA);
//		System.out.println(playerPosB);
//		
//		// Get angle to Player B from Player A, mapped to screen coords where
//		// 12 o'clock is 0, everything CW goes up to 180, everything CCW goes up to -180 
//		float a = PApplet.atan2(playerPosB.x - playerPosA.x, -(playerPosB.z - playerPosA.z));
//		
//		// Convert radians to degrees
//		float deg = PApplet.degrees(a); 
//		
//		// Map to 0 (12 o'clock) to 360 degrees (12 o'clock again)
//		return ((deg + 360) % 360);
		
	}
	
}
