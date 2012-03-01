
function onPlayerEnter(game, player, trigger) {
    if (game.get("dwarvenSlaversTryEntrance") != null) {
        game.startConversation(INSERT_PARENT, INSERT_TARGET, "INSERT_SCRIPT_ID");
    }
}
