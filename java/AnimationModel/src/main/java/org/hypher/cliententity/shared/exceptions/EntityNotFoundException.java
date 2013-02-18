package org.hypher.cliententity.shared.exceptions;

import org.hypher.cliententity.shared.ids.EntityIdentifier;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class EntityNotFoundException extends RuntimeException {
	public EntityNotFoundException(EntityIdentifier identifier) {
		super("Entity not found for identifier: " + identifier);
	}
}
