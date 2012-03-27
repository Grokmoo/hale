function canActivate(game, parent) {
	var weapon = parent.getInventory().getEquippedMainHand();
	
	return weapon != null;
}

function onActivate(game, slot) {
	var ability = slot.getAbility();
	var parent = slot.getParent();
	
	var weapon = parent.getInventory().getEquippedMainHand();
	if (weapon == null) return;
	
	var lvls = parent.getRoles().getLevel("Assassin");
	
	var duration = 5;
	if (parent.getAbilities().has("LingeringPoison"))
		duration += 3;
	
	slot.setActiveRoundsLeft(duration);
	slot.activate();
	
	var effect = slot.createEffect("effects/poison");
	effect.setDuration(duration);
	effect.setTitle(ability.getName());
	effect.put("attacksLeft", lvls + 3);
	
	var generator = game.getBaseParticleGenerator("flame");
	generator.setVelocityDistribution(game.getFixedAngleDistribution(20.0, 35.0, 3.14159 / 2));
	generator.setGreenSpeedDistribution(game.getFixedDistribution(0.0));
	generator.setRedDistribution(game.getFixedDistribution(0.5));
	generator.setBlueDistribution(game.getFixedDistribution(0.6));
	generator.setRedSpeedDistribution(game.getGaussianDistribution(-0.5, 0.05));
	generator.setBlueSpeedDistribution(game.getGaussianDistribution(-4.0, 0.05));
	if (parent.drawWithSubIcons()) {
		if (weapon.getWeaponType().toString().equals("BOW")) {
			var pos = parent.getSubIconScreenPosition("OffHandWeapon");
			generator.setPosition(pos.x, pos.y);
		} else {
			var pos = parent.getSubIconScreenPosition("MainHandWeapon");
			generator.setPosition(pos.x - 5.0, pos.y - 5.0);
		}
	} else {
		var pos = parent.getPosition().toScreen();
		generator.setPosition(pos.x - 15.0, pos.y - 15.0);
	}
	effect.addAnimation(generator);
	
	weapon.applyEffect(effect);
}
