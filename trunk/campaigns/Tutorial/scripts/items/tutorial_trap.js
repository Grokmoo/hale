function onSpringTrap(game, trap, target) {
	target.getInventory().addItem("tutorial_potion", "Good");
	
	var cb = trap.getScript().createDelayedCallback("tutorialTrap");
	cb.setDelay(0.7);
	cb.start();
}

function tutorialTrap(game) {
	var tutQuest = game.getQuestEntry("Tutorial");
	if (tutQuest.hasSubEntry("Potions")) return;

	var popup = game.createHTMLPopup("tutorial/tutorial_14.html");
    popup.setSize(400, 250);
    popup.show();

    tutQuest.setCurrentSubEntriesCompleted();
    var subEntry = tutQuest.createSubEntry("Potions");
    subEntry.setShowTitle(false);
    subEntry.addExternalText("tutorial/tutorial_14.html");
}