package org.hypher.cliententity.shared.entity;

/**
 * Base interface for objects which are aware of client entities.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ClientEntityAware {
	void setEntityResolver(ClientEntityResolver entityResolver);
}
