package org.hypher.gradientea.ui.client.simulator;

import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Vector2;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ParallaxHelper {

	public static MeshPhongMaterialBuilder meshPhongMaterial() {
		return new MeshPhongMaterialBuilder();
	}

	public static class MeshPhongMaterialBuilder {
		protected MeshPhongMaterial material = new MeshPhongMaterial();

		public MeshPhongMaterial get() {
			return material;
		}

		public MeshPhongMaterialBuilder setSpecular(final Color specular) {
			material.setSpecular(specular);
			return this;
		}

		public MeshPhongMaterialBuilder setShininess(final double shininess) {
			material.setShininess(shininess);
			return this;
		}

		public MeshPhongMaterialBuilder setPerPixel(final boolean isPerPixel) {
			material.setPerPixel(isPerPixel);
			return this;
		}

		public MeshPhongMaterialBuilder setMetal(final boolean isMetal) {
			material.setMetal(isMetal);
			return this;
		}

		public MeshPhongMaterialBuilder setWrapAround(final boolean wrapAround) {
			material.setWrapAround(wrapAround);
			return this;
		}

		public MeshPhongMaterialBuilder setWrapRGB(final Vector3 wrapRGB) {
			material.setWrapRGB(wrapRGB);
			return this;
		}

		public MeshPhongMaterialBuilder setWireframe(final boolean wireframe) {
			material.setWireframe(wireframe);
			return this;
		}

		public MeshPhongMaterialBuilder setWireframeLineWidth(final int wireframeLineWidth) {
			material.setWireframeLineWidth(wireframeLineWidth);
			return this;
		}

		public MeshPhongMaterialBuilder setEnvMap(final Texture envMap) {
			material.setEnvMap(envMap);
			return this;
		}

		public MeshPhongMaterialBuilder setCombine(final Texture.OPERATIONS combine) {
			material.setCombine(combine);
			return this;
		}

		public MeshPhongMaterialBuilder setReflectivity(final double reflectivity) {
			material.setReflectivity(reflectivity);
			return this;
		}

		public MeshPhongMaterialBuilder setRefractionRatio(final double refractionRatio) {
			material.setRefractionRatio(refractionRatio);
			return this;
		}

		public MeshPhongMaterialBuilder setLightMap(final Texture lightMap) {
			material.setLightMap(lightMap);
			return this;
		}

		public MeshPhongMaterialBuilder setFog(final boolean fog) {
			material.setFog(fog);
			return this;
		}

		public MeshPhongMaterialBuilder setColor(final Color color) {
			material.setColor(color);
			return this;
		}

		public MeshPhongMaterialBuilder setMap(final Texture map) {
			material.setMap(map);
			return this;
		}

		public MeshPhongMaterialBuilder setVertexColors(final Material.COLORS vertexColors) {
			material.setVertexColors(vertexColors);
			return this;
		}

		public MeshPhongMaterialBuilder setSkinning(final boolean isSkinning) {
			material.setSkinning(isSkinning);
			return this;
		}

		public MeshPhongMaterialBuilder setMorphTargets(final boolean isMorphTargets) {
			material.setMorphTargets(isMorphTargets);
			return this;
		}

		public MeshPhongMaterialBuilder setMorphNormals(final boolean isMorphNormals) {
			material.setMorphNormals(isMorphNormals);
			return this;
		}

		public MeshPhongMaterialBuilder setNumSupportedMorphTargets(final int num) {
			material.setNumSupportedMorphTargets(num);
			return this;
		}

		public MeshPhongMaterialBuilder setNumSupportedMorphNormals(final int num) {
			material.setNumSupportedMorphNormals(num);
			return this;
		}

		public MeshPhongMaterialBuilder setAmbient(final Color ambient) {
			material.setAmbient(ambient);
			return this;
		}

		public MeshPhongMaterialBuilder setEmissive(final Color emissive) {
			material.setEmissive(emissive);
			return this;
		}

		public MeshPhongMaterialBuilder setSpecularMap(final Texture specularMap) {
			material.setSpecularMap(specularMap);
			return this;
		}

		public MeshPhongMaterialBuilder setBumpMap(final Texture bumpMap) {
			material.setBumpMap(bumpMap);
			return this;
		}

		public MeshPhongMaterialBuilder setBumpScale(final double bumpScale) {
			material.setBumpScale(bumpScale);
			return this;
		}

		public MeshPhongMaterialBuilder setNormalMap(final Texture normalMap) {
			material.setNormalMap(normalMap);
			return this;
		}

		public MeshPhongMaterialBuilder setNormalScale(final Vector2 normalScale) {
			material.setNormalScale(normalScale);
			return this;
		}
	}
}
