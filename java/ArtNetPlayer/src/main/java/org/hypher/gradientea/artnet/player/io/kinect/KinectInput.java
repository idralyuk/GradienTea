package org.hypher.gradientea.artnet.player.io.kinect;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.Context;
import org.OpenNI.DepthGenerator;
import org.OpenNI.DepthMetaData;
import org.OpenNI.GeneralException;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.OutArg;
import org.OpenNI.Point3D;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.ScriptNode;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;

import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class KinectInput {
	private static KinectInput instance;

	public static KinectInput instance() {
		if (instance == null) {
			instance = new KinectInput();
		}

		return instance;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private OutArg<ScriptNode> scriptNode;
	private Context context;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private float histogram[];
	private String calibPose = null;
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

	private int width, height;

	private boolean kinectEnabled;

	public KinectInput()
	{
		try {
			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(getClass().getResource("/OpenNIConfig.xml").getFile(), scriptNode);

			depthGen = DepthGenerator.create(context);
			DepthMetaData depthMD = depthGen.getMetaData();

			histogram = new float[10000];
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();

			userGen = UserGenerator.create(context);
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();

			userGen.getNewUserEvent().addObserver(new NewUserObserver());
			userGen.getLostUserEvent().addObserver(new LostUserObserver());
			skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());
			poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver());

			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();

			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			context.startGeneratingAll();
			kinectEnabled = true;
		} catch (GeneralException e) {
			kinectEnabled = false;
			System.err.println("Failed to init kinect!");
			e.printStackTrace();
		}
	}

	public void calcHist(ShortBuffer depth)
	{
		// reset
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		depth.rewind();

		int points = 0;
		while(depth.remaining() > 0)
		{
			short depthVal = depth.get();
			if (depthVal != 0)
			{
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++)
		{
			histogram[i] += histogram[i-1];
		}

		if (points > 0)
		{
			for (int i = 1; i < histogram.length; i++)
			{
				histogram[i] = 1.0f - (histogram[i] / (float)points);
			}
		}
	}

	public void getJoint(int user, SkeletonJoint joint) throws StatusException
	{
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
		if (pos.getPosition().getZ() != 0)
		{
			joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
		}
		else
		{
			joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
		}
	}

	public void getJoints(int user) throws StatusException
	{
		getJoint(user, SkeletonJoint.HEAD);
		getJoint(user, SkeletonJoint.NECK);

		getJoint(user, SkeletonJoint.LEFT_SHOULDER);
		getJoint(user, SkeletonJoint.LEFT_ELBOW);
		getJoint(user, SkeletonJoint.LEFT_HAND);

		getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
		getJoint(user, SkeletonJoint.RIGHT_ELBOW);
		getJoint(user, SkeletonJoint.RIGHT_HAND);

		getJoint(user, SkeletonJoint.TORSO);

//		getJoint(user, SkeletonJoint.LEFT_HIP);
//		getJoint(user, SkeletonJoint.LEFT_KNEE);
//		getJoint(user, SkeletonJoint.LEFT_FOOT);
//
//		getJoint(user, SkeletonJoint.RIGHT_HIP);
//		getJoint(user, SkeletonJoint.RIGHT_KNEE);
//		getJoint(user, SkeletonJoint.RIGHT_FOOT);
	}

	public DepthGenerator getDepthGen() {
		return depthGen;
	}

	public UserGenerator getUserGen() {
		return userGen;
	}

	public SkeletonCapability getSkeletonCap() {
		return skeletonCap;
	}

	public PoseDetectionCapability getPoseDetectionCap() {
		return poseDetectionCap;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public String getCalibPose() {
		return calibPose;
	}

	public float[] getHistogram() {
		return histogram;
	}

	public boolean isKinectEnabled() {
		return kinectEnabled;
	}

	public HashMap<SkeletonJoint, SkeletonJointPosition> getUserJoints(int userId) {
		try {
			getJoints(userId);
		} catch (StatusException e) {
			System.err.println("Failed to get joints for user " + userId + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
		return joints.get(userId);
	}

	public void waitAnyUpdateAll() {
		try {
			context.waitAnyUpdateAll();
		} catch (StatusException e) {
			System.err.println("Update/Wait failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	class NewUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
			UserEventArgs args)
		{
			System.out.println("New user " + args.getId());
			try
			{
				if (skeletonCap.needPoseForCalibration())
				{
					poseDetectionCap.startPoseDetection(calibPose, args.getId());
				}
				else
				{
					skeletonCap.requestSkeletonCalibration(args.getId(), true);
				}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	class LostUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
			UserEventArgs args)
		{
			System.out.println("Lost user " + args.getId());
			joints.remove(args.getId());
		}
	}

	class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
	{
		@Override
		public void update(IObservable<CalibrationProgressEventArgs> observable,
			CalibrationProgressEventArgs args)
		{
			System.out.println("Calibraion complete: " + args.getStatus());
			try
			{
				if (args.getStatus() == CalibrationProgressStatus.OK)
				{
					System.out.println("starting tracking "  +args.getUser());
					skeletonCap.startTracking(args.getUser());
					joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
				}
				else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT)
				{
					if (skeletonCap.needPoseForCalibration())
					{
						poseDetectionCap.startPoseDetection(calibPose, args.getUser());
					}
					else
					{
						skeletonCap.requestSkeletonCalibration(args.getUser(), true);
					}
				}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs>
	{
		@Override
		public void update(IObservable<PoseDetectionEventArgs> observable,
			PoseDetectionEventArgs args)
		{
			System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
			try
			{
				poseDetectionCap.stopPoseDetection(args.getUser());
				skeletonCap.requestSkeletonCalibration(args.getUser(), true);
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
}
