package org.hypher.gradientea.ui.client.simulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import net.blimster.gwt.threejs.cameras.PerspectiveCamera;
import net.blimster.gwt.threejs.core.Color;
import net.blimster.gwt.threejs.core.Geometry;
import net.blimster.gwt.threejs.core.Matrix4;
import net.blimster.gwt.threejs.core.Object3D;
import net.blimster.gwt.threejs.core.Vector3;
import net.blimster.gwt.threejs.extras.core.Shape;
import net.blimster.gwt.threejs.extras.geometries.CylinderGeometry;
import net.blimster.gwt.threejs.extras.helpers.AxisHelper;
import net.blimster.gwt.threejs.lights.PointLight;
import net.blimster.gwt.threejs.materials.Material;
import net.blimster.gwt.threejs.materials.MeshPhongMaterial;
import net.blimster.gwt.threejs.objects.Mesh;
import net.blimster.gwt.threejs.renderers.WebGLRenderer;
import net.blimster.gwt.threejs.scenes.Scene;
import org.hypher.gradientea.lightingmodel.shared.dome.DomeSpecification;
import org.hypher.gradientea.ui.client.player.DmxInterface;

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
	protected double rotationsPerMinute = 10;

	protected DomeGeometry domeGeometry;

	protected DomeRenderer domeRenderer;

	protected Canvas canvas;

	public DomeModelWidget() {
		initWidget(canvas == null ? new Label("Canvas not supported") : canvas);

		domeRenderer = new DomeRenderer(canvas);
	}

	public void buildDomeModel(DomeSpecification specification) {
		domeGeometry = new DomeGeometry(specification);
	}

	@Override
	public void onResize() {
		domeRenderer.setSize(getElement().getClientWidth(), getElement().getClientHeight());
	}

	@Override
	protected void onLoad() {
		super.onLoad();

		onResize();
	}

	@Override
	public void display(final int[][] dmxChannelValues) {
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	/**
	 * Renderer for domes. Each unit in the 3d space is considered to be 1 inch.
	 */
	protected class DomeRenderer {
		//
		// Reused globals
		//
		protected Vector3 origin = Vector3.create();

		protected Geometry jointGeometry = CylinderGeometry.create(inches(4), inches(4), inches(.5), 5, 1, false);
		protected MeshPhongMaterial joinMaterial = MeshPhongMaterial.create(0xCCCCCC);

		protected MeshPhongMaterial strutMaterial = MeshPhongMaterial.create(0xAAAAAA);
		protected double strutDiameter = inches(1);

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
		// Dome info
		//
		private DomeGeometry domeModel;

		public DomeRenderer(Canvas canvas) {
			renderer = WebGLRenderer.create(canvas, true);
			renderer.setClearColor(Color.create(0x000000), 1.0f);

			scene = Scene.create();

			camera = PerspectiveCamera.create(75.0f, 1, 1.0f, 1000.0f);
			camera.getPosition().setZ(20.0);
			camera.setUp(Vector3.create(0.0, 0.0, 1.0));
			camera.lookAt(Vector3.create());

			lights = ImmutableList.of(
				PointLight.create(0xFFEEFF, 3, 0),
				PointLight.create(0xFFEEFF, 3, 0),
				PointLight.create(0xFFEEFF, 3, 0)
			);

			setSize(canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
		}

		protected void setSize(int width, int height) {
			renderer.setSize(width, height);
			camera.setAspect((double)width / height);
		}

		protected void updateCameraAndLights() {
			double radius = domeModel.getSpec().getRadius();

			camera.getPosition().setZ(radius * 0.9);
			camera.lookAt(origin);
			camera.setUp(Vector3.create(0.0, 0.0, 1.0));

			for (int i=0; i<lights.size(); i++) {
				PointLight light = lights.get(i);

				light.getPosition().setZ(radius*1.5);
				light.setDistance(domeModel.getSpec().getRadius()*2);
			}
		}

		protected void renderDome(DomeGeometry model) {
			this.domeModel = model;

			clear();
			buildJoints();
			buildStruts();
			buildPanels();
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
				Mesh mesh = Mesh.create(
					jointGeometry,
					joinMaterial
				);

				mesh.setPosition(vertex);
				mesh.lookAt(origin);
				joints.add(mesh);

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
		protected double inches(final double count) {
			return count / 12;
		}

		protected Mesh createStrut(DomeGeometry.DomeEdge edge) {
			double distance = edge.getV1().distanceTo(edge.getV2());

			final Geometry cylinder = CylinderGeometry.create(strutDiameter/2, strutDiameter/2, distance, 10, 1, false);

			Matrix4 orientation = Matrix4.create();
			orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
			orientation.setPosition(Vector3.create(0, 0, distance / 2));
			cylinder.applyMatrix(orientation);

			Mesh mesh = Mesh.create(
				cylinder,
				strutMaterial
			);

			mesh.setPosition(edge.getV1().clone());
			mesh.lookAt(edge.getV2());
			return mesh;
		}

		protected Mesh createPanel(DomeGeometry.DomeFace face) {
			double sideLength = domeModel.getSpec().getPanelSideLength();

			// From http://mathworld.wolfram.com/EquilateralTriangle.html
			double sideRadius = (1.0/6)*Math.sqrt(3)*sideLength;
			double connerRadius = (1.0/3)*Math.sqrt(3)*sideLength;

			Shape shape = Shape.createShape();
			shape.moveTo(0, connerRadius);
			shape.lineTo(sideLength/2, -sideRadius);
			shape.lineTo(-sideLength/2, -sideRadius);
			shape.closePath();

//			Geometry geometry = shape.extrude(
//				ExtrudeGeometry.ExtrudeOptions.create()
//					.setAmount(domeModel.getSpec().getPanelThickness())
//					.setSteps(3)
//					.setBevelEnabled(false)
//			);

			// TODO: This is easier to position than the stupid triangular prism
			Geometry geometry = CylinderGeometry.create(
				sideLength/2,
				sideLength/2,
				domeModel.getSpec().getPanelThickness(),
				10,
				1,
				false
			);

			// Rotate the cylinder so the Z-axis is orthognal to the ends
			Matrix4 orientation = Matrix4.create();
			orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
			geometry.applyMatrix(orientation);

			Material material = MeshPhongMaterial.create(0xFF0000);

			Mesh mesh = Mesh.create(
				geometry,
				material
			);

			Vector3 sideMiddle = Vector3.create().add(face.getA(), face.getB()).divideScalar(2);

			double containingSideLength = face.getA().distanceTo(face.getB());
			double containingRadius = containingSideLength * Math.sqrt(3d)/3d;
			double containingHeight = containingSideLength * Math.sqrt(3d)/2d;

			Vector3 middle = Vector3.create().add(
				sideMiddle,
				Vector3.create().sub(face.getC(), sideMiddle).multiplyScalar(1 - containingRadius / containingHeight)
			);

			//mesh.add(AxisHelper.create((int) (sideRadius*3)));
			AxisHelper axisHelper = AxisHelper.create((int) (sideRadius * 3));

			mesh.setPosition(middle.clone());
			mesh.lookAt(Vector3.create());

			mesh.getRotation().setZ(0);

			return mesh;
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
