package racketeer.kinect;

import SimpleOpenNI.SimpleOpenNI;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class Camera {

	private PApplet applet;
	private SimpleOpenNI kinect;

	public Camera(PApplet applet) {
		this.applet = applet;
	}

	public boolean init(boolean mirrorImage, Object userManager) {

		kinect = new SimpleOpenNI(applet, SimpleOpenNI.RUN_MODE_SINGLE_THREADED);

		if (kinect.isInit()) {

			// Setup Kinect
			kinect.setMirror(mirrorImage);
			kinect.enableDepth();
			kinect.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL, userManager); // User manager >
			// https://forum.processing.org/two/discussion/315/kinect-sdk-v1-7-simpleopenni-1-96-with-simpleopenni-0-27-examples-occured-a-lot-of-errors-help
			// kinect.enableIR();
			kinect.enableRGB();

			return true;

		} else {
			System.err.println("Failed to init Kinect SimpleOpenNI camera; maybe the camera is not connected!");
			return false;
		}

	}
	
	public PImage getIrImage() {
		return kinect.irImage();
	}

	public PImage getRgbImage() {
		return kinect.rgbImage();
	}

	public SimpleOpenNI getContext() {
		return kinect;
	}
	
	public void update() {

		kinect.update();

		int[] userList = kinect.getUsers();
		for (int i = 0; i < userList.length; i++) {
			int userId = userList[i];
//			System.out.println(userId);
//			if (kinect.isTrackingSkeleton(userId)) {
//				System.out.println("> T");
//			}
		}

	}

	public PVector getRealWorldPosition(int userId, int joint) {

		// Fetch joint position
		PVector pos = new PVector();
		kinect.getJointPositionSkeleton(userId, joint, pos);

		// Map to real world position
		PVector projectedPos = new PVector();
		kinect.convertRealWorldToProjective(pos, projectedPos);

		return projectedPos;

	}

	/**
	 * Draw skeleton
	 * 
	 * @param userId
	 */
	public void drawSkeleton(PGraphics g, int userId) {

		g.strokeWeight(5);
		g.stroke(255, 255, 255);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_TORSO);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT);

		kinect.drawLimb(userId, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE);
		kinect.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT);

	}

}
