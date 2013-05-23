package org.hypher.gradientea.geometry.shared;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * Represents one face of the dome
 */
public class GeoFace implements Serializable {
	/**
	 * Comparator for faces which doesn't provide any particular ordering, but will be consistent.
	 */
	public final transient static Comparator<GeoFace> arbitraryComparator = new Comparator<GeoFace>() {
		@Override
		public int compare(
			final GeoFace o1, final GeoFace o2
		) {
			return ComparisonChain.start()
				.compare(o1.getA(), o2.getA(), GeoVector3.xyzComparator)
				.compare(o1.getB(), o2.getB(), GeoVector3.xyzComparator)
				.compare(o1.getC(), o2.getC(), GeoVector3.xyzComparator)
				.result();
		}
	};

	protected GeoVector3 v1;
	protected GeoVector3 v2;
	protected GeoVector3 v3;
	protected transient GeoVector3 center;

	protected GeoFace() {}

	public GeoFace(final GeoVector3 v1, final GeoVector3 v2, final GeoVector3 v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	public GeoVector3 getA() {
		return v1;
	}

	public GeoVector3 getB() {
		return v2;
	}

	public GeoVector3 getC() {
		return v3;
	}

	public Set<GeoVector3> getVertices() {
		return ImmutableSet.of(v1,v2,v3);
	}

	public static transient Function<? super GeoFace, Collection<GeoVector3>> getVertices = new Function<GeoFace, Collection<GeoVector3>>() {
		@Override
		public Collection<GeoVector3> apply(final GeoFace input) {
			return input.getVertices();
		}
	};


	public Collection<? extends GeoEdge> getEdges() {
		return Arrays.asList(
			new GeoEdge(v1, v2),
			new GeoEdge(v2, v3),
			new GeoEdge(v3, v1)
		);
	}

	public GeoVector3 center() {
		if (center == null) {
			center = v1.midpointBetween(v2).midpointBetween(v3);
		}
		return center;
	}

	/**
	 * @return The angle of the center of this face in the xy plane.
	 */
	public double theta() {
		return center().theta();
	}

	/**
	 * @return The angle of the center of this face in the yz plane.
	 */
	public double phi() {
		return center().phi();
	}

	private double normalizeAngle(double radians) {
		radians = radians % (2*Math.PI);
		if (radians < 0) radians += 2*Math.PI;
		return radians;
	}

	public int commonVerticiesWith(final GeoFace other) {
		Set<GeoVector3> verticies = getVertices();

		return
			(verticies.contains(other.getA()) ? 1 : 0) +
			(verticies.contains(other.getB()) ? 1 : 0) +
			(verticies.contains(other.getC()) ? 1 : 0);
	}

	public static class ContainsVertex implements Predicate<GeoFace> {
		protected Collection<GeoVector3> verticies;

		public ContainsVertex(final Collection<GeoVector3> verticies) {
			this.verticies = verticies;
		}

		@Override
		public boolean apply(final GeoFace face) {
			return ! Collections2.filter(
				verticies, new Predicate<GeoVector3>() {
				@Override
				public boolean apply(@Nullable final GeoVector3 input) {
					return input.equals(face.getA())
						|| input.equals(face.getB())
						|| input.equals(face.getC());
				}
			}
			).isEmpty();
		}
	}
}
