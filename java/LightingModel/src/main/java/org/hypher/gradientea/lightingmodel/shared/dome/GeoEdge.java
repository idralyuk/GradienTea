package org.hypher.gradientea.lightingmodel.shared.dome;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

/**
 * Represents one edge of the dome
 */
public class GeoEdge implements Serializable {
	protected GeoVector3 v1;
	protected GeoVector3 v2;

	protected GeoEdge() { /* For GWT Serialization */ }

	public GeoEdge(final GeoVector3 v1, final GeoVector3 v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final GeoEdge geoEdge = (GeoEdge) o;

		if (v1.equals(geoEdge.v1) && v2.equals(geoEdge.v2))
			return true;

		if (v2.equals(geoEdge.v1) && v1.equals(geoEdge.v2))
			return true;

		return false;
	}

	@Override
	public int hashCode() {
		int result;
		if (lessThan(v1, v2)) {
			result = v1.hashCode();
			result = 31 * result + v2.hashCode();
		} else {
			result = v2.hashCode();
			result = 31 * result + v1.hashCode();
		}

		return result;
	}

	protected boolean lessThan(GeoVector3 a, GeoVector3 b) {
		return ComparisonChain.start()
			.compare(a.getX(), b.getX())
			.compare(a.getY(), b.getY())
			.compare(a.getZ(), b.getZ())
			.result() < 0;
	}

	@Override
	public String toString() {
		return "GeoEdge{" +
			"v1=" + v1 +
			", v2=" + v2 +
			'}';
	}

	public GeoVector3 getV1() {
		return v1;
	}

	public GeoVector3 getV2() {
		return v2;
	}
}
