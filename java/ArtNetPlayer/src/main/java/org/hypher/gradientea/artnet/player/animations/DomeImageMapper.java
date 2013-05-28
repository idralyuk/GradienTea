package org.hypher.gradientea.artnet.player.animations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeoVector3;
import org.hypher.gradientea.geometry.shared.GeodesicSphereGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.math.GeoPolarVector2;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.*;

/**
* @author Yona Appletree (yona@concentricsky.com)
*/
class DomeImageMapper {
	public final static int POLYGON_SPACE_SIZE = 100;
	private static ExecutorService executor = Executors.newCachedThreadPool();

	private GradienTeaDomeGeometry geometry;
	private List<GeoFace> lightedFaces;
	private short[][] polySpaceMask = new short[POLYGON_SPACE_SIZE][POLYGON_SPACE_SIZE];
	private Map<GeoFace,Polygon> facePolygonMap = Maps.newHashMap();

	DomeImageMapper(final GradienTeaDomeGeometry geometry) {
		this.geometry = geometry;
		lightedFaces = ImmutableList.copyOf(geometry.getLightedFaces());

		buildPixelFaceMap();
	}

	private void buildPixelFaceMap() {
		for (GeoFace face : lightedFaces) {
			double[] a = mercator(face.getA());
			double[] b = mercator(face.getB());
			double[] c = mercator(face.getC());

			facePolygonMap.put(
				face,
				new Polygon(
					new int[] {
						normalToPoly(a[0]*1+0.5),
						normalToPoly(b[0]*1+0.5),
						normalToPoly(c[0]*1+0.5)
					},
					new int[] {
						normalToPoly(a[1]+0.5),
						normalToPoly(b[1]+0.5),
						normalToPoly(c[1]+0.5)
					},
					3
				)
			);
		}

		for (int x=0; x<POLYGON_SPACE_SIZE; x++) {
			yLoop:
			for (int y=0; y<POLYGON_SPACE_SIZE; y++) {
				for (int i=0; i<lightedFaces.size(); i++) {
					if (facePolygonMap.get(lightedFaces.get(i)).contains(x, y)) {
						polySpaceMask[x][y] = (short) i;
						continue yLoop;
					}

					polySpaceMask[x][y] = -1;
				}
			}
		}
	}

	public void drawImage(
		final BufferedImage image,
		final DomePixelCanvas canvas
	) {
		final int[][] faceRgbSums = new int[lightedFaces.size()][4];

		final int[] pixelRgb = new int[3];
		final Raster data = image.getData();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		for (int x=0; x<POLYGON_SPACE_SIZE; x++) {
			for (int y=0; y<POLYGON_SPACE_SIZE; y++) {
				int faceIndex = polySpaceMask[x][y];

				if (faceIndex >= 0) {
					data.getPixel((int)polyToScaled(x, imageWidth), (int)polyToScaled(y, imageHeight), pixelRgb);
					faceRgbSums[faceIndex][0] += pixelRgb[0];
					faceRgbSums[faceIndex][1] += pixelRgb[1];
					faceRgbSums[faceIndex][2] += pixelRgb[2];
					faceRgbSums[faceIndex][3] ++;
				}
			}
		}

		for (int i=0; i<lightedFaces.size(); i++) {
			if (faceRgbSums[i][3] > 0) {
				canvas.draw(
					lightedFaces.get(i),
					new RgbColor(
						faceRgbSums[i][0] / faceRgbSums[i][3],
						faceRgbSums[i][1] / faceRgbSums[i][3],
						faceRgbSums[i][2] / faceRgbSums[i][3]
					)
				);
			}
		}
	}

	public void drawMask(final Graphics2D g, final int x, final int y, final int width, final int height) {
		g.setColor(Color.gray);
		Composite oldComposite = g.getComposite();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f));

		for (Polygon originalPolygon : facePolygonMap.values()) {
			Polygon imageSpacePolygon = new Polygon(
				new int[] {
					x + (int) polyToScaled(originalPolygon.xpoints[0], width),
					x + (int) polyToScaled(originalPolygon.xpoints[1], width),
					x + (int) polyToScaled(originalPolygon.xpoints[2], width),
				},
				new int[] {
					y + (int) polyToScaled(originalPolygon.ypoints[0], height),
					y + (int) polyToScaled(originalPolygon.ypoints[1], height),
					y + (int) polyToScaled(originalPolygon.ypoints[2], height),
				},

				3
			);

			g.drawPolygon(imageSpacePolygon);
		}

		g.setComposite(oldComposite);
	}

	private int normalToPoly(double v) {
		return (int) (v * POLYGON_SPACE_SIZE);
	}

	private double polyToNormal(int i) {
		return (double) i / POLYGON_SPACE_SIZE;
	}

	private double polyToScaled(int i, double scale) {
		return ((double)i / POLYGON_SPACE_SIZE) * scale;
	}

	private double[] mercator(GeoVector3 point) {
		GeoPolarVector2 polarPoint = point.toPolar();

		final GeoPolarVector2 topVertex = GeodesicSphereGeometry.topVertex.toPolar();
		double theta0 = 0;
		double phi0 = PI/2;

		double[] result = new double[2];

		double theta = polarPoint.getTheta();
		double phi = polarPoint.getPhi();

		// From http://mathworld.wolfram.com/AzimuthalEquidistantProjection.html
		// and http://stackoverflow.com/questions/11945814/formulas-in-azimuthal-equidistant-projection
		double c = acos(sin(phi0)*sin(phi) + cos(phi0)*cos(phi)*cos(theta-theta0));
		double kPrime = c / sin(c);
		if (Double.isNaN(kPrime)) kPrime = 0;

		result[0] = kPrime * cos(phi) * sin(theta-theta0);
		result[1] = kPrime * (cos(phi0)*sin(phi)-sin(phi0)*cos(phi)*cos(theta-theta0));

		result[0] /= Math.PI;
		result[1] /= Math.PI;

//		result[0] = point.getX()*0.4;
//		result[1] = point.getY()*0.4;

		return result;
	}
}
