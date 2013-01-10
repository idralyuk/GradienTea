package org.hypher.gradientea.ui.client.simulator;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import net.blimster.gwt.threejs.cameras.Camera;
import net.blimster.gwt.threejs.cameras.PerspectiveCamera;
import net.blimster.gwt.threejs.core.Color;
import net.blimster.gwt.threejs.core.Geometry;
import net.blimster.gwt.threejs.core.Matrix4;
import net.blimster.gwt.threejs.core.Object3D;
import net.blimster.gwt.threejs.core.Vector3;
import net.blimster.gwt.threejs.extras.core.Shape;
import net.blimster.gwt.threejs.extras.geometries.CylinderGeometry;
import net.blimster.gwt.threejs.extras.geometries.ExtrudeGeometry;
import net.blimster.gwt.threejs.extras.geometries.SphereGeometry;
import net.blimster.gwt.threejs.extras.helpers.AxisHelper;
import net.blimster.gwt.threejs.lights.PointLight;
import net.blimster.gwt.threejs.materials.Material;
import net.blimster.gwt.threejs.materials.MeshPhongMaterial;
import net.blimster.gwt.threejs.objects.Mesh;
import net.blimster.gwt.threejs.renderers.WebGLRenderer;
import net.blimster.gwt.threejs.scenes.Scene;
import org.hypher.gradientea.lightingmodel.shared.dome.DomeSpecification;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class TestModelWidget extends Composite {
	public static final SphereGeometry DOME_VERTEX_SPHERE = SphereGeometry.create(2d/12, 20, 20);

	protected Canvas canvas = Canvas.createIfSupported();

	private WebGLRenderer renderer;
	private Scene scene;
	private Camera camera;
	private Mesh mesh;

	public TestModelWidget() {
		initWidget(canvas == null ? new Label("Canvas not supported") : canvas);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	double theta = Math.PI * .7;

	@Override
	protected void onLoad() {
		super.onLoad();

		int width = 900;
		int height = 900;

		renderer = WebGLRenderer.create(canvas, true);
		renderer.setSize(width, height);
		renderer.setClearColor(Color.create(0x000000), 1.0f);

		Material mat1 = MeshPhongMaterial.create(0xAAAAAA, 50);
		mat1.setOpacity(1.00);

		scene = Scene.create();
		scene.setDynamic(true);
		//scene.add(this.mesh);

		createDome(scene, mat1);

		camera = PerspectiveCamera.create(75.0f, width / height, 1.0f, 1000.0f);
		camera.getPosition().setZ(20.0);
		camera.setUp(Vector3.create(0.0, 0.0, 1.0));
		camera.lookAt(Vector3.create());

		for (int i=0; i<3; i++) {
			//DirectionalLight light = DirectionalLight.create(0xFFEEFF);
			//light.lookAt(Vector3.create());
			PointLight light = PointLight.create(0xFFEEFF, 3, 130);
			light.setPosition(Vector3.create(
				Math.cos(i * 2d/3 * Math.PI)*70,
				Math.sin(i * 2d / 3 * Math.PI)*70,
				50
			));
			scene.add(light);
		}

		AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
			@Override
			public void execute(final double timestamp) {
				//mesh.getRotation().setZ(mesh.getRotation().getZ() + 0.01);

				theta += Math.PI/1500;

				camera.getPosition().setX(50 * Math.cos(theta));
				camera.getPosition().setY(50 * Math.sin(theta));
				camera.lookAt(Vector3.create());

				renderer.render(scene, camera);

				AnimationScheduler.get().requestAnimationFrame(this);
			}
		});
	}

	private void createDome(final Scene scene, final Material mat1) {
		DomeGeometry model = new DomeGeometry(new DomeSpecification(
			4, 100, 20, 2.33, 1
		));

		Mesh person = Mesh.create(
			CylinderGeometry.create(20/12.0, 20/12.0, 6.0, 20, 1, false),
			MeshPhongMaterial.create(0xDDDD00)
		);
		person.getRotation().setX(Math.PI/2);
		person.getScale().setX(0.6);
		person.getPosition().setZ(3.0);
		scene.add(person);

		for (Vector3 v : model.getVertices()) {
			Mesh dot = Mesh.create(DOME_VERTEX_SPHERE, mat1);
			dot.setPosition(v);
			scene.add(dot);
		}

		for (DomeGeometry.DomeEdge edge : model.getEdges()) {
			scene.add(cylinderConnecting(edge.v1, edge.v2, mat1));
		}

		for (DomeGeometry.DomeFace face : model.getFaces())
		{
			addLightTriangle(scene, face, 2.33, 2d / 12);
		}

		scene.add(AxisHelper.create(100));
	}

	private Mesh cylinderConnecting(Vector3 a, Vector3 b, final Material material) {
		double distance = a.distanceTo(b);

		final Geometry cylinder = CylinderGeometry.create(1d/12, 1d/12, distance, 10, 1, false);

		Matrix4 orientation = Matrix4.create();
		orientation.setRotationFromEuler(Vector3.create(Math.PI/2, 0, 0));
		orientation.setPosition(Vector3.create(0, 0, distance / 2));
		cylinder.applyMatrix(orientation);

		Mesh mesh = Mesh.create(
			cylinder,
			material
		);

		mesh.setPosition(a.clone());
		mesh.lookAt(b);
		return mesh;
	}

	private Mesh addLightTriangle(final Scene scene, DomeGeometry.DomeFace face, double sideLength, double thickness) {
		// From http://mathworld.wolfram.com/EquilateralTriangle.html
		double sideRadius = (1.0/6)*Math.sqrt(3)*sideLength;
		double connerRadius = (1.0/3)*Math.sqrt(3)*sideLength;

		Shape shape = Shape.createShape();
		shape.moveTo(0, connerRadius);
		shape.lineTo(sideLength/2, -sideRadius);
		shape.lineTo(-sideLength/2, -sideRadius);
		shape.closePath();

		Geometry geometry = shape.extrude(
			ExtrudeGeometry.ExtrudeOptions.create()
				.setAmount(thickness)
				.setSteps(3)
				.setBevelEnabled(false)
		);

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
		//AxisHelper axisHelper = AxisHelper.create((int) (sideRadius * 3));
		//mesh.add(axisHelper);

		orientPanel(face, mesh, middle);

		scene.add(mesh);
		return mesh;
	}

	protected void orientPanel(DomeGeometry.DomeFace face, Mesh panel, Vector3 pCenter) {
		Vector3 p0 = face.getA();
		Vector3 p1 = face.getB();
		Vector3 p2 = face.getC();

		Vector3 v1 = Vector3.create().sub(p1, p0);
		Vector3 v2 = Vector3.create().sub(p2, p0);

		Vector3 vKprime = Vector3.create().cross(v1, v2).normalize();
		Vector3 vJprime = Vector3.create().sub(p0, pCenter).normalize();
		Vector3 vIprime = Vector3.create().cross(vJprime, vKprime).normalize();

		panel.getMatrix().set(
			vIprime.getX(), vJprime.getX(), vKprime.getX(), pCenter.getX(),
			vIprime.getY(), vJprime.getY(), vKprime.getY(), pCenter.getY(),
			vIprime.getZ(), vJprime.getZ(), vKprime.getZ(), pCenter.getZ(),
			0,              0,              0,              1
		);

		panel.getRotation().setEulerFromRotationMatrix(panel.getMatrix(), panel.getEulerOrder());
		panel.setPosition(pCenter.clone());
	}

	static Object3D myObj;


	protected void rotateAroundObjectAxis(Object3D object, Vector3 axis, double radians) {
		Matrix4 rotationMatrix = Matrix4.create();

		rotationMatrix.makeRotationAxis(axis.normalize(), radians);
		rotationMatrix.multiplySelf(object.getMatrix());
		object.setMatrix(rotationMatrix);
		object.getRotation().setEulerFromRotationMatrix(object.getMatrix(), object.getEulerOrder());
	}

	protected void rotateAroundWorldAxis(Object3D object, Vector3 axis, double radians) {
		Matrix4 rotWorldMatrix = Matrix4.create();
		rotWorldMatrix.makeRotationAxis(axis.normalize(), radians);
		rotWorldMatrix.multiplySelf(object.getMatrix());        // pre-multiply
		object.setMatrix(rotWorldMatrix);

		object.getRotation().setEulerFromRotationMatrix(object.getMatrix(), object.getEulerOrder());
	}

	protected static String vectorString(Vector3 v) {
		return "("+v.getX()+", "+v.getY()+", "+v.getZ()+")";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes

	protected static boolean equal(Vector3 a, Vector3 b) {
		return Math.abs(a.getX() - b.getX()) < 0.0000001
			&& Math.abs(a.getY() - b.getY()) < 0.0000001
			&& Math.abs(a.getZ() - b.getZ()) < 0.0000001;
	}

	protected static int vectorHash(Vector3 v) {
		return v == null
			? 0
			: (int) ((v.getX()*31 + v.getY())*31 + v.getZ())*31;
	}
}
