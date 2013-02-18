package org.hypher.gradientea.lightingmodel.shared.pixel;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * A simple implementation of a {@link PixelGroup} which consists of a pre-defined list of other pixel groups. Colors
 * are applied without any modification to all children.
 *
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ListPixelGroup extends AbstractPixelGroup {
	protected ImmutableList<PixelGroup> children;

	protected ListPixelGroup() {}

	public ListPixelGroup(final Collection<? extends PixelGroup> children) {
		this.children = ImmutableList.copyOf(children);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Instance Methods

	@Override
	public List<PixelGroup> getChildren() {
		return children;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generated Methods

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Getters and Setters


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Inner Classes
}
