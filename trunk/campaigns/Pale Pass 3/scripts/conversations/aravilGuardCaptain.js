function startConversation(game, parent, target, conversation) {
    conversation.addText("Well met.  What can I do for you, citizen?");
    conversation.addResponse("Nothing today, thanks.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
