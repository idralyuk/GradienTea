package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.JsArray;
import net.blimster.gwt.threejs.cameras.PerspectiveCamera;
import net.blimster.gwt.threejs.core.Color;
import net.blimster.gwt.threejs.core.Geometry;
import net.blimster.gwt.threejs.core.Matrix4;
import net.blimster.gwt.threejs.core.Object3D;
import net.blimster.gwt.threejs.core.Vector3;
import net.blimster.gwt.threejs.extras.core.Shape;
import net.blimster.gwt.threejs.extras.geometries.CylinderGeometry;
import net.blimster.gwt.threejs.extras.geometries.ExtrudeGeometry;
import net.blimster.gwt.threejs.extras.geometries.PlaneGeometry;
import net.blimster.gwt.threejs.lights.Light;
import net.blimster.gwt.threejs.lights.PointLight;
import net.blimster.gwt.threejs.materials.Material;
import net.blimster.gwt.threejs.materials.MeshBasicMaterial;
import net.blimster.gwt.threejs.materials.MeshPhongMaterial;
import net.blimster.gwt.threejs.objects.Mesh;
import net.blimster.gwt.threejs.renderers.CanvasRenderer;
import net.blimster.gwt.threejs.renderers.Renderer;
import net.blimster.gwt.threejs.renderers.WebGLRenderer;
import net.blimster.gwt.threejs.scenes.Scene;
import net.blimster.gwt.threejsx.util.JsArrays;
import org.hypher.gradientea.geometry.shared.GeoEdge;
import org.hypher.gradientea.geometry.shared.GeoFace;
import org.hypher.gradientea.geometry.shared.GradienTeaDomeGeometry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for domes. Each unit in the 3d space is considered to be 1 foot.
 */
class GradienTeaDomeRenderer {
	protected static final boolean openGlSupported;
	/**
	 * Enables the inclusion of extra triangles to double the size of the panels.
	 */
	protected static final boolean includeExtraTriangles = false;
	static {
		Canvas canvas = Canvas.createIfSupported();
		openGlSupported = (canvas != null
			&& (canvas.getContext("webgl") != null || canvas.getContext("experimental-webgl") != null));
	}

	//
	// Reused globals
	//
	protected Vector3 origin = Vector3.create();

	protected Geometry jointGeometry = createJoinGeometry(inches(3), inches(2));
	protected Material joinMaterial = MeshPhongMaterial.create(0xCCCCCC)
			.setShininess(.8)
			.setReflectivity(.4)
			.setMetal(true);

	protected Material strutMaterial = openGlSupported
		? MeshPhongMaterial.create(0x999999)
			.setShininess(.8)
			.setReflectivity(.4)
			.setMetal(true)
		: MeshBasicMaterial.create(0x999999);

	protected Geometry strutGeometry = createStrutGeometry(inches(1));

	protected Map<Double, Geometry> panelGeometries = Maps.newHashMap();

	//
	// Rendering objects
	//
	protected Renderer renderer;
	protected Scene scene;
	protected PerspectiveCamera outsideCamera;
	protected List<PointLight> lights;
	protected Mesh groundMesh;
	protected boolean lowQualityMode;

	//
	// Rendering Options
	//
	/**
	 * The angle the camera should be positioned.
	 */
	protected double cameraAngleRadians;

	/**
	 * The height the camera should be positioned off the ground.
	 */
	protected double cameraHeightFeet;

	/**
	 * The height the camera should be pointed at. The camera always looks towards the center of the dome.
	 */
	protected double cameraViewHeightFeet;

	/**
	 * The distance the camera should be positioned from the center of the dome.
	 */
	protected double cameraDistanceFeet;

	//
	// Dome-related meshes
	//
	private Object3D domeObject;
	private List<Mesh> joints = Lists.newArrayList();
	private Map<GeoEdge, Mesh> struts = Maps.newHashMap();
	private Map<GeoFace, PanelObject> panels = Maps.newHashMap();

	//
	// Unused meshes
	//
	private LinkedList<Mesh> unusedJoints = Lists.newLinkedList();
	private LinkedList<Mesh> unusedStruts = Lists.newLinkedList();
	private LinkedList<PanelObject> unusedPanels = Lists.newLinkedList();

	//
	// Dome geometry
	//
	private DomeProjection domeProjection;
	private double domeRadius;
	private double domePanelSideLength;


	public GradienTeaDomeRenderer(Canvas canvas) {
		lowQualityMode = (canvas.getContext("webgl") == null && canvas.getContext("experimental-webgl") == null);

		if (lowQualityMode) {
			renderer = CanvasRenderer.create(canvas);
		} else {
			renderer = WebGLRenderer.create(canvas, true);
		}

		renderer.setClearColor(Color.create(0x000000), 1.0f);

		scene = Scene.create();

		outsideCamera = PerspectiveCamera.create(75.0f, 1, 1.0f, 1000.0f);
		outsideCamera.getPosition().setZ(20.0);
		outsideCamera.setUp(Vector3.create(0.0, 0.0, 1.0));
		outsideCamera.lookAt(Vector3.create());

		lights = ImmutableList.of(
			PointLight.create(0xFFEEFF, 0.7, 100),
			PointLight.create(0xFFEEFF, 0.7, 100),
			PointLight.create(0xFFEEFF, 0.7, 100)
		);

		if (openGlSupported) {
			for (Light light : lights) {
				scene.add(light);
			}
		}

		Material personMaterial = openGlSupported
			? MeshPhongMaterial.create(0xEECEB3)
			: MeshBasicMaterial.create(0x706155);

		Mesh person = Mesh.create(
			CylinderGeometry.create(20 / 12.0, 20 / 12.0, 6.0, openGlSupported ? 20 : 3, 1, false),
			personMaterial
		);
		person.getRotation().setX(Math.PI/2);
		person.getScale().setX(0.6);
		person.getPosition().setZ(3.0);
		scene.add(person);

		//scene.add(AxisHelper.create(100));

		groundMesh = Mesh.create(
			PlaneGeometry.create(100000, 100000),
			MeshPhongMaterial.create(0xAFAC90)
		);
		groundMesh.getPosition().setZ(-0);

		if (openGlSupported) {
			scene.add(groundMesh);
		}

		domeObject = Object3D.create();
		scene.add(domeObject);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public void applyFaceColor(final GeoFace domeFace, int red, int green, int blue) {
		Preconditions.checkArgument(panels.containsKey(domeFace), "This model does not have a panel for " + domeFace);

		if (openGlSupported) {
			((MeshPhongMaterial) panels.get(domeFace).mesh.getMaterial()).getEmissive().setRGB(
				(double) red / 255,
				(double) green / 255,
				(double) blue / 255
			);
		} else {
			((MeshBasicMaterial) panels.get(domeFace).mesh.getMaterial()).getColor().setRGB(
				(double) red / 255,
				(double) green / 255,
				(double) blue / 255
			);
		}
	}

	public void setSize(int width, int height) {

		if (!openGlSupported) {
			// Using a small square saves rendering resources
			width = height = Math.min(width, height);
		}

		renderer.setSize(width, height);

		outsideCamera.setAspect((double) width / height);
		outsideCamera.updateProjectionMatrix();
	}

	public void renderDome(GradienTeaDomeGeometry geometry) {
		domeProjection = new DomeProjection(geometry);

		domeRadius = geometry.getSpec().getRadius();
		domePanelSideLength = geometry.getSpec().getPanelSideLength();

		clear();

		buildJoints();
		buildStruts();
		buildPanels();

		domeObject.getPosition().setZ(-domeProjection.bottomZ());

		updateCameraAndLights();
	}

	public void renderFrame() {
		outsideCamera.getPosition().set(
			cameraDistanceFeet * Math.cos(cameraAngleRadians),
			cameraDistanceFeet * Math.sin(cameraAngleRadians),
			cameraHeightFeet
		);
		outsideCamera.lookAt(Vector3.create(0, 0, cameraViewHeightFeet));

		renderer.render(scene, outsideCamera);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Setup Methods

	protected void updateCameraAndLights() {
		outsideCamera.getPosition().setZ(domeRadius * 0.9);
		outsideCamera.lookAt(origin);
		outsideCamera.setUp(Vector3.create(0.0, 0.0, 1.0));

		for (int i=0; i<lights.size(); i++) {
			PointLight light = lights.get(i);

			light.getPosition().setZ(domeRadius*1.5);
			light.getPosition().setX(domeRadius*1.5*Math.cos(Math.PI*2 * ((double)i/lights.size())));
			light.getPosition().setY(domeRadius * 1.5 * Math.sin(Math.PI * 2 * ((double) i / lights.size())));

			light.setDistance(domeRadius*2.5);
		}
	}

	private void clear() {
		unusedJoints.addAll(joints);
		unusedStruts.addAll(struts.values());
		unusedPanels.addAll(panels.values());

		for (Object3D o : Iterables.concat(
			unusedJoints,
			unusedStruts,
			Iterables.transform(unusedPanels, PanelObject.getMesh)
		)) {
			o.setVisible(false);
		}

		joints.clear();
		struts.clear();
		panels.clear();
	}

	private void buildJoints() {
		if (openGlSupported) {
			// Only render joints on accelerated systems
			for (Vector3 vertex : domeProjection.vertices()) {
				Mesh mesh = unusedJoints.isEmpty()
					? domeObject.add(Mesh.create(
						jointGeometry,
						joinMaterial
					))
					: unusedJoints.remove();

				mesh.setVisible(true);

				mesh.setPosition(vertex);
				mesh.lookAt(origin);
				joints.add(mesh);

				mesh.updateMatrix();
				mesh.setMatrixAutoUpdate(false);
			}
		}
	}

	private void buildStruts() {
		for (GeoEdge edge : edges()) {
			Mesh mesh = createStrut(edge);

			struts.put(edge, mesh);
		}
	}

	private void buildPanels() {
		for (GeoFace face : lightedFaces()) {
			PanelObject panelObject = createPanel(face);
			panels.put(face, panelObject);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utility Methods

	protected double inches(final double count) {
		return count / 12;
	}

	protected Mesh createStrut(GeoEdge edge) {
		Vector3[] edgeVertices = domeProjection.edge(edge);

		Mesh mesh = unusedStruts.isEmpty() ? domeObject.add(Mesh.create(
			strutGeometry,
			strutMaterial
		)) : unusedStruts.remove();

		mesh.setVisible(true);

		mesh.getScale().setZ(edgeVertices[0].distanceTo(edgeVertices[1]));
		mesh.setPosition(edgeVertices[0].clone());
		mesh.lookAt(edgeVertices[1]);

		mesh.updateMatrix();
		mesh.setMatrixAutoUpdate(false);
		return mesh;
	}

	protected PanelObject createPanel(GeoFace face) {
		double sideLength = domePanelSideLength;

		PanelObject panel;

		if (unusedPanels.isEmpty()) {
			Geometry geometry = createPanelGeometry();

			Material material;

			if (openGlSupported) {
				material = MeshPhongMaterial.create(0x000000).setTransparent(true);
				((MeshPhongMaterial) material).getAmbient().setRGB(1.0, 1.0, 1.0);
				material.setOpacity(0.8);
			} else {
				material = MeshBasicMaterial.create(0x000000);
			}

			panel = new PanelObject(
				Mesh.create(geometry, material)
			);

			domeObject.add(panel.container);
		}
		else {
			panel = unusedPanels.remove();
			panel.mesh.setVisible(true);
		}

		panel.mesh.setScale(
			Vector3.create(
				domeProjection.getGeometry().getSpec().getPanelSideLength(),
				domeProjection.getGeometry().getSpec().getPanelSideLength(),
				domeProjection.getGeometry().getSpec().getPanelThickness()
			)
		);

		orientPanel(face, panel.container);

		return panel;
	}

	protected void orientPanel(GeoFace face, Object3D panel) {
		Vector3[] faceVertices = domeProjection.face(face);

		Vector3 vABmidpoint = faceVertices[0].clone().addSelf(faceVertices[1]).divideScalar(2);

		double faceSideLength = face.getA().distanceTo(face.getB());
		double faceRadius = faceSideLength * Math.sqrt(3d)/3d;
		double faceHeight = faceSideLength * Math.sqrt(3d)/2d;

		Vector3 pFaceCenter = Vector3.create().add(
			vABmidpoint,
			Vector3.create().sub(faceVertices[2], vABmidpoint).multiplyScalar(1 - faceRadius / faceHeight)
		);

		Vector3 v1 = Vector3.create().sub(faceVertices[1], faceVertices[0]);
		Vector3 v2 = Vector3.create().sub(faceVertices[2], faceVertices[0]);

		Vector3 vKprime = Vector3.create().cross(v1, v2).normalize();
		Vector3 vJprime = Vector3.create().sub(faceVertices[0], pFaceCenter).normalize();
		Vector3 vIprime = Vector3.create().cross(vJprime, vKprime).normalize();

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
		Geometry cylinder = CylinderGeometry.create(radius, radius, 1, openGlSupported ? 10 : 3, 1, false);

		Matrix4 orientation = Matrix4.create();
		orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
		orientation.setPosition(Vector3.create(0, 0, 1d / 2));
		cylinder.applyMatrix(orientation);

		return cylinder;
	}

	private Geometry createJoinGeometry(final double radius, final double height) {
		Geometry cylinder = CylinderGeometry.create(radius, radius, height, openGlSupported ? 10 : 3, 1, false);

		Matrix4 orientation = Matrix4.create();
		orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
		cylinder.applyMatrix(orientation);

		return cylinder;
	}

	private Geometry createPanelGeometry() {
		double sideLength = 1.0;

		if (panelGeometries.containsKey(sideLength)) {
			return panelGeometries.get(sideLength);
		}

		// From http://mathworld.wolfram.com/EquilateralTriangle.html
		double sideRadius = (1.0/6)*Math.sqrt(3)*sideLength;
		double connerRadius = (1.0/3)*Math.sqrt(3)*sideLength;

		JsArray<Shape> shapes = JsArrays.newArray();

		Shape shape = Shape.createShape();
		shape.moveTo(0, connerRadius);
		shape.lineTo(sideLength / 2, -sideRadius);
		shape.lineTo(-sideLength/2, -sideRadius);
		shape.closePath();

		shapes.push(shape);

		if (includeExtraTriangles) {
			for (int i=0; i<3; i++) {
				double cx = 2.1*sideRadius*Math.cos(Math.PI/6 + i*(Math.PI*(2d/3)));
				double cy = 2.1*sideRadius*Math.sin(Math.PI/6 + i * (Math.PI*(2d/3)));

				shape = Shape.createShape();
				shape.moveTo(cx, cy-connerRadius);
				shape.lineTo(cx-sideLength/2, cy+sideRadius);
				shape.lineTo(cx+sideLength/2, cy+sideRadius);
				shape.closePath();

				shapes.push(shape);
			}
		}

		Geometry geometry = ExtrudeGeometry.createExtrudeGeometry(
			shapes, ExtrudeGeometry.ExtrudeOptions.create()
			.setAmount(1/24d)
			.setSteps(3)
			.setBevelEnabled(false)
		);
//
//		Geometry geometry = CylinderGeometry.create(sideLength/2, sideLength/2, 1.0, 3, 1, false);
//
		if (includeExtraTriangles) {
			Matrix4 orientation = Matrix4.create();
			orientation.setRotationFromEuler(Vector3.create(0, 0, Math.PI));
			geometry.applyMatrix(orientation);
		}

		panelGeometries.put(sideLength, geometry);

		return geometry;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Geometry Utility Methods

	protected Set<GeoEdge> edges() {
		return domeProjection.getGeometry().getDomeGeometry().getEdges();
	}

	protected Set<GeoFace> faces() {
		return domeProjection.getGeometry().getDomeGeometry().getFaces();
	}

	protected Set<GeoFace> lightedFaces() {
		return domeProjection.getGeometry().getLightedFaces();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	public List<Mesh> getJoints() {
		return joints;
	}

	public Map<GeoEdge, Mesh> getStruts() {
		return struts;
	}

	public Map<GeoFace, PanelObject> getPanels() {
		return panels;
	}

	public double getCameraAngleRadians() {
		return cameraAngleRadians;
	}

	public void setCameraAngleRadians(final double cameraAngleRadians) {
		this.cameraAngleRadians = cameraAngleRadians;
	}

	public double getCameraHeightFeet() {
		return cameraHeightFeet;
	}

	public void setCameraHeightFeet(final double cameraHeightFeet) {
		this.cameraHeightFeet = cameraHeightFeet;
	}

	public double getCameraViewHeightFeet() {
		return cameraViewHeightFeet;
	}

	public void setCameraViewHeightFeet(final double cameraViewHeightFeet) {
		this.cameraViewHeightFeet = cameraViewHeightFeet;
	}

	public double getCameraDistanceFeet() {
		return cameraDistanceFeet;
	}

	public void setCameraDistanceFeet(final double cameraDistanceFeet) {
		this.cameraDistanceFeet = cameraDistanceFeet;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	protected static class PanelObject {
		protected Mesh mesh;
		protected Object3D container;

		public PanelObject(final Mesh mesh) {
			this.mesh = mesh;

			this.container = Object3D.create();
			this.container.setMatrixAutoUpdate(false);
			this.container.add(mesh);
		}

		public static final Function<PanelObject, Object3D> getMesh = new Function<PanelObject, Object3D>(){
			public Object3D apply(final PanelObject input) {
				return input.mesh;
			}
		};
	}
}
