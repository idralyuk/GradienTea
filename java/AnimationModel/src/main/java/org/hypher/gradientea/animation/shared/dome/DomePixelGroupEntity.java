package org.hypher.gradientea.animation.shared.dome;

import org.hypher.cliententity.shared.entity.ClientEntity;
import org.hypher.gradientea.animation.shared.PixelGroupContainer;
import org.hypher.gradientea.animation.shared.group.BasePixelGroupContainer;
import org.hypher.gradientea.animation.shared.ids.PixelGroupId;

import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class DomePixelGroupEntity extends BasePixelGroupContainer implements ClientEntity<PixelGroupId> {
	private PixelGroupId identifier;
	private List<PixelGroupContainer> childGroups;

	protected DomePixelGroupEntity() {}

	public DomePixelGroupEntity(
		final PixelGroupId identifier,
		final List<PixelGroupContainer> childGroups
	) {
		this.identifier = identifier;
		this.childGroups = childGroups;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Interface Implementation Methods
	@Override
	public PixelGroupId getIdentifier() {
		return identifier;
	}

	@Override
	public Collection<? extends PixelGroupContainer> getChildGroups() {
		return childGroups;
	}
	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Generated Methods

	//endregion

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//region// Getters and Setters

	//endregion
}
