package org.hypher.gradientea.ui.client.simulator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.canvas.client.Canvas;
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
import net.blimster.gwt.threejs.materials.MeshPhongMaterial;
import net.blimster.gwt.threejs.objects.Mesh;
import net.blimster.gwt.threejs.renderers.CanvasRenderer;
import net.blimster.gwt.threejs.renderers.Renderer;
import net.blimster.gwt.threejs.renderers.WebGLRenderer;
import net.blimster.gwt.threejs.scenes.Scene;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoEdge;
import org.hypher.gradientea.lightingmodel.shared.dome.GeoFace;
import org.hypher.gradientea.lightingmodel.shared.dome.GradienTeaDomeGeometry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for domes. Each unit in the 3d space is considered to be 1 foot.
 */
class GradienTeaDomeRenderer {
	//
	// Reused globals
	//
	protected Vector3 origin = Vector3.create();

	protected Geometry jointGeometry = createJoinGeometry(inches(3), inches(2));
	protected MeshPhongMaterial joinMaterial = MeshPhongMaterial.create(0xCCCCCC)
		.setShininess(.8)
		.setReflectivity(.4)
		.setMetal(true);

	protected MeshPhongMaterial strutMaterial = MeshPhongMaterial.create(0xAAAAAA)
		.setShininess(.8)
		.setReflectivity(.4)
		.setMetal(true);

	protected Geometry strutGeometry = createStrutGeometry(inches(1));

	protected Map<Double, Geometry> panelGeometries = Maps.newHashMap();

	//
	// Rendering objects
	//
	protected Renderer renderer;
	protected Scene scene;
	protected PerspectiveCamera outsideCamera;
	protected PerspectiveCamera insideCamera;
	protected List<PointLight> lights;
	protected Mesh groundMesh;

	//
	// Dome-related meshes
	//
	private Object3D domeObject;
	private List<Mesh> joints = Lists.newArrayList();
	private Map<GeoEdge, Mesh> struts = Maps.newHashMap();
	private Map<GeoFace, Mesh> panels = Maps.newHashMap();
	private Map<GeoFace, PointLight> panelLights = Maps.newHashMap();

	//
	// Unused meshes
	//
	private LinkedList<Mesh> unusedJoints = Lists.newLinkedList();
	private LinkedList<Mesh> unusedStruts = Lists.newLinkedList();
	private LinkedList<Mesh> unusedPanels = Lists.newLinkedList();
	private LinkedList<Object3D> unusedPanelLights = Lists.newLinkedList();

	//
	// Dome geometry
	//
	private DomeProjection domeProjection;
	private double domeRadius;
	private double domePanelSideLength;


	public GradienTeaDomeRenderer(Canvas canvas) {

		if (canvas.getContext("webgl") == null && canvas.getContext("experimental-webgl") == null) {
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

		insideCamera = PerspectiveCamera.create(55.0f, 1, 1.0f, 1000.0f);
		insideCamera.getPosition().setZ(5.0);
		insideCamera.setUp(Vector3.create(0.0, 0.0, 1.0));

		lights = ImmutableList.of(
			PointLight.create(0xFFEEFF, 0.6, 100),
			PointLight.create(0xFFEEFF, 0.6, 100),
			PointLight.create(0xFFEEFF, 0.6, 100)
		);

		for (Light light : lights) {
			scene.add(light);
		}

		MeshPhongMaterial personMaterial = MeshPhongMaterial.create(0xEECEB3);

		Mesh person = Mesh.create(
			CylinderGeometry.create(20 / 12.0, 20 / 12.0, 6.0, 20, 1, false),
			personMaterial
		);
		person.getRotation().setX(Math.PI/2);
		person.getScale().setX(0.6);
		person.getPosition().setZ(3.0);
		scene.add(person);

		//scene.add(AxisHelper.create(100));

		groundMesh = Mesh.create(
			PlaneGeometry.create(1000, 1000),
			MeshPhongMaterial.create(0xAFAC90)
		);
		groundMesh.getPosition().setZ(-0);
		scene.add(groundMesh);

		domeObject = Object3D.create();
		scene.add(domeObject);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	public void applyFaceColor(final GeoFace domeFace, int red, int green, int blue) {
		Preconditions.checkArgument(panels.containsKey(domeFace), "This model does not have a panel for " + domeFace);

		((MeshPhongMaterial) panels.get(domeFace).getMaterial()).getEmissive().setRGB(
			(double) red / 255,
			(double) green / 255,
			(double) blue / 255
		);

		if (panelLights.containsKey(domeFace)) {
			panelLights.get(domeFace).getColor().setRGB(
				(double) red / 255,
				(double) green / 255,
				(double) blue / 255
			);
		}
	}

	public void setSize(int width, int height) {
		renderer.setSize(width, height);
		outsideCamera.setAspect((double) width / height);
		outsideCamera.updateProjectionMatrix();

		insideCamera.setAspect((double) width / height);
		insideCamera.updateProjectionMatrix();
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

	public void renderFrame(double cameraRotation, boolean useInsideCamera) {
		outsideCamera.getPosition().setX(domeRadius*2 * Math.cos(cameraRotation));
		outsideCamera.getPosition().setY(domeRadius*2 * Math.sin(cameraRotation));
		outsideCamera.lookAt(origin);

		insideCamera.lookAt(outsideCamera.getPosition());
		insideCamera.getUp().set(0, 0, 1.0);

		renderer.render(scene, useInsideCamera ? insideCamera : outsideCamera);
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
		unusedPanelLights.addAll(panelLights.values());

		for (Object3D o : Iterables.concat(unusedJoints, unusedStruts, unusedPanels, unusedPanelLights)) {
			o.setVisible(false);
		}

		joints.clear();
		struts.clear();
		panels.clear();
		panelLights.clear();
	}

	private void buildJoints() {
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

	private void buildStruts() {
		for (GeoEdge edge : edges()) {
			Mesh mesh = createStrut(edge);

			struts.put(edge, mesh);
		}
	}

	private void buildPanels() {
		for (GeoFace face : lightedFaces()) {
			Mesh mesh = createPanel(face);

			panels.put(face, mesh);

//			PointLight light = PointLight.create(0x000000, .05, 20);
//			panelLights.put(face, light);
//			domeObject.add(light);
//			light.getPosition().copy(mesh.getPosition());
//			light.getPosition().setLength(light.getPosition().length()+0.2);
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

	protected Mesh createPanel(GeoFace face) {
		double sideLength = domePanelSideLength;


		MeshPhongMaterial material;
		Mesh mesh;
		if (unusedPanels.isEmpty()) {
			Geometry geometry = panelGeometryFor(1.0);

			material = MeshPhongMaterial.create(0x000000).setTransparent(true);
			material.getAmbient().setRGB(1.0, 1.0, 1.0);
			material.setOpacity(0.8);

			mesh = Mesh.create(geometry, material);
			mesh.setMatrixAutoUpdate(false);

			domeObject.add(mesh);
		}
		else {
			mesh = unusedPanels.remove();
			material = (MeshPhongMaterial) mesh.getMaterial();
			mesh.setVisible(true);
		}

		orientPanel(face, mesh);

		mesh.getMatrix().scale(
			Vector3.create(
				domeProjection.getGeometry().getSpec().getPanelSideLength(),
				domeProjection.getGeometry().getSpec().getMaxPanelHeight(),
				domeProjection.getGeometry().getSpec().getPanelThickness()
			)
		);

		return mesh;
	}

	protected void orientPanel(GeoFace face, Mesh panel) {
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
		Geometry cylinder = CylinderGeometry.create(radius, radius, 1, 10, 1, false);

		Matrix4 orientation = Matrix4.create();
		orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
		orientation.setPosition(Vector3.create(0, 0, 1d / 2));
		cylinder.applyMatrix(orientation);

		return cylinder;
	}

	private Geometry createJoinGeometry(final double radius, final double height) {
		Geometry cylinder = CylinderGeometry.create(radius, radius, height, 10, 1, false);

		Matrix4 orientation = Matrix4.create();
		orientation.setRotationFromEuler(Vector3.create(Math.PI / 2, 0, 0));
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

		Shape shape = Shape.createShape();
		shape.moveTo(0, connerRadius);
		shape.lineTo(sideLength / 2, -sideRadius);
		shape.lineTo(-sideLength/2, -sideRadius);
		shape.closePath();

		Geometry geometry = shape.extrude(
			ExtrudeGeometry.ExtrudeOptions.create()
				.setAmount(1.0)
				.setSteps(3)
				.setBevelEnabled(false)
		);

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

	public Map<GeoFace, Mesh> getPanels() {
		return panels;
	}
}
