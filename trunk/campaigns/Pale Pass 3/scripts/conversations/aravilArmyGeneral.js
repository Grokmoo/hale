function startConversation(game, parent, target, conversation) {
	if (parent.get("attackQuestGiven") != null) {
		conversation.addText("Good luck, friends.");
		
		conversation.addResponse("Farewell.", "onExit");
	} else {
		conversation.addText("Hail friends!  You must be the adventurers I was told about.");
	
		conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo02");
	}
}

function convo02(game, parent, target, conversation) {
	conversation.addText("It is good you are here, we need all the help we can get.");
	
	conversation.addResponse("What is the situation like here?", "convo03");
}

function convo03(game, parent, target, conversation) {
	conversation.addText("Well, the Master has brought in a large army of demons from somewhere up north.");
	
	conversation.addText("As of yet, there hasn't been much fighting.  But we'll be ready for the bastards.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
	conversation.addText("I had recieved word from one of my scouts that he had found something matching the description of your focus crystal.");
	
	conversation.addText("Unfortunately, his group was ambushed and we believe the crystal now lies somewhere in the enemy camp, probably in the command tent.");
	
	conversation.addResponse("So how do we get it back?", "convo05");
}

function convo05(game, parent, target, conversation) {
	conversation.addText("We don't have much chance of breaking through the enemy lines at the moment.  However, we should be able to keep the bulk of the Master's forces occupied on the front lines.");
	
	conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
	conversation.addText("That should give you a chance to break into the enemy camp and destroy the crystal.  It will be hard fighting, but you seem up to the challenge.");
	
	conversation.addResponse("Very well.  Good luck, general.", "onExit");
	
	parent.put("attackQuestGiven", true);
	game.runExternalScript("quests/theMaster", "theFocusCrystal");
	game.revealWorldMapLocation("Master's Army");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
