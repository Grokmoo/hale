
function startConversation(game, parent, target, conversation) {
	conversation.addText("Hello again surfacer.");

	if (game.get("gateQuestStarted") == null) {
		conversation.addResponse("You said you could help me return to the surface.", "convo02");
		conversation.addResponse("Farewell.", "onExit");
	} else {
		addConvoGateQuest(game, parent, target, conversation);
	}
}

function onExit(game, parent, target, conversation) {
    conversation.exit();
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Yes, I did say that.  However, it is not quite so simple.");
    
    conversation.addResponse("What do you mean?", "convo03");
    conversation.addResponse("You better not have betrayed me!", "convo02b");
}

function convo02b(game, parent, target, conversation) {
    conversation.addText("Calm down, I have not betrayed you.");
    conversation.addText("I will help you.  Besides, you have little choice but to trust me.");
    
    conversation.addResponse("Very well.  So how do you help me?", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Well, there is a gate to the surface about a day's journey south of here.");
    
    conversation.addText("However, it isn't as simple as walking through it.");
    
    conversation.addResponse("Continue.", "convo04");
    conversation.addResponse("Why not?", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("The path to the gate is guarded by a clan of drakes.  Fearsome creatures, but even that is not the real problem.");
    
    conversation.addResponse("So what is the real problem?", "convo05");
    conversation.addResponse("Out with it then, what's the problem?", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("The gate is protected by ancient magic and cannot be opened except with a special key.  Unfortunately, the pieces of the key have been scattered about these caves over the years.");
    
    conversation.addText("I happen to have one piece of the key here, but there are three others that you will need in order to open the gate.");
    
    conversation.addResponse("This sounds difficult.  Is there no other way to reach the surface?", "convo05b");
    conversation.addResponse("So if the only gate to the surface is sealed, how do you know of my language?", "convo05c");
    conversation.addResponse("Where are the other pieces of the key located?", "convo06");
}

function convo05b(game, parent, target, conversation) {
    conversation.addText("There is no other way known to our people.  These caves stretch for hundreds of miles though, so it is possible there is another entrance somewhere far away.");
    
    conversation.addResponse("So if the only gate to the surface is sealed, how do you know of my language?", "convo05c");
    conversation.addResponse("Where are the other pieces of the key located?", "convo06");
}

function convo05c(game, parent, target, conversation) {
    conversation.addText("There are other keys, but they are possessed by surfacers who sometimes come through to trade.");
    
    conversation.addText("If you wish to wait at least a few months for another trader to come, you may.  I had the sense that you had some urgency in your mission, however.");
    
    conversation.addResponse("This sounds difficult.  Is there no other way to reach the surface?", "convo05b");
    conversation.addResponse("Where are the other pieces of the key located?", "convo06");
}

function convo06(game, parent, target, conversation) {
	game.put("gateQuestStarted", true);
	
	game.revealWorldMapLocation("Mushroom Forest");
	game.runExternalScript("quests/theMaster", "startGate");

    conversation.addText("There are three pieces you must collect.  One is with the lizard people, who live in a great underground lake to the northeast.  Another is with the deep dwarves, who live to the north of the forest.  Finally, I believe one piece is still in its original resting place - a great tomb to the northwest.");
	
	addConvoGateQuest(game, parent, target, conversation);
}

function addConvoGateQuest(game, parent, target, conversation) {
	conversation.addText("Do you have any questions?");

	conversation.addResponse("If the only gate to the surface is sealed, how do you know of my language?", "askLanguage");
	conversation.addResponse("Tell me of the lizard people.", "askLizard");
	conversation.addResponse("Tell me of the deep dwarves.", "askDwarves");
	conversation.addResponse("Tell me about the tomb.", "askTomb");
	conversation.addResponse("I will find the pieces of the key and return to you.  Farewell.", "onExit");
}

function askLanguage(game, parent, target, conversation) {
	conversation.addText("There are other keys, but they are possessed by surfacers who sometimes come through to trade.");
    
    conversation.addText("If you wish to wait at least a few months for another trader to come, you may.  I had the sense that you had some urgency in your mission, however.");
	
	conversation.addResponse("Back to my other questions.", "addConvoGateQuest");
	conversation.addResponse("I had best get back to it then.  Farewell.", "onExit");
}

function askLizard(game, parent, target, conversation) {
	conversation.addText("The lizard people live around a great lake to the northeast.  To reach it, travel north to the mushroom forest and then east, over a great river and to the lake.");
	conversation.addText("The lizard people are not friendly to outsiders, but you may be able to negotiate to get the key from them if you can talk to their leader.");
	
	conversation.addResponse("Back to my other questions.", "addConvoGateQuest");
	conversation.addResponse("I had best get back to it then.  Farewell.", "onExit");
}

function askDwarves(game, parent, target, conversation) {
	conversation.addText("The deep dwarves have built a vast underground city to the north of here.  To reach it, head north straight through the mushroom forest.");
	conversation.addText("There is a group of slavers camped south of the city who will likely attack you on sight.");
	conversation.addText("However, once you reach the city itself you can most likely avoid hostilities.");

	conversation.addResponse("Back to my other questions.", "addConvoGateQuest");
	conversation.addResponse("I had best get back to it then.  Farewell.", "onExit");
}

function askTomb(game, parent, target, conversation) {
	conversation.addText("The tomb containing the key fragment is located north, and then west.");
	conversation.addText("Originally, the entire key was located there, but looters have robbed the place of most of its treasure.  The entire place is infested with the undead.");
	
	conversation.addResponse("Tell me more about the tomb.", "askTomb2");
	conversation.addResponse("Back to my other questions.", "addConvoGateQuest");
	conversation.addResponse("I had best get back to it then.  Farewell.", "onExit");
}

function askTomb2(game, parent, target, conversation) {
	conversation.addText("The tomb was created hundreds of years ago, after a great battle between the surfacers and the deep dwarves.  The result of that battle was the creation of a powerful spell sealing off the underground from the surface.");
	
	conversation.addText("If you go to the dwarven city, they will likely know more.");
	
	conversation.addResponse("Back to my other questions.", "addConvoGateQuest");
	conversation.addResponse("I had best get back to it then.  Farewell.", "onExit");
}