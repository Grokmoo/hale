function startQuest(game) {
	if (game.hasQuestEntry("The Dwarven Key")) return;
	
	var quest = game.getQuestEntry("The Dwarven Key");
	
	var entry = quest.createSubEntry("Find the Key");
	
	entry.addText("The Dwarves have a key fragment located in their city to the north.  Travel to the city and find the key fragment.");
}