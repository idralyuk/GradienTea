package org.hypher.gradientea.lightingmodel.shared.dome;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

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
				.compare(o1.getA(), o2.getA(), GeoVector3.componentComparator)
				.compare(o1.getB(), o2.getB(), GeoVector3.componentComparator)
				.compare(o1.getC(), o2.getC(), GeoVector3.componentComparator)
				.result();
		}
	};

	protected GeoVector3 v1;
	protected GeoVector3 v2;
	protected GeoVector3 v3;

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

	public Collection<GeoVector3> getVertices() {
		return Arrays.asList(v1, v2, v3);
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
