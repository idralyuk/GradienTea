package org.hypher.cliententity.shared.entity;

import org.hypher.cliententity.shared.ids.EntityIdentifier;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ClientEntityResolver {
	<I extends EntityIdentifier, E extends ClientEntity<I>> E resolve(I id);
}
