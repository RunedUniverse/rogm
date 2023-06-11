/*
 * Copyright © 2022 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.lib.rogm.test.model.relations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.runeduniverse.lib.rogm.annotations.Direction;
import net.runeduniverse.lib.rogm.annotations.Property;
import net.runeduniverse.lib.rogm.annotations.RelationshipEntity;
import net.runeduniverse.lib.rogm.annotations.StartNode;
import net.runeduniverse.lib.rogm.annotations.TargetNode;
import net.runeduniverse.lib.rogm.test.model.Inventory;
import net.runeduniverse.lib.rogm.test.model.Item;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RelationshipEntity(label = "ITEM_SLOT", direction = Direction.OUTGOING)
public class Slot extends ARelationEntity {
	@Property
	private Integer slot;
	@StartNode
	private Inventory inventory;
	@TargetNode
	private Item item;
}
