/*
 * A semi-transparent halo appears and hovers over the target
 * You need to set the position and duration
 */

var g1 = game.createParticleGenerator("Point", "Continuous", "particles/haloCross", 0.5);
g1.setDrawParticlesInOpaque(true);
g1.setStopParticlesAtOpaque(false);
g1.setDurationDistribution(game.getFixedDistribution(2.0));
var color = game.getFixedDistribution(1.0);
g1.setRedDistribution(color);
g1.setGreenDistribution(color);
g1.setBlueDistribution(color);
g1.setAlphaDistribution(game.getFixedDistribution(0.5));
g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.2));

g1;