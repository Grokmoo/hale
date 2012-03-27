function onAttackHit(game, attack, damage, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	var lvls = attacker.getRoles().getLevel("Assassin");
	
	var dc = 60 + lvls * 5;
	
	if (!target.physicalResistanceCheck(dc)) {
		var targetEffect = attacker.createEffect("effects/damageOverTime");
		
		if (attacker.getAbilities().has("LingeringPoison"))
			targetEffect.setDuration(3);
		else
			targetEffect.setDuration(2);
		
		targetEffect.put("damageType", "Poison");
		
		if (attacker.getAbilities().has("LethalPoison")) {
			targetEffect.put("minDamagePerRound", 1 + parseInt(lvls / 2));
			targetEffect.put("maxDamagePerRound", 6 + parseInt(lvls / 2));
		} else {
			targetEffect.put("minDamagePerRound", 1);
			targetEffect.put("maxDamagePerRound", 6);
		}
		
		target.applyEffect(targetEffect);
		
		game.addMessage("red", target.getName() + " has been poisoned.");
	}
	
	var attacksLeft = effect.get("attacksLeft");
	attacksLeft -= 1;
	
	effect.put("attacksLeft", attacksLeft);
	
	if (attacksLeft == 0) {
		effect.getTarget().removeEffect(effect);
	}
}
