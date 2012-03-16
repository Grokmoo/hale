function onActivate(game, slot) {
	var ability = slot.getAbility();
	var parent = slot.getParent();
	
	var lvls = parent.getRoles().getLevel("Berserker");
	
	var duration = 3 + parseInt(lvls / 2);
	
	if (parent.getAbilities().has("ImprovedRage"))
		duration += 2;
	
	if (parent.getAbilities().has("EpicRage"))
		var strBonus = 8;
	else if (parent.getAbilities().has("ImprovedRage"))
		var strBonus = 6;
	else
		var strBonus = 3;
	
	slot.setActiveRoundsLeft(duration);
	slot.activate();
	
	var effect = slot.createEffect("effects/rage");
	effect.setDuration(duration);
	effect.setTitle(ability.getName());
	
	effect.getBonuses().addBonus('Str', 'Stackable', strBonus);
	
	effect.getBonuses().addBonus('OneHandedMeleeWeaponDamage', 'Morale', 25);
	effect.getBonuses().addBonus('TwoHandedMeleeWeaponDamage', 'Morale', 25);
	
	if (parent.getAbilities().has("UnstoppableRage")) {
		effect.getBonuses().add('ImmobilizationImmunity');
		effect.getBonuses().add('CriticalHitImmunity');
		
		effect.getBonuses().addBonus('SpellResistance', 10 + 2 * lvls);
	}
	
	if (parent.getAbilities().has("EpicRage")) {
		effect.getBonuses().addBonus('ActionPoint', 5 + 2 * lvls);
	}
	
	effect.getBonuses().addPenalty('Attack', 'Stackable', -25);
	effect.getBonuses().addPenalty('ArmorClass', 'Stackable', -25);
	parent.applyEffect(effect);
	
	var anim = game.getBaseAnimation("blast");
	var position = parent.getScreenPosition();
	anim.setPosition(position.x, position.y - 20);
	anim.setRed(1.0);
	anim.setGreen(0.2);
	anim.setBlue(0.2);
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
