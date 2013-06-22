package org.hypher.gradientea.artnet.player.controller.programs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;
import org.hypher.gradientea.artnet.player.controller.OscConstants;
import org.hypher.gradientea.artnet.player.io.kinect.KinectInput;
import org.hypher.gradientea.artnet.player.io.osc.OscHelper;
import org.hypher.gradientea.geometry.shared.GeoVector3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hypher.gradientea.geometry.shared.math.DomeMath.f;

/**
 * Program that does nothing, just a blank dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class MotionControlProgram extends BaseDomeProgram {
	public static final SkeletonJoint[] TRACKED_JOINTS = new SkeletonJoint[]{
		SkeletonJoint.LEFT_HAND, SkeletonJoint.RIGHT_HAND
	};

	private long lastUserSeenAt = 0;
	private KinectInput kinectInput;
	private Map<Integer, Map<SkeletonJoint, JointMotion>> userJointMotionMap = Maps.newHashMap();

	private OscHelper.OscBoolean oscEnabled = OscHelper.booleanValue(OscConstants.Control.Motion.ENABLED, true);

	private OscHelper.OscXY oscOffset = OscHelper.xyValue(OscConstants.Control.Motion.OFFSET, .7, .3);
	private OscHelper.OscDouble oscScale = OscHelper.doubleValue(OscConstants.Control.Motion.SCALE, .1, 1.5, .3);

	private OscHelper.OscDouble oscVelocity = OscHelper.doubleValue(OscConstants.Control.Motion.VELOCITY, 0, 1, .3);
	private OscHelper.OscDouble oscIntensity = OscHelper.doubleValue(OscConstants.Control.Motion.INTENSITY, 0, 1, .3);

	private OscHelper.OscDouble oscCutoff = OscHelper.doubleValue(OscConstants.Control.Motion.CUTOFF, 0.001, 0.05, .008);
	private OscHelper.OscDouble oscColorRotation = OscHelper.doubleValue(OscConstants.Control.Motion.COLOR_ROTATION, 0.01, 0.3, .1);

	private Map<SkeletonJoint, OscHelper.OscBoolean> oscJointEnabledMap = ImmutableMap.of(
		SkeletonJoint.LEFT_HAND, OscHelper.booleanValue(OscConstants.Control.Motion.ENABLE_LEFT_HAND, true),
		SkeletonJoint.RIGHT_HAND, OscHelper.booleanValue(OscConstants.Control.Motion.ENABLE_RIGHT_HAND, true)
	);

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
		updateMotionMap();

		for (Integer userId : userJointMotionMap.keySet()) {
			final Map<SkeletonJoint, JointMotion> jointMotionMap = userJointMotionMap.get(userId);

			for (SkeletonJoint joint : jointMotionMap.keySet()) {
				final JointMotion jointMotion = jointMotionMap.get(joint);

				if (oscJointEnabledMap.containsKey(joint) && oscJointEnabledMap.get(joint).value()) {
					if (jointMotion.getVelocity() > oscCutoff.getValue()) {
						fluidCanvas().emitDirectional(
							f(oscOffset.getX()) + (jointMotion.getX() - .5f) * oscScale.floatValue(),
							f(oscOffset.getY()) + (jointMotion.getY() - .5f) * oscScale.floatValue(),
							f(jointMotion.getAngle()),
							f(jointMotion.getTotalDistance() * oscColorRotation.getValue() * (jointMotion.hashCode() % 100f) / 100f),
							f(jointMotion.getVelocity() * 2 * oscVelocity.getValue()),
							f(jointMotion.getVelocity() * 1000 * oscIntensity.getValue())
						);
					}
				}
			}
		}
	}

	private void updateMotionMap() {
		try {
			int[] users = kinectInput.getUserGen().getUsers();
			for (int i = 0; i < users.length; ++i)
			{
				int userId = users[i];
				if (kinectInput.getSkeletonCap().isSkeletonTracking(userId)) {
					final HashMap<SkeletonJoint,SkeletonJointPosition> userJoints = kinectInput.getUserJoints(userId);

					for (SkeletonJoint joint : TRACKED_JOINTS) {
						updateJointMotion(userId, userJoints, joint);
					}
				}
			}
		} catch (StatusException e) {}

		// Cull any old entries
		for (Iterator<Integer> userI = userJointMotionMap.keySet().iterator(); userI.hasNext();) {
			int userId = userI.next();
			final Map<SkeletonJoint, JointMotion> jointMap = userJointMotionMap.get(userId);

			for (Iterator<SkeletonJoint> jointI = jointMap.keySet().iterator(); jointI.hasNext();) {
				SkeletonJoint joint = jointI.next();
				if (! jointMap.get(joint).isActive()) {
					jointI.remove();
				}
			}

			if (jointMap.isEmpty()) {
				userI.remove();
			}
		}
	}

	private void updateJointMotion(
		final int userId,
		final HashMap<SkeletonJoint, SkeletonJointPosition> userJointMap,
		final SkeletonJoint joint
	) throws StatusException {
		final SkeletonJointPosition jointPosition = userJointMap.get(joint);

		if (jointPosition.getConfidence() > 0) {
			Point3D jointPos = jointPosition.getPosition();

			if (! userJointMotionMap.containsKey(userId)) {
				userJointMotionMap.put(userId, new HashMap<SkeletonJoint, JointMotion>());
			}

			final Map<SkeletonJoint, JointMotion> userJointMotionMap = this.userJointMotionMap.get(userId);
			if (! userJointMotionMap.containsKey(joint)) {
				userJointMotionMap.put(joint, new JointMotion());
			}

			userJointMotionMap.get(joint).updatePos(
				jointPos.getX() / 640f,
				jointPos.getY() / 480f
			);
		} else {
			// Remove the entry if it exists
			if (userJointMotionMap.containsKey(userId)) {
				userJointMotionMap.get(userId).remove(joint);
			}
		}
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
		if (oscEnabled.value()) {
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
		} else {
			return false;
		}
	}


	@Override
	public void stop() {
	}

	public static class JointMotion {
		long lastMotionAt = 0;

		float lastX = 0;
		float lastY = 0;

		float x;
		float y;
		private double totalDistance;

		public void updatePos(float newX, float newY) {
			lastMotionAt = System.currentTimeMillis();

			this.lastX = this.x;
			this.lastY = this.y;

			this.x = newX;
			this.y = newY;

			totalDistance += getVelocity();
		}

		public float getLastX() {
			return lastX;
		}

		public float getLastY() {
			return lastY;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

		public boolean isActive() {
			return (System.currentTimeMillis() - lastMotionAt) < 2000;
		}

		public double getDeltaX() {
			return x - lastX;
		}

		public double getDeltaY() {
			return y - lastY;
		}

		public double getAngle() {
			return Math.atan2(getDeltaY(), getDeltaX());
		}

		public double getVelocity() {
			return Math.sqrt(Math.pow(getDeltaX(),2) + Math.pow(getDeltaY(),2));
		}

		public double getTotalDistance() {
			return totalDistance;
		}
	}
}
