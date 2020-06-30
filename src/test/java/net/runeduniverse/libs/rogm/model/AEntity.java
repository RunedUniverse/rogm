package net.runeduniverse.libs.rogm.model;

import lombok.ToString;
import net.runeduniverse.libs.rogm.annotations.GeneratedValue;
import net.runeduniverse.libs.rogm.annotations.Id;
import net.runeduniverse.libs.rogm.annotations.NodeEntity;

@NodeEntity
@ToString
public abstract class AEntity {
	
	@Id
	@GeneratedValue
	// TODO: add compatibility to long = Long
	private Long id;
}
