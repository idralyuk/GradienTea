package org.hypher.cliententity.shared.ids;

import com.google.common.base.Preconditions;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class TypedIdentifier {
	private String identifier;

	protected TypedIdentifier() {}

	public TypedIdentifier(String identifier) {
		Preconditions.checkNotNull(identifier, "Identifier must not be null");

		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final TypedIdentifier that = (TypedIdentifier) o;

		if (!identifier.equals(that.identifier)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode() * getClass().getName().hashCode();
	}
}
