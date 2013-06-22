package org.hypher.gradientea.artnet.player.controller.programs;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;
import org.hypher.gradientea.artnet.player.animations.DomeImageMapper;
import org.hypher.gradientea.artnet.player.io.kinect.KinectInput;
import org.hypher.gradientea.geometry.shared.GeoVector3;

import java.util.HashMap;

/**
 * Program that does nothing, just a blank dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MotionControlProgram extends BaseDomeProgram {
	private long lastUserSeenAt = 0;
	private KinectInput kinectInput;

	public MotionControlProgram() {
		super(ProgramId.MOTION);
	}

	@Override
	protected void initialize() {
		kinectInput = KinectInput.instance();
	}

	@Override
	public void start() {

	}

	@Override
	public void update() {
		try {
			int[] users = kinectInput.getUserGen().getUsers();
			for (int i = 0; i < users.length; ++i)
			{
				int userId = users[i];
				if (kinectInput.getSkeletonCap().isSkeletonTracking(userId)) {
					final HashMap<SkeletonJoint,SkeletonJointPosition> userJoints = kinectInput.getUserJoints(userId);

					final SkeletonJointPosition leftHand = userJoints.get(SkeletonJoint.LEFT_HAND);
					final SkeletonJointPosition rightHand = userJoints.get(SkeletonJoint.RIGHT_HAND);
					final SkeletonJointPosition torso = userJoints.get(SkeletonJoint.TORSO);

					if (leftHand.getConfidence() > 0 && rightHand.getConfidence() > 0 && torso.getConfidence() > 0) {
						final Point3D leftHandWorldCoord = kinectInput.getDepthGen().convertRealWorldToProjective(leftHand.getPosition());
						final Point3D rightHandWorldCoord = kinectInput.getDepthGen().convertRealWorldToProjective(rightHand.getPosition());
						final Point3D torsoWorldCoord = torso.getPosition();

						System.out.println("User " + userId + ": left=" + str(leftHandWorldCoord) + ", right=" + str(rightHandWorldCoord));

						double[] leftHand2d = DomeImageMapper.mercator(normal(torsoWorldCoord, leftHandWorldCoord));
						double[] rightHand2d = DomeImageMapper.mercator(normal(torsoWorldCoord, rightHandWorldCoord));

						fluidCanvas().emitDirectional(
							.5f + (float) leftHand2d[0], .5f + (float) leftHand2d[1],
							0f, .5f,
							.2f,
							.01f,
							20
						);

						fluidCanvas().emitDirectional(
							.5f + (float) rightHand2d[0], .5f + (float) rightHand2d[1],
							1.0f, .5f,
							.8f,
							.01f,
							20
						);
					}
				}
			}
		} catch (StatusException e) {}
	}

	private GeoVector3 normal(final Point3D reference, final Point3D coord) {
		return new GeoVector3(
			coord.getX() - reference.getX(),
			coord.getY() - reference.getY(),
			coord.getZ() - reference.getZ()
		).normalize();
	}

	private String str(final Point3D point) {
		return "(" + point.getX() + ", " + point.getY() + ", " + point.getZ() + ")";
	}

	@Override
	public boolean isFocusDesired() {
		try {
			int[] users = kinectInput.getUserGen().getUsers();
			for (int i = 0; i < users.length; ++i)
			{
				int userId = users[i];
				if (kinectInput.getSkeletonCap().isSkeletonTracking(userId)) {
					final HashMap<SkeletonJoint,SkeletonJointPosition> userJoints = kinectInput.getUserJoints(userId);

					if (userJoints.get(SkeletonJoint.LEFT_HAND).getConfidence() > 0) {
						lastUserSeenAt = now();
						break;
					}
				}
			}
		} catch (StatusException e) {}

		return (now() - lastUserSeenAt) < 2000;
	}


	@Override
	public void stop() {
	}
}
