package racketeer;
import processing.core.PApplet;
import processing.core.PVector;
import racketeer.kinect.Camera;

public class ServerApplication extends PApplet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String RACKET_HOST = "192.168.1.86";
	public static final int RACKET_PORT = 8888;
	
	public final int FIELD_X = 20;
	public final int FIELD_Y = 20;
	public final int FIELD_WIDTH = 500;
	public final int FIELD_HEIGHT = 700;
	
	public final int RW_MIN_X = 30;
	public final int RW_MAX_X = 550;
	public final int RW_MIN_Z = 940;
	public final int RW_MAX_Z = 4000;
	
	private Racket racket;
	private Camera camera;
	
	private Player hero;
	private Player opponent;
	
	private PVector ballPos = new PVector(FIELD_X, FIELD_Y);

	@Override
	public void setup() {
		
		// Applet config
		size(1824, 740);
		
		// Init racket
		racket = new Racket(RACKET_HOST, RACKET_PORT);
		
		// Init camera
		camera = new Camera(this);
		if (!camera.init(true, this)) {
			System.err.println("Failed to initialize camera");
			exit();
		}
		
		// Init player objects
		hero = new Player(camera);
		opponent = new Player(camera);
		
		// Set drawing setup
		ellipseMode(CENTER);
		smooth();
		
	}
	
	@Override
	public void draw() {
		background(0);
		
//		// Failsafe when Kinect doesn't work 
//		handleOverRide();
//		return;
		
		// Update ball position
		if (mouseX >= FIELD_X && mouseX <= FIELD_X + FIELD_WIDTH
			&& mouseY >= FIELD_Y && mouseX <= FIELD_Y + FIELD_HEIGHT) {
			ballPos.x = mouseX;
			ballPos.y = mouseY;
		}

		// Update camera
		camera.update();
		image(camera.getRgbImage(), FIELD_X + FIELD_WIDTH + 60, 120);
		
		// Draw playing field
		strokeWeight(3);
		noFill();
		stroke(255, 255, 255);
		rect(FIELD_X, FIELD_Y, FIELD_WIDTH, FIELD_HEIGHT);
		line(FIELD_X, FIELD_Y + FIELD_HEIGHT / 2, FIELD_X + FIELD_WIDTH, FIELD_Y + FIELD_HEIGHT / 2);
		line(FIELD_X + FIELD_WIDTH / 2, FIELD_Y + FIELD_HEIGHT / 2, FIELD_X + FIELD_WIDTH / 2, FIELD_Y + FIELD_HEIGHT);
		
		// Handle app state
		if (hero.isAvailable() && opponent.isAvailable()) {
			handleGameUpdate();
		} else {
			handleWaitUpdate();
			drawRacket(180, 180);
		}
		
		if (hero.isAvailable()) {
			drawUser(hero, color(0, 0, 255));
		}
		if (opponent.isAvailable()) {
			drawUser(opponent, color(255, 0, 0));
		}
		
		// Draw ball
		noStroke();
		fill(255);
		ellipse(ballPos.x, ballPos.y, 10, 10);
		
	}
	
	private void handleOverRide() {
		racket.updatePositions(
		map(mouseX, 0f, (float) width, 0f, 1f), 
		map(mouseY, 0f, (float) height, 0f, 1f));
	}
	
	private void handleGameUpdate() {
		
		// Get angle from hero to opponent
		float opponentAngle = hero.getAngleToOtherPlayer(opponent);
		
		// Get angle from hero to ball
		float ballAngle = getAngle(hero.getWorldPos(), mapScreenToRealworldPos(ballPos));
		
		// Update racket
		racket.updatePositions(
			map(opponentAngle, 0f, 360f, 0f, 1f), 
			map(ballAngle, 0f, 360f, 0f, 1f));
		
		// TODO : draw angles on screen
		drawRacket(opponentAngle, ballAngle);
		
	}
	
	/**
	 * Get angle in degrees from pos A to pos B in screen coordinates from [0, 360]
	 * 12 o'clock is 0/360, 6 o'clock is 180
	 * 
	 * @param posA
	 * @param posB
	 * @return
	 */
	public static float getAngle(PVector posA, PVector posB) {
		
		// Get angle to Player B from Player A, mapped to screen coords where
		// 12 o'clock is 0, everything CW goes up to 180, everything CCW goes up to -180 
		float a = PApplet.atan2(posB.x - posA.x, -(posB.z - posA.z));
		
		// Convert radians to degrees
		float deg = PApplet.degrees(a); 
		
		// Map to 0 (12 o'clock) to 360 degrees (12 o'clock again)
		return ((deg + 360) % 360);
		
	}
	
	private void drawUser(Player player, int color) {
		PVector pos = mapRealworldToScreenPos(player.getWorldPos());
		noStroke();
		fill(color);
		ellipse(pos.x, pos.y, 20, 20);
	}
	
	/**
	 * Map real world (Kinect) position to screen field position. 
	 * Will map RW x to screen x and RW z to screen y
	 *  
	 * @param realWorldPos
	 * @return
	 */
	private PVector mapRealworldToScreenPos(PVector realWorldPos) {
		return new PVector(
			PApplet.map(realWorldPos.x, RW_MIN_X, RW_MAX_X, FIELD_X, FIELD_X + FIELD_WIDTH),
			PApplet.map(realWorldPos.z, RW_MAX_Z, RW_MIN_Z, FIELD_Y, FIELD_Y + FIELD_HEIGHT)
		);
	}
	
	private PVector mapScreenToRealworldPos(PVector screenPos) {
		return new PVector(
			PApplet.map(screenPos.x, FIELD_X, FIELD_X + FIELD_WIDTH, RW_MAX_X, RW_MIN_X),
			0,
			PApplet.map(screenPos.y, FIELD_Y, FIELD_Y + FIELD_HEIGHT, RW_MAX_Z, RW_MIN_Z)
		);
	}
	
	/**
	 * Draw simulated racket light
	 * 
	 * @param playerAngle
	 * @param ballAngle
	 */
	private void drawRacket(float playerAngle, float ballAngle) {
		noFill();
		pushMatrix();
		
		translate(1500, 360);
		rotate(+HALF_PI);
		
		// Player
		stroke(255);
		strokeWeight(0.5f);
		ellipse(0, 0, 400, 400);
		
		strokeWeight(12);
		stroke(255, 0, 0);
		arc(0, 0, 400, 400, radians(playerAngle - 30), radians(playerAngle + 30));
		
		// Ball
		stroke(255);
		strokeWeight(0.5f);
		ellipse(0, 0, 300, 300);
		
		strokeWeight(12);
		stroke(0, 255, 0);
		arc(0, 0, 300, 300, radians(ballAngle - 10), radians(ballAngle + 10));
		
		popMatrix();
	}
	
	private void handleWaitUpdate() {
		// Draw info
		String debugText = !hero.isAvailable() ? "Waiting for Player 1" : "Waiting for Player 2";
		fill(255);
		textSize(40);
		textAlign(CENTER);
		text(debugText, FIELD_X + FIELD_WIDTH / 2, FIELD_Y + FIELD_HEIGHT / 3);
	}
	
	public void onNewUser(int userId) {
		
		System.out.println("Detected new user: " + userId);
		
		// Skip if we already have two players
		if (hero.isAvailable() && opponent.isAvailable()) {
			return;
		}
		
		// Start tracking user as new player
		camera.getContext().requestCalibrationSkeleton(userId, true);
		camera.getContext().startTrackingSkeleton(userId);
		
		// Assign camera user id to player
		if (!hero.isAvailable()) {
			hero.assignKinectUserId(userId);
		} else {
			opponent.assignKinectUserId(userId);
		}

	}

	public void onLostUser(int userId) {
		
		System.out.println("Lost user: " + userId);
		
		if (hero.getKinectUserId() == userId) {
			hero.revokeKinectUserId();
		} else if (opponent.getKinectUserId() == userId) {
			opponent.revokeKinectUserId();
		}
	}

	public static void main(String[] args) {
		String[] appletArgs = { ServerApplication.class.getName() };
		PApplet.main(appletArgs);
	}

}
