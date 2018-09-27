package racketeer;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PVector;
import racketeer.kinect.Camera;

@SuppressWarnings("serial")
public class KinectTest extends PApplet {
	
	private Camera camera;
	
	@Override
	public void setup() {
		size(1524, 768);
		
		camera = new Camera(this);
		if (!camera.init(true, this)) {
			System.err.println("Failed to initialize camera");
			exit();
		}
		
		ellipseMode(CENTER);
		
	}
	
	@Override
	public void draw() {
		background(0);
		
		// Update camera
		camera.update();
		
		image(camera.getRgbImage(), 550, 20);
		
		noFill();
		
		int rx = 20;
		int ry = 20;
		int rWidth = 500;
		int rHeight = 700;
		stroke(255, 255, 255);
		rect(rx, ry, rWidth, rHeight);
		
		SimpleOpenNI ctx = camera.getContext();
		int[] users = ctx.getUsers();
		for (int i = 0; i < users.length; i++) {
			PVector pos = camera.getRealWorldPosition(users[i], SimpleOpenNI.SKEL_TORSO);
			System.out.println(users[i] + " > " + pos);
			
			float x = PApplet.map(pos.x, 30, 550, ry, rWidth);
			float y = PApplet.map(pos.z, 4500, 940, ry, rHeight);
			
			noStroke();
			fill(255, 0, 0, 125);
			ellipse(x, y, 20, 20);
		}
		
	}
	
	public void onNewUser(int userId) {
		System.out.println("onNewUser - userId: " + userId);
		
//		if (userId != 1) {
//			return;
//		}
		
		// Start tracking user as new player
		camera.getContext().requestCalibrationSkeleton(userId, true);
		camera.getContext().startTrackingSkeleton(userId);
		
	}

	public void onLostUser(int userId) {
		System.out.println("onLostUser - userId: " + userId);
	}

	
	public static void main(String[] args) {
		String[] appletArgs = {KinectTest.class.getName()};
		PApplet.main(appletArgs);
	}

}
