package org.hypher.cliententity.shared.entity;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.hypher.cliententity.shared.ids.EntityIdentifier;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class BaseEntityAware implements ClientEntityAware {
	private transient ClientEntityResolver entityResolver;
	protected Function<EntityIdentifier, ClientEntity<?>> resolve = new Function<EntityIdentifier, ClientEntity<?>>() {
		@Override
		public ClientEntity<?> apply(final EntityIdentifier input) {
			return ensureEntityResolver().resolve(input);
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Abstract Methods
	protected abstract Iterable<? extends ClientEntityAware> getEntityAwareChildren();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Protected Methods
	protected ClientEntityResolver ensureEntityResolver() {
		Preconditions.checkState(entityResolver != null, "EntityResolver has not been set yet");

		return entityResolver;
	}

	protected <I extends EntityIdentifier, E extends ClientEntity<I>> E resolve(I id) {
		return ensureEntityResolver().resolve(id);
	}

	protected <I extends EntityIdentifier, E extends ClientEntity<I>> Iterable<E> resolve(Iterable<? extends I> ids) {
		//noinspection unchecked
		return Iterables.transform(ids, (Function<I, E>) resolve);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Interface Implementation
	@Override
	public final void setEntityResolver(final ClientEntityResolver entityResolver) {
		this.entityResolver = entityResolver;

		for (ClientEntityAware child : getEntityAwareChildren()) {
			child.setEntityResolver(entityResolver);
		}
	}


}
