/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.lexsemresource.graph;

import de.tudarmstadt.ukp.dkpro.lexsemresource.Entity;

public class EntityGraphEdge {

	private Entity source;
	private Entity target;

	public EntityGraphEdge(Entity source, Entity target) {
		this.source = source;
		this.target = target;
	}

	public Entity getSource() {
		return source;
	}
	public void setSource(Entity source) {
		this.source = source;
	}
	public Entity getTarget() {
		return target;
	}
	public void setTarget(Entity target) {
		this.target = target;
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof EntityGraphEdge)) {
			return false;
		}
		EntityGraphEdge otherEdge = (EntityGraphEdge) other;
		return equals(source, otherEdge.getSource())
				&& equals(target, otherEdge.getTarget());
	}

	/**
	 * Compares two objects for equality taking care of null references
	 *
	 * @param x
	 *            an object
	 * @param y
	 *            an object
	 * @return returns true when <code>x</code> equals <code>y</code> or if both
	 *         are null
	 */
	protected final boolean equals(Object x, Object y)
	{
		return (x == null && y == null) || (x != null && x.equals(y));
	}

}
