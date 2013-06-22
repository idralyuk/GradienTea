package org.hypher.gradientea.artnet.player.io.kinect;

import org.OpenNI.DepthMetaData;
import org.OpenNI.Point3D;
import org.OpenNI.SceneMetaData;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.StatusException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class KinectDisplay extends Component {
	private KinectInput kinectInput = KinectInput.instance();

	private boolean drawBackground = false;
	private boolean drawPixels = true;
	private boolean drawSkeleton = true;
	private boolean printID = true;
	private boolean printState = true;

	private byte[] imgbytes;

	private BufferedImage bimg;

	private int kinectDataWidth, kinectDataHeight;

	public KinectDisplay() {
		kinectDataWidth = kinectInput.getWidth();
		kinectDataHeight = kinectInput.getHeight();

		imgbytes = new byte[kinectDataWidth * kinectDataHeight *3];
	}

	Color colors[] = {Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE, Color.CYAN};
	public void paint(Graphics g)
	{
		if (drawPixels)
		{
			if (bimg == null) {
				DataBufferByte dataBuffer = new DataBufferByte(imgbytes, kinectDataWidth * kinectDataHeight *3);

				WritableRaster raster = Raster.createInterleavedRaster(
					dataBuffer,
					kinectDataWidth,
					kinectDataHeight,
					kinectDataWidth * 3,
					3,
					new int[]{0, 1, 2},
					null
				);

				ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

				bimg = new BufferedImage(colorModel, raster, false, null);
			}

			g.drawImage(bimg, 0, 0, null);
		}

		try
		{
			int[] users = kinectInput.getUserGen().getUsers();
			for (int i = 0; i < users.length; ++i)
			{
				Color c = colors[users[i]%colors.length];
				c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

				g.setColor(c);
				if (drawSkeleton && kinectInput.getSkeletonCap().isSkeletonTracking(users[i]))
				{
					drawSkeleton(g, users[i]);
				}

				if (printID)
				{
					Point3D com = kinectInput.getDepthGen().convertRealWorldToProjective(
						kinectInput.getUserGen()
							.getUserCoM(users[i])
					);
					String label = null;
					if (!printState)
					{
						label = new String(""+users[i]);
					}
					else if (kinectInput.getSkeletonCap().isSkeletonTracking(users[i]))
					{
						// Tracking
						label = new String(users[i] + " - Tracking");
					}
					else if (kinectInput.getSkeletonCap().isSkeletonCalibrating(users[i]))
					{
						// Calibrating
						label = new String(users[i] + " - Calibrating");
					}
					else
					{
						// Nothing
						label = new String(users[i] + " - Looking for pose (" + kinectInput.getCalibPose() + ")");
					}

					g.drawString(label, (int)com.getX(), (int)com.getY());
				}
			}
		} catch (StatusException e)
		{
			e.printStackTrace();
		}
	}

	public void updateDepth()
	{
		kinectInput.waitAnyUpdateAll();

		DepthMetaData depthMD = kinectInput.getDepthGen().getMetaData();
		SceneMetaData sceneMD = kinectInput.getUserGen().getUserPixels(0);

		ShortBuffer scene = sceneMD.getData().createShortBuffer();
		ShortBuffer depth = depthMD.getData().createShortBuffer();
		kinectInput.calcHist(depth);
		depth.rewind();

		final float[] histogram = kinectInput.getHistogram();

		while(depth.remaining() > 0)
		{
			int pos = depth.position();
			short pixel = depth.get();
			short user = scene.get();

			imgbytes[3*pos] = 0;
			imgbytes[3*pos+1] = 0;
			imgbytes[3*pos+2] = 0;

			if (drawBackground || pixel != 0)
			{
				int colorID = user % (colors.length-1);
				if (user == 0)
				{
					if (drawBackground) {
						colorID = colors.length-1;
					}
					else {
						continue;
					}
				}

				if (pixel != 0)
				{
					float histValue = histogram[pixel];
					imgbytes[3*pos] = (byte)(histValue*colors[colorID].getRed());
					imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
					imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
				}
			}
		}
	}

	void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2)
	{
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();

		if (jointHash.get(joint1).getConfidence() < 0.4 || jointHash.get(joint2).getConfidence() < 0.4)
			return;

		g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
	}

	public void drawSkeleton(Graphics g, int userId) throws StatusException
	{
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = kinectInput.getUserJoints(userId);

		drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
		drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
		drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

//		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
//		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
//		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);
//
//		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
//		drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
//
//		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
//		drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
	}

	public Dimension getPreferredSize() {
		return new Dimension(kinectDataWidth, kinectDataHeight);
	}
}
