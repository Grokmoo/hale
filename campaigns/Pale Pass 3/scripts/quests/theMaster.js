function startQuest(game) {
    if (game.hasQuestEntry("The Master")) return;
    
    var quest = game.getQuestEntry("The Master");
    
    var entry = quest.createSubEntry("Escape from the Underground");
    
    entry.addText("You have escaped from the subterranean land you were trapped in.  While exploring the cave system, you discovered that you are of a chosen blood, and that the Master will not stop until you are dead.");
    
    entry.addText("In order to stop the seemingly endless stream of assassins, you must find and defeat the Master.");
}

function learnOfArmy(game) {
	var quest = game.getQuestEntry("The Master");
	
	if (quest.hasSubEntry("The Master's Army")) return;
	
	quest.setCurrentSubEntriesCompleted();
	
	var entry = quest.createSubEntry("The Master's Army");
	
	entry.addText("You met a scout who told you that while you were trapped underground, the Master has assembled an army and is now openly opposing the King.");
	
	entry.addText("You should travel to Aravil to learn more and help with the fight against the Master.");
	
	
}
