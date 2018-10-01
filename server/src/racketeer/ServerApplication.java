package racketeer;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
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
	
	public final int RW_MIN_X = 609;
	public final int RW_MAX_X = 45;
	public final int RW_MIN_Z = 1000;
	public final int RW_MAX_Z = 4300;
	
	private Racket racket;
	private Camera camera;
	
	private Player hero;
	private Player opponent;
	
	private PVector ballPos = new PVector(FIELD_X, FIELD_Y);
	
	// Graphics
	private PFont font;
	private PImage background;
	
	// Trails
	public final int HISTORY_POINT_COUNT = 60;
	
	private ArrayList<PVector> ballHistory = new ArrayList<PVector>();
	private ArrayList<PVector> heroHistory = new ArrayList<PVector>();
	private ArrayList<PVector> opponentHistory = new ArrayList<PVector>();

	@Override
	public void setup() {
		
		// Applet config
		size(1824, 740);
		
		frameRate(60);
		
		// Load resources
		font = createFont("C:\\Users\\Wilco\\Projects\\sensorlab\\videowall\\videowall-server\\data\\Square721BT-BoldExtended.otf", 16);
		textFont(font);
		background = loadImage("C:\\Users\\Wilco\\Projects\\sensorlab\\videowall\\videowall-server\\data\\background.png");
		
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
		background(background);
		
//		// Failsafe when Kinect doesn't work 
//		handleOverRide();
//		return;
		
		// Update ball position
		if (mouseX >= FIELD_X && mouseX <= FIELD_X + FIELD_WIDTH
			&& mouseY >= FIELD_Y && mouseX <= FIELD_Y + FIELD_HEIGHT) {
			ballPos.x = mouseX;
			ballPos.y = mouseY;
		}
		ballHistory.add(new PVector(ballPos.x, ballPos.y));
		if (ballHistory.size() > HISTORY_POINT_COUNT) {
			ballHistory.remove(0);
		}

		// Update camera
		camera.update();
		image(camera.getRgbImage(), FIELD_X + FIELD_WIDTH + 120, 160, 580, 400);
		
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
			
			// Update/draw history
			heroHistory.add(mapRealworldToScreenPos(hero.getWorldPos()));
			if (heroHistory.size() > HISTORY_POINT_COUNT) {
				heroHistory.remove(0);
			}
			drawHistory(heroHistory, 97, 97, 255);
			
			// Draw user
			drawUser(hero, color(97, 97, 255), "PLAYER 1");
		}
		if (opponent.isAvailable()) {
			
			// Update/draw history
			opponentHistory.add(mapRealworldToScreenPos(opponent.getWorldPos()));
			if (opponentHistory.size() > HISTORY_POINT_COUNT) {
				opponentHistory.remove(0);
			}
			drawHistory(opponentHistory, 255, 0, 0);
			
			// Draw user
			drawUser(opponent, color(255, 0, 0), "PLAYER 2");
		}
		
		// Draw ball
		drawHistory(ballHistory, 0, 255, 0);
		noStroke();
		fill(0, 255, 0);
		ellipse(ballPos.x, ballPos.y, 10, 10);
		
		textAlign(LEFT, CENTER);
		textSize(20);
		text("BALL", ballPos.x + 20, ballPos.y);
		textSize(10);
		PVector ballWorldPos = mapScreenToRealworldPos(ballPos);
		text(String.format("[%.2f, %.2f]", ballWorldPos.x, ballWorldPos.z), ballPos.x + 20, ballPos.y + 20);
		
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
		
		// Draw angles on screen
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
	
	private void drawUser(Player player, int color, String name) {
		PVector worldPos = player.getWorldPos();
		PVector pos = mapRealworldToScreenPos(worldPos);
		noStroke();
		fill(color);
		ellipse(pos.x, pos.y, 20, 20);
		
		textAlign(LEFT, CENTER);
		textSize(20);
		text(name, pos.x + 20, pos.y);
		textSize(10);
		text(String.format("[%.2f, %.2f]", worldPos.x, worldPos.z), pos.x + 20, pos.y + 20);
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
		
		pushMatrix();
		
		translate(1500, 360);
		rotate(+HALF_PI);
		
		// Hero
		noStroke();
		fill(97, 97, 255);
		ellipse(0, 0, 100, 100);
		noFill();
		
		// Opponent
		stroke(255);
		strokeWeight(0.5f);
		ellipse(0, 0, 300, 300);
		
		strokeWeight(12);
		stroke(255, 0, 0);
		arc(0, 0, 300, 300, radians(playerAngle - 30), radians(playerAngle + 30));
		
		// Ball
		stroke(255);
		strokeWeight(0.5f);
		ellipse(0, 0, 400, 400);
		
		strokeWeight(12);
		stroke(0, 255, 0);
		arc(0, 0, 400, 400, radians(ballAngle - 10), radians(ballAngle + 10));
		
		popMatrix();
	}
	
	private void handleWaitUpdate() {
		// Draw info
		String debugText = !hero.isAvailable() ? "Waiting for Player 1" : "Waiting for Player 2";
		fill(255);
		textSize(30);
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

	private void drawHistory(ArrayList<PVector> history, int c1, int c2, int c3) {
		noFill();
		strokeWeight(4);
		for (int i = history.size()-2; i > 0; i--) {
			stroke(c1, c2, c3, map(i, history.size(), 0, 255, 10));
			PVector historyPoint = history.get(i);
			PVector historyPoint2 = history.get(i - 1);
			line(historyPoint.x, historyPoint.y, historyPoint2.x, historyPoint2.y);
		}
	}
	
}
