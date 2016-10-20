function onAttackHit(game, attack, damage, effect) {
	var parent = attack.getAttacker();
	var target = attack.getDefender();
	
	if (effect.get("stance") == "Burning Palm" && parent.abilities.has("SearingImpact")) {
		var checkDC = 50 + 4 * (parent.stats.getWis() - 10) + parent.roles.getLevel("Storm Hand") * 4;
		if (!target.stats.getPhysicalResistanceCheck(checkDC)) {
			var callback = effect.createDelayedCallback("applyEffect");
			callback.setDelay(0.5);
			callback.addArguments([ effect, target ]);
			callback.start();
			
			if (target.drawsWithSubIcons()) {
				var anim = game.getBaseAnimation("subIconFlash");
				anim.addFrame(target.getIconRenderer().getIcon("BaseForeground"));
				anim.setColor(target.getIconRenderer().getColor("BaseForeground"));
		
				var pos = target.getSubIconScreenPosition("BaseForeground");
				anim.setPosition(pos.x, pos.y);
			} else {
				var anim = game.getBaseAnimation("iconFlash");
				anim.addFrameAndSetColor(target.getTemplate().getIcon());
				var pos = target.getLocation().getCenteredScreenPoint();
				anim.setPosition(pos.x, pos.y);
			}
	
			anim.setSecondaryGreen(0.0);
			anim.setSecondaryBlue(0.0);
			game.runAnimationNoWait(anim);
		}
	}
}

function applyEffect(game, effect, target) {
	var targetEffect = effect.getSlot().createEffect();
	targetEffect.setDuration(5);
	targetEffect.setTitle("Searing Impact");
	targetEffect.getBonuses().addPenalty("Con", "Stackable", -1);
	targetEffect.addNegativeIcon("items/enchant_death_small");
	target.applyEffect(targetEffect);
}