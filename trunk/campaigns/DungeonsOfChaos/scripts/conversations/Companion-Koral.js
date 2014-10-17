function startConversation(game, parent, talker, conv) {
	if (!parent.get("inParty")) {
	    conv.addTextWithFont("Before you stands a tall, sturdy woman.  She has blue skin with a sheen almost like a fish's scales, and long green hair.", "medium-italic");
        conv.addTextWithFont("She looks you over carefully before speaking.", "medium-italic");
	    conv.addTextWithFont("I am Koral.  Can I do something for you, stranger?.", "medium");

	    conv.addResponse("Would you be interested in joining me in exploring the dungeon?", "askJoin");
	} else {
	    conv.addTextWithFont("Before you stands Koral, your companion.", "medium-italic");
	    conv.addTextWithFont("What do you need?", "medium");
	}
	
    conv.addResponse("Nothing now, thanks.", "onExit");
}

function askJoin(game, parent, talker, conv) {
    game.addCompanion(parent);
	parent.put("inParty", true);

    conv.addTextWithFont("It is better to go together than alone.  I will join you.", "medium");
	
	conv.addResponse("Let's get going, then.", "onExit");
}

function onExit(game, parent, talker, conv) {
    conv.exit();
}

