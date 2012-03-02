function onAttack(game, attack, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	if (!isBackstab(game, attack)) return;
	
	var mastery = attacker.getAbilities().has("BackstabMastery");
	
	var charLevel = attacker.getRoles().getLevel("Rogue");
	
	var extraDamage = 3 + game.dice().rand(charLevel / 2, charLevel);
	var extraAttack = 10 + charLevel;
	
	if (mastery) {
		extraDamage += 3;
		extraAttack += parseInt(charLevel / 2);
	}
	
	game.addMessage("red", attack.getAttacker().getName() + " gets Backstab against " + attack.getDefender().getName());
	
	attack.addExtraDamage(extraDamage);
	attack.addExtraAttack(extraAttack);
}

function onAttackHit(game, attack, damage, effect) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();

	var cripple = attacker.getAbilities().has("CripplingBackstab");
	if (!cripple) return;

	if (!isBackstab(game, attack)) return;
	
	var mastery = attacker.getAbilities().has("BackstabMastery");
	
	var charLevel = attacker.getRoles().getLevel("Rogue");
	
	var dc = 50 + charLevel * 4;
	
	if (mastery) {
		dc += charLevel;
	}
	
	if (!target.physicalResistanceCheck(dc)) {
		var crippleEffect = attacker.createEffect();
		
		crippleEffect.setDuration(3);
		crippleEffect.setTitle("Crippling Backstab");
		
		crippleEffect.getBonuses().addPenalty('Str', 'Stackable', -1);
		crippleEffect.getBonuses().addPenalty('Dex', 'Stackable', -1);
		
		target.applyEffect(crippleEffect);
	}
}

function isBackstab(game, attack) {
	var attacker = attack.getAttacker();
	var target = attack.getDefender();
	
	var improved = attacker.getAbilities().has("ImprovedBackstab");
	var ranged = attacker.getAbilities().has("RangedBackstab");
	var mastery = attacker.getAbilities().has("BackstabMastery");
	
	var rangedDistance = 15;
	if (mastery) rangedDistance += 5;
	
	// attack must be melee unless the attacker has ranged backstab,
	// in which case max distance is 15 or 20
	if (!attack.isMeleeWeaponAttack()) {
		if (!ranged || game.distance(attacker.getPosition(), target.getPosition()) > rangedDistance)
			return false;
	}
	
	// attacker must be hidden or target helpless / immobilized unless
	// attacker has improved backstab, in which case flanking is sufficient
	if (!improved || !attack.isFlankingAttack()) {
		if (!attacker.stats().has("Hidden") && !target.isHelpless() && !target.isImmobilized())
			return false;
	}
	
	return true;
}