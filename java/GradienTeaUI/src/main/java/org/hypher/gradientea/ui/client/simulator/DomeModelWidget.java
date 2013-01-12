package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import org.hypher.gradientea.lightingmodel.shared.dome.DomeSpecification;
import org.hypher.gradientea.ui.client.player.DmxInterface;
import thothbot.parallax.core.client.context.Canvas3d;
import thothbot.parallax.core.client.context.Canvas3dAttributes;
import thothbot.parallax.core.client.renderers.WebGLRenderer;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.ExtrudeGeometry;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.Matrix4;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.curves.Shape;
import thothbot.parallax.core.shared.geometries.CylinderGeometry;
import thothbot.parallax.core.shared.helpers.AxisHelper;
import thothbot.parallax.core.shared.lights.Light;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.materials.HasColor;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.objects.Object3D;
import thothbot.parallax.core.shared.scenes.Scene;

import java.util.List;
import java.util.Map;

/**
 * Renders a {@link org.hypher.gradientea.lightingmodel.shared.rendering.RenderableAnimation} onto a model
 * of a geodesic dome.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomeModelWidget extends Composite implements RequiresResize, DmxInterface {

	/**
	 * Rotation speed in rotations per second
	 */
	protected double rotationsPerMinute = 2;

	protected DomeGeometry domeGeometry;

	protected DomeRenderer domeRenderer;

	protected Canvas3d canvas;

	protected LayoutPanel layout = new LayoutPanel();

	public DomeModelWidget() {
		try {
			canvas = new Canvas3d(new Canvas3dAttributes());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create 3d canvas", e);
		}

		layout.add(canvas);
		initWidget(layout);


		domeRenderer = new DomeRenderer(canvas);
	}

	public void displayDome(DomeSpecification specification) {
		domeGeometry = new DomeGeometry(specification);

		domeRenderer.renderDome(domeGeometry);
		onResize();
	}

	protected double calculateCameraRotation() {
		return ((rotationsPerMinute * Duration.currentTimeMillis()) / (60 * 1000.0))%1.0 * Math.PI * 2;
	}

	@Override
	public void onResize() {
		domeRenderer.setSize(
			layout.getElement().getClientWidth(),
			layout.getElement().getClientHeight()
		);

		domeRenderer.renderFrame(calculateCameraRotation());
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
			@Override
			public void execute(final double timestamp) {
				onResize();
			}
		});
	}

	@Override
	public void display(final int[][] dmxChannelValues) {
		int faceIndex = 0;
		List<DomeGeometry.DomeFace> faces = domeGeometry.getFaces();

		universeLoop:
		for (int u=0; u<dmxChannelValues.length && faceIndex < faces.size(); u++) {
			int[] universe = dmxChannelValues[u];

			for (int c=0; c<universe.length-3 && faceIndex < faces.size(); c += 3, faceIndex ++) {
				domeRenderer.applyFaceColor(
					faces.get(faceIndex),
					universe[c],
					universe[c+1],
					universe[c+2]
				);
			}
		}

		//domeRenderer.renderFrame(calculateCameraRotation());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	/**
	 * Renderer for domes. Each unit in the 3d space is considered to be 1 foot.
	 */
	protected class DomeRenderer {
		//
		// Reused globals
		//
		protected Vector3 origin = new Vector3();

		protected Geometry jointGeometry = createJoinGeometry(inches(3), inches(2));
		protected MeshPhongMaterial joinMaterial = ParallaxHelper.meshPhongMaterial()
			.setColor(new Color(0xCCCCCC))
			.setShininess(.8)
			.setReflectivity(.4)
			.setMetal(true)
			.get();

		protected MeshPhongMaterial strutMaterial = ParallaxHelper.meshPhongMaterial()
			.setColor(new Color(0xAAAAAA))
			.setShininess(.8)
			.setReflectivity(.4)
			.setMetal(true)
			.get();

		protected Geometry strutGeometry = createStrutGeometry(inches(1));

		protected Map<Double, Geometry> panelGeometries = Maps.newHashMap();

		//
		// Rendering objects
		//
		protected WebGLRenderer renderer;
		protected Scene scene;
		protected PerspectiveCamera camera;
		protected List<PointLight> lights;

		//
		// Dome-related meshes
		//
		private List<Mesh> joints = Lists.newArrayList();
		private Map<DomeGeometry.DomeEdge, Mesh> struts = Maps.newHashMap();
		private Map<DomeGeometry.DomeFace, Mesh> panels = Maps.newHashMap();

		//
		// Dome geometry
		//
		private DomeGeometry domeModel;

		public DomeRenderer(Canvas3d canvas) {
			renderer =  new WebGLRenderer(canvas.getGL(), 100, 100);
			renderer.setClearColor(new Color(0x000000), 1.0f);

			scene = new Scene();

			camera = new PerspectiveCamera(75.0f, 1, 1.0f, 1000.0f);
			camera.getPosition().setZ(20.0);
			camera.setUp(new Vector3(0.0, 0.0, 1.0));
			camera.lookAt(new Vector3());

			lights = ImmutableList.of(
				new PointLight(0xFFEEFF, 3, 100),
				new PointLight(0xFFEEFF, 3, 100),
				new PointLight(0xFFEEFF, 3, 100)
			);

			for (Light light : lights) {
				scene.add(light);
			}

			MeshPhongMaterial personMaterial = new MeshPhongMaterial();
			personMaterial.setColor(new Color(0xEECEB3));

			Mesh person = new Mesh(
				new CylinderGeometry(20 / 12.0, 20 / 12.0, 6.0, 20, 1, false),
				personMaterial
			);
			person.getRotation().setX(Math.PI/2);
			person.getScale().setX(0.6);
			person.getPosition().setZ(3.0);
			scene.add(person);

			scene.add(new AxisHelper());
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Instance Methods

		public void applyFaceColor(final DomeGeometry.DomeFace domeFace, int red, int green, int blue) {
			Preconditions.checkArgument(panels.containsKey(domeFace), "This model does not have a panel for " + domeFace);

			((HasColor) panels.get(domeFace).getMaterial()).getColor().setRGB(
				(double) red / 255,
				(double) green / 255,
				(double) blue / 255
			);
		}

		public void setSize(int width, int height) {
			renderer.setSize(width, height);
			camera.setAspectRatio((double) width / height);
			camera.updateProjectionMatrix();
		}

		public void renderDome(DomeGeometry geometry) {
			this.domeModel = geometry;

			clear();
			buildJoints();
			buildStruts();
			buildPanels();

			updateCameraAndLights();
		}

		public void renderFrame(double cameraRotation) {
			camera.getPosition().setX(domeGeometry.getSpec().getRadius()*2 * Math.cos(cameraRotation));
			camera.getPosition().setY(domeGeometry.getSpec().getRadius()*2 * Math.sin(cameraRotation));
			camera.lookAt(origin);

			renderer.render(scene, camera);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Setup Methods

		protected void updateCameraAndLights() {
			double radius = domeModel.getSpec().getRadius();

			camera.getPosition().setZ(radius * 0.9);
			camera.lookAt(origin);
			camera.setUp(new Vector3(0.0, 0.0, 1.0));

			for (int i=0; i<lights.size(); i++) {
				PointLight light = lights.get(i);

				light.getPosition().setZ(radius*1.5);
				light.getPosition().setX(radius*1.5*Math.cos(Math.PI*2 * ((double)i/lights.size())));
				light.getPosition().setY(radius * 1.5 * Math.sin(Math.PI * 2 * ((double) i / lights.size())));

				light.setDistance(radius*2.5);
			}
		}

		private void clear() {
			for (Object3D obj : Iterables.concat(joints, struts.values(), panels.values())) {
				scene.remove(obj);
			}

			joints.clear();
			struts.clear();
			panels.clear();
		}

		private void buildJoints() {
			for (Vector3 vertex : domeModel.getVertices()) {
				Mesh mesh = new Mesh(
					jointGeometry,
					joinMaterial
				);

				mesh.setPosition(vertex);
				mesh.lookAt(origin);
				joints.add(mesh);

				mesh.updateMatrix();
				mesh.setMatrixAutoUpdate(false);

				scene.add(mesh);
			}
		}

		private void buildStruts() {
			for (DomeGeometry.DomeEdge edge : domeModel.getEdges()) {
				Mesh mesh = createStrut(edge);

				scene.add(mesh);
				struts.put(edge, mesh);
			}
		}

		private void buildPanels() {
			for (DomeGeometry.DomeFace face : domeModel.getFaces()) {
				Mesh mesh = createPanel(face);

				scene.add(mesh);
				panels.put(face, mesh);
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Utility Methods

		protected double inches(final double count) {
			return count / 12;
		}

		protected Mesh createStrut(DomeGeometry.DomeEdge edge) {
			double distance = edge.getV1().distanceTo(edge.getV2());

			Mesh mesh = new Mesh(
				strutGeometry,
				strutMaterial
			);

			mesh.getScale().setZ(distance);
			mesh.setPosition(edge.getV1().clone());
			mesh.lookAt(edge.getV2());

			mesh.updateMatrix();
			mesh.setMatrixAutoUpdate(false);
			return mesh;
		}

		protected Mesh createPanel(DomeGeometry.DomeFace face) {
			double sideLength = domeModel.getSpec().getPanelSideLength();

			Geometry geometry = panelGeometryFor(sideLength);

			MeshBasicMaterial material = new MeshBasicMaterial();
			material.setColor(new Color(0x000000));

			Mesh mesh = new Mesh(geometry, material);
			mesh.setMatrixAutoUpdate(false);

			orientPanel(face, mesh);

			return mesh;
		}

		protected void orientPanel(DomeGeometry.DomeFace face, Mesh panel) {
			Vector3 vABmidpoint = new Vector3().add(face.getA(), face.getB()).divide(2);

			double faceSideLength = face.getA().distanceTo(face.getB());
			double faceRadius = faceSideLength * Math.sqrt(3d)/3d;
			double faceHeight = faceSideLength * Math.sqrt(3d)/2d;

			Vector3 pFaceCenter = new Vector3().add(
				vABmidpoint,
				new Vector3().sub(face.getC(), vABmidpoint).multiply(1 - faceRadius / faceHeight)
			);


			Vector3 p0 = face.getA();
			Vector3 p1 = face.getB();
			Vector3 p2 = face.getC();

			Vector3 v1 = new Vector3().sub(p1, p0);
			Vector3 v2 = new Vector3().sub(p2, p0);

			Vector3 vKprime = new Vector3().cross(v1, v2).normalize();
			Vector3 vJprime = new Vector3().sub(p0, pFaceCenter).normalize();
			Vector3 vIprime = new Vector3().cross(vJprime, vKprime).normalize();

			panel.getMatrix().set(
				vIprime.getX(), vJprime.getX(), vKprime.getX(), pFaceCenter.getX(),
				vIprime.getY(), vJprime.getY(), vKprime.getY(), pFaceCenter.getY(),
				vIprime.getZ(), vJprime.getZ(), vKprime.getZ(), pFaceCenter.getZ(),
				0,              0,              0,              1
			);

			panel.getRotation().setEulerFromRotationMatrix(panel.getMatrix(), panel.getEulerOrder());
			panel.setPosition(pFaceCenter.clone());
		}

		private Geometry createStrutGeometry(final double radius) {
			Geometry cylinder = new CylinderGeometry(radius, radius, 1, 10, 1, false);

			Matrix4 orientation = new Matrix4();
			orientation.setRotationFromEuler(new Vector3(Math.PI / 2, 0, 0));
			orientation.setPosition(new Vector3(0, 0, 1d / 2));
			cylinder.applyMatrix(orientation);

			return cylinder;
		}

		private Geometry createJoinGeometry(final double radius, final double height) {
			Geometry cylinder = new CylinderGeometry(radius, radius, height, 10, 1, false);

			Matrix4 orientation = new Matrix4();
			orientation.setRotationFromEuler(new Vector3(Math.PI / 2, 0, 0));
			cylinder.applyMatrix(orientation);

			return cylinder;
		}

		private Geometry panelGeometryFor(final double sideLength) {
			if (panelGeometries.containsKey(sideLength)) {
				return panelGeometries.get(sideLength);
			}

			// From http://mathworld.wolfram.com/EquilateralTriangle.html
			double sideRadius = (1.0/6)*Math.sqrt(3)*sideLength;
			double connerRadius = (1.0/3)*Math.sqrt(3)*sideLength;

			Shape shape = new Shape();
			shape.moveTo(0, connerRadius);
			shape.lineTo(sideLength/2, -sideRadius);
			shape.lineTo(-sideLength/2, -sideRadius);
			shape.closePath();

			final ExtrudeGeometry.ExtrudeGeometryParameters params = new ExtrudeGeometry.ExtrudeGeometryParameters();
			params.amount = domeModel.getSpec().getPanelThickness();
			params.steps = 3;
			params.bevelEnabled = false;

			Geometry geometry = shape.extrude(params);

			panelGeometries.put(sideLength, geometry);

			return geometry;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Getters and Setters

		public List<Mesh> getJoints() {
			return joints;
		}

		public Map<DomeGeometry.DomeEdge, Mesh> getStruts() {
			return struts;
		}

		public Map<DomeGeometry.DomeFace, Mesh> getPanels() {
			return panels;
		}
	}
}
