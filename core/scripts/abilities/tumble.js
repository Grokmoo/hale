function canActivate(game, parent) {
	return parent.getTimer().canPerformAction(parent.stats().getMovementCost());
}

function onActivate(game, slot) {
	var parent = slot.getParent();
	
	// this is an estimate of how much movement the parent can perform
	// it will not be exact if their are movement penalizing effects on the area
	var AP = parent.getTimer().getAP();
	var moves = AP / parent.stats().getMovementCost();
	if (moves > 3) moves = 3;
	
	var targeter = game.createCircleTargeter(slot);
	targeter.setAllowOccupiedTileSelection(false);
	targeter.setMinRange(1);
	targeter.setMaxRange(moves);
	targeter.setRadius(0);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var parent = targeter.getParent();
	var position = targeter.getAffectedPoints().get(0);
	var ability = targeter.getSlot().getAbility();
	
	var path = game.ai.getMovementPath(parent, position);
	if (path == null || !parent.getTimer().canMove(path)) {
		game.addMessage("red", "You cannot move there or the distance is too great.");
		return;
	}
	
	targeter.getSlot().activate();
	
	var cb = ability.createDelayedCallback("performTumble");
	cb.addArgument(parent);
	cb.addArgument(position);
	cb.start();
}

function performTumble(game, parent, position) {
	// perform movement provoking no attacks of opportunity
	if (!game.ai.moveTowards(parent, position, 0, false)) {
		game.addMessage("red", "Tumble by " + parent + " was interrupted.");
		return;
	}
}