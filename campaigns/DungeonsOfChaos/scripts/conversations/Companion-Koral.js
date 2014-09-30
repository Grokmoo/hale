function startConversation(game, parent, target, conversation) {
    conversation.addTextWithFont("Before you stands a tall, sturdy woman.  She has blue skin with a sheen almost like a fish's scales, and long green hair.", "medium-italic");

    conversation.addTextWithFont("She looks you over carefully before speaking.", "medium-italic");
    
    conversation.addTextWithFont("Can I do something for you, stranger?.", "medium");

    conversation.addResponse("Nothing now, thanks.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
