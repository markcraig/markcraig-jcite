/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * If applicable, add the following below this MPL 2.0 HEADER, replacing
 * the fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *     Portions Copyright [yyyy] [name of copyright owner]
 *
 *     Portions Copyright 2013 ForgeRock AS
 *
 */

/**
 * The interface {@link sej.describable.Describable} is implemented by classes
 * supporting complex, often multi-line descriptions of themselves. Buildup of
 * these descriptions is typically done in an StringBuilder for performance
 * reasons. This makes it easy and fast to incorporate subobjects' descriptions.
 *
 * <p>The specialized {@link sej.describable.DescriptionBuilder} supports proper
 * indentation in a structured way.
 */

package ch.arrenbrecht.describable;
