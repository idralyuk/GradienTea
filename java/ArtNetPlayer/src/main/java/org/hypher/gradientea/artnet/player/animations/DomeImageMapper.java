package org.hypher.gradientea.artnet.player.animations;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.hypher.gradientea.animation.shared.color.HsbColor;
import org.hypher.gradientea.animation.shared.color.PixelColor;
import org.hypher.gradientea.animation.shared.color.RgbColor;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GeoVector3;
import org.hypher.gradientea.geometry.shared.GeodesicSphereGeometry;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;
import org.hypher.gradientea.geometry.shared.math.DomeMath;
import org.hypher.gradientea.geometry.shared.math.GeoPolarVector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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
public class DomeImageMapper {
	public final static int POLYGON_SPACE_SIZE = 100;

	private static ExecutorService executor = Executors.newCachedThreadPool();

	private GradienTeaDomeGeometry geometry;
	private List<GeoFace> lightedFaces;
	private List<GeoVector3> lightedVertices;
	private short[][] polySpaceFaceMask = new short[POLYGON_SPACE_SIZE][POLYGON_SPACE_SIZE];
	private short[][] polySpaceVertexMask = new short[POLYGON_SPACE_SIZE][POLYGON_SPACE_SIZE];

	private Map<GeoFace,Polygon> facePolygonMap = Maps.newHashMap();
	private Map<GeoVector3,Ellipse2D> vertexShapeMap = Maps.newHashMap();

	private int[][] faceRgbSums;
	private int[][] vertexRgbSums;

	private double rotationRadians = Math.PI/2.2;

	/**
	 * The amount to scale the vertex circles when drawing overlays and data
	 */
	private double vertexScaleFactor = .4;

	private double faceScaleFactor = .5;

	public DomeImageMapper(final GradienTeaDomeGeometry geometry) {
		this.geometry = geometry;
		lightedFaces = ImmutableList.copyOf(geometry.getLightedFaces());
		lightedVertices = ImmutableList.copyOf(geometry.getLightedVertices());

		buildPixelFaceMap();
	}

	public double getRotationRadians() {
		return rotationRadians;
	}

	public void setRotationRadians(final double rotationRadians) {
		this.rotationRadians = rotationRadians;

		buildPixelFaceMap();
	}

	private synchronized void buildPixelFaceMap() {
		faceRgbSums = new int[lightedFaces.size()][4];
		vertexRgbSums = new int[lightedVertices.size()][4];

		for (GeoFace face : lightedFaces) {
			double[] a = mercator(face.getA(), rotationRadians);
			double[] b = mercator(face.getB(), rotationRadians);
			double[] c = mercator(face.getC(), rotationRadians);

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

		double polySpaceVertexRadius = normalToPoly(geometry.getVertexRadius());
		for (GeoVector3 vertex : lightedVertices) {
			double[] xyVertex = mercator(vertex, rotationRadians);

			vertexShapeMap.put(
				vertex,
				new Ellipse2D.Double(
					normalToPoly(xyVertex[0]+0.5) - polySpaceVertexRadius/2,
					normalToPoly(xyVertex[1]+0.5) - polySpaceVertexRadius/2,
					polySpaceVertexRadius,
					polySpaceVertexRadius
				)
			);
		}

		for (int x=0; x<POLYGON_SPACE_SIZE; x++) {
			for (int y=0; y<POLYGON_SPACE_SIZE; y++) {

				for (int i=0; i<lightedFaces.size(); i++) {
					if (facePolygonMap.get(lightedFaces.get(i)).contains(x, y)) {
						polySpaceFaceMask[x][y] = (short) i;
						break;
					}

					polySpaceFaceMask[x][y] = -1;
				}

				for (int i=0; i<lightedVertices.size(); i++) {
					if (vertexShapeMap.get(lightedVertices.get(i)).contains(x, y)) {
						polySpaceVertexMask[x][y] = (short) i;
						break;
					}

					polySpaceVertexMask[x][y] = -1;
				}
			}
		}
	}

	public synchronized void drawImage(
		final BufferedImage image,
		final DomePixelCanvas canvas
	) {
		final int[] pixelArgb = new int[4];
		final Raster data = image.getData();

		final int imageWidth = image.getWidth();
		final int imageHeight = image.getHeight();

		zeroArray(faceRgbSums);
		zeroArray(vertexRgbSums);

		for (int x=0; x<POLYGON_SPACE_SIZE; x++) {
			for (int y=0; y<POLYGON_SPACE_SIZE; y++) {
				int faceIndex = polySpaceFaceMask[x][y];
				int vertexIndex = polySpaceVertexMask[x][y];

				if (faceIndex >= 0 || vertexIndex >= 0) {
					data.getPixel((int)polyToScaled(x, imageWidth), (int)polyToScaled(y, imageHeight), pixelArgb);
				}

				if (faceIndex >= 0) {
					faceRgbSums[faceIndex][0] += pixelArgb[0];
					faceRgbSums[faceIndex][1] += pixelArgb[1];
					faceRgbSums[faceIndex][2] += pixelArgb[2];
					faceRgbSums[faceIndex][3] ++;
				}

				if (vertexIndex >= 0) {
					vertexRgbSums[vertexIndex][0] += pixelArgb[0];
					vertexRgbSums[vertexIndex][1] += pixelArgb[1];
					vertexRgbSums[vertexIndex][2] += pixelArgb[2];
					vertexRgbSums[vertexIndex][3] ++;
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

		for (int i=0; i<lightedVertices.size(); i++) {
			if (vertexRgbSums[i][3] > 0) {
				canvas.draw(
					lightedVertices.get(i),
					new RgbColor(
						(vertexRgbSums[i][0] / vertexRgbSums[i][3]) * 2,
						(vertexRgbSums[i][1] / vertexRgbSums[i][3]) * 2,
						(vertexRgbSums[i][2] / vertexRgbSums[i][3]) * 2
					)
				);
			}
		}
	}

	private void zeroArray(final int[][] array) {
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[i].length; j++) {
				array[i][j] = 0;
			}
		}
	}

	Cache<String, BufferedImage> overlayCache = CacheBuilder.newBuilder()
		.maximumSize(10)
		.build();

	private final static int maskSizeIntervalPx = 25;

	public void drawMask(
		final Graphics2D g2,
		int x,
		int y,
		int drawWidth,
		int drawHeight,
		boolean drawLabels,
		boolean drawVertices
	) {
		// Snap the given width and height to the next integer that is divisible by maskSizeIntervalPx
		int imageWidth = (drawWidth + maskSizeIntervalPx - drawWidth % maskSizeIntervalPx);
		int imageHeight = (drawHeight + maskSizeIntervalPx - drawHeight % maskSizeIntervalPx);

		String key = imageWidth + "," + imageHeight + "," + drawLabels + "," + drawVertices;
		BufferedImage overlayImage = overlayCache.getIfPresent(key);

		if (overlayImage == null) {
			overlayImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
			overlayCache.put(key, overlayImage);

			Graphics2D imageG = (Graphics2D) overlayImage.createGraphics();

			imageG.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON
			);

			Composite oldComposite = imageG.getComposite();
	//		g.setColor(Color.white);
	//		g.fillRect(x, y, width, height);


			// Hack to make it prettier
	//		y += 15;
	//		height -= 15;
	//
	//		x += 15;
	//		width -= 15;

			imageG.setFont(new Font("Arial", Font.BOLD, 12));
			imageG.setStroke(new BasicStroke(2));
			imageG.setColor(new Color(1f, 1f, 1f, .4f));

			final FontMetrics fontMetrics = imageG.getFontMetrics();
			int textHeight = fontMetrics.getHeight();

			for (Map.Entry<GeoFace, Polygon> entry : facePolygonMap.entrySet()) {
				Polygon originalPolygon = entry.getValue();
				Polygon imageSpacePolygon = new Polygon(
					new int[] {
						x + (int) polyToScaled(originalPolygon.xpoints[0], imageWidth),
						x + (int) polyToScaled(originalPolygon.xpoints[1], imageWidth),
						x + (int) polyToScaled(originalPolygon.xpoints[2], imageWidth),
					},
					new int[] {
						y + (int) polyToScaled(originalPolygon.ypoints[0], imageHeight),
						y + (int) polyToScaled(originalPolygon.ypoints[1], imageHeight),
						y + (int) polyToScaled(originalPolygon.ypoints[2], imageHeight),
					},

					3
				);

				final String numberStr = String.valueOf(lightedFaces.indexOf(entry.getKey()));
				int textWidth = fontMetrics.charsWidth(numberStr.toCharArray(), 0, numberStr.length());

				final int centerX =
					(imageSpacePolygon.xpoints[0] + imageSpacePolygon.xpoints[1] + imageSpacePolygon.xpoints[2]) / 3;
				final int centerY =
					(imageSpacePolygon.ypoints[0] + imageSpacePolygon.ypoints[1] + imageSpacePolygon.ypoints[2]) / 3;

				if (drawLabels) {
					imageG.drawString(
						numberStr,
						centerX - textWidth/2,
						centerY + textHeight/2
					);
				}

				imageG.draw(
					translateAndScale(imageSpacePolygon, centerX, centerY, faceScaleFactor)
				);
			}

			if (drawVertices) {
				for (Map.Entry<GeoVector3, Ellipse2D> entry : vertexShapeMap.entrySet()) {
					Ellipse2D polySpaceCircle = entry.getValue();
					Ellipse2D imageSpaceCircle = new Ellipse2D.Double(
						x + polyToScaled(polySpaceCircle.getX(), imageWidth),
						y + polyToScaled(polySpaceCircle.getY(), imageHeight),
						polyToScaled(polySpaceCircle.getWidth(), imageWidth),
						polyToScaled(polySpaceCircle.getWidth(), imageHeight)
					);

					final String numberStr = String.valueOf(lightedVertices.indexOf(entry.getKey()));
					int textWidth = fontMetrics.charsWidth(numberStr.toCharArray(), 0, numberStr.length());

//					if (drawLabels) {
//						Color olderColor = imageG.getColor();
//						imageG.setColor(Color.black);
//						imageG.fill(imageSpaceCircle);
//						imageG.setColor(olderColor);
//					}

					imageG.draw(translateAndScale(imageSpaceCircle, imageSpaceCircle.getCenterX(), imageSpaceCircle.getCenterY(), vertexScaleFactor));

					if (drawLabels) {
						imageG.drawString(
							numberStr,
							(int) (imageSpaceCircle.getCenterX() - textWidth / 2),
							(int) (imageSpaceCircle.getCenterY() + textHeight / 2)
						);
					}
				}
			}

			imageG.setComposite(oldComposite);
		}

		g2.drawImage(overlayImage, x, y, drawWidth, drawHeight, null);
	}

	private Shape translateAndScale(final Shape shape, final double centerX, final double centerY, double scaleFactor) {
		return
			AffineTransform.getTranslateInstance(centerX, centerY).createTransformedShape(
				AffineTransform.getScaleInstance(scaleFactor, scaleFactor).createTransformedShape(
					AffineTransform.getTranslateInstance(-centerX, -centerY).createTransformedShape(shape)
				)
			);
	}

	public void drawPanelState(
		DomePixelCanvas pixelCanvas,
		final Graphics2D g2,
		int x,
		int y,
		int drawWidth,
		int drawHeight,
		boolean drawVertices,
		float opacity
	) {
		final Map<GeoFace, PixelColor> faceColorMap = pixelCanvas.getFaceColorMap();
		final Map<GeoVector3, PixelColor> vertexColorMap = pixelCanvas.getVertexColorMap();

		for (Map.Entry<GeoFace, Polygon> entry : facePolygonMap.entrySet()) {
			final PixelColor pixelColor = faceColorMap.get(entry.getKey());

			if (pixelColor != null && !pixelColor.isBlack()) {
				Polygon originalPolygon = entry.getValue();
				Polygon imageSpacePolygon = new Polygon(
					new int[] {
						x + (int) polyToScaled(originalPolygon.xpoints[0], drawWidth),
						x + (int) polyToScaled(originalPolygon.xpoints[1], drawWidth),
						x + (int) polyToScaled(originalPolygon.xpoints[2], drawWidth),
					},
					new int[] {
						y + (int) polyToScaled(originalPolygon.ypoints[0], drawHeight),
						y + (int) polyToScaled(originalPolygon.ypoints[1], drawHeight),
						y + (int) polyToScaled(originalPolygon.ypoints[2], drawHeight),
					},

					3
				);

				final int centerX =
					(imageSpacePolygon.xpoints[0] + imageSpacePolygon.xpoints[1] + imageSpacePolygon.xpoints[2]) / 3;
				final int centerY =
					(imageSpacePolygon.ypoints[0] + imageSpacePolygon.ypoints[1] + imageSpacePolygon.ypoints[2]) / 3;


				g2.setColor(fillColorFor(pixelColor, opacity*3));
				g2.fill(translateAndScale(imageSpacePolygon, centerX, centerY, faceScaleFactor));
			}
		}

		if (drawVertices) {
			for (Map.Entry<GeoVector3, Ellipse2D> entry : vertexShapeMap.entrySet()) {
				final PixelColor pixelColor = vertexColorMap.get(entry.getKey());
				if (pixelColor != null && !pixelColor.isBlack()) {
					Ellipse2D polySpaceCircle = entry.getValue();
					final double ellipsePxWidth = polyToScaled(polySpaceCircle.getWidth(), drawWidth);
					final double ellipsePxHeight = polyToScaled(polySpaceCircle.getWidth(), drawHeight);

					Ellipse2D imageSpaceCircle = new Ellipse2D.Double(
						x + polyToScaled(polySpaceCircle.getX(), drawWidth),
						y + polyToScaled(polySpaceCircle.getY(), drawHeight),
						ellipsePxWidth,
						ellipsePxHeight
					);

					g2.setColor(fillColorFor(pixelColor, opacity));
					g2.fill(translateAndScale(imageSpaceCircle, imageSpaceCircle.getCenterX(), imageSpaceCircle.getCenterY(), vertexScaleFactor));
				}
			}
		}
	}

	private Color fillColorFor(PixelColor color, final float opacity) {
		if (color == null) {
			return new Color(0,0,0,0);
		} else {
			HsbColor baseColor = HsbColor.hsbColor(color);
			int[] fullBrightness = baseColor.withBrightness(1.0).asRgb();

			return new Color(
				fullBrightness[0], fullBrightness[1], fullBrightness[2],
				DomeMath.clip(0, 255, (int) (baseColor.getBrightness() * 255 * opacity))
			);
		}
	}

	private int normalToPoly(double v) {
		return (int) (v * POLYGON_SPACE_SIZE);
	}

	private double polyToNormal(double i) {
		return i / POLYGON_SPACE_SIZE;
	}

	private double polyToScaled(double i, double scale) {
		return (i / POLYGON_SPACE_SIZE) * scale;
	}

	public static double[] mercator(GeoVector3 point, double rotationRadians) {
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

		result[0] *= -1;

//		result[0] = point.getX()*0.4;
//		result[1] = point.getY()*0.4;

		// Now rotate

		rotate(result, rotationRadians);
		return result;
	}

	private static void rotate(final double[] result, final double rotationRadians) {
		double cx = 0, cy = 0;

		double s = sin(rotationRadians);
		double c = cos(rotationRadians);

		result[0] -= cx;
		result[1] -= cy;

		double newX = result[0] * c - result[1] * s;
		double newY = result[0] * s + result[1] * c;

		result[0] = newX + cx;
		result[1] = newY + cy;
	}
}
