
function onAreaLoadFirstTime(game, area, transition) {
    game.showCutscene("intro");
    
    game.runExternalScript("quests/theMaster", "startQuest");
}
