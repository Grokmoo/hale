function onAttack(game, attack, effect) {
	if (!attack.isMeleeWeaponAttack()) return;

	var attacker = attack.getAttacker();
	var defender = attack.getDefender();
	
	var parent = effect.getSlot().getParent();
	var lvls = parent.getRoles().getLevel("Duelist");
	
	if (parent == defender) {
		attack.setDefenderAC(attack.getDefenderAC() + 10 + 4 * lvls);
	}
	
	if (parent.getAbilities().has("Parry")) {
		var dist = game.distance(parent, attacker);
		if (dist <= 5)
			attack.addExtraAttack(-10 - 2 * lvls);
	}
}

function onDefense(game, attack, effect) {
	if (!attack.isMeleeWeaponAttack()) return;

	var attacker = attack.getAttacker();
	var defender = attack.getDefender();
	
	var parent = effect.getSlot().getParent();
	var lvls = parent.getRoles().getLevel("Duelist");
	
	if (parent == attacker) {
		attack.addExtraAttack(10 + 4 * lvls);
		
		if (parent.getAbilities().has("DeadlyDuel"))
			attack.addExtraDamage(lvls);
	}
}
