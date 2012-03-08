function startQuest(game) {
    if (game.hasQuestEntry("The Lizardling Island")) return;
    
    var quest = game.getQuestEntry("The Lizardling Island")
    var entry = quest.createSubEntry("Clear the Island")
    entry.addText("The Lizardling King has offered you a reward for kill the giant serpent on the island in the center of his lake.  In addition, you will be able to obtain the lizardling key fragment from the vault in the island's center.");
    
}

function questComplete(game) {
    var quest = game.getQuestEntry("The Lizardling Island");
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("Quest Complete");
    
    entry.addText("You killed the sea serpents and claimed your prize, a powerful ring.");
    
    quest.setCompleted();
}