package org.hypher.cliententity.shared.entity;

import org.hypher.cliententity.shared.ids.EntityIdentifier;

import java.io.Serializable;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ClientEntity<IdType extends EntityIdentifier> extends ClientEntityAware, Serializable {
	IdType getIdentifier();
}
