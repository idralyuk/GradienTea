package org.hypher.cliententity.shared.entity;

import org.hypher.cliententity.shared.ids.EntityIdentifier;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseClientEntity<IdType extends EntityIdentifier> extends BaseEntityAware implements ClientEntity<IdType> {
	private IdType identifier;

	protected BaseClientEntity() { }

	protected BaseClientEntity(final IdType identifier) {
		this.identifier = identifier;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation Methods
	@Override
	public IdType getIdentifier() {
		return identifier;
	}
	//endregion
}
