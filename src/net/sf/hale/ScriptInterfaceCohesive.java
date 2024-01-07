package net.sf.hale;


import java.io.File;
import java.io.IOException;

import net.sf.hale.ability.Scriptable;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.Entity;
import net.sf.hale.entity.Inventory;
import net.sf.hale.particle.AngleDistributionBase;
import net.sf.hale.particle.Animation;
import net.sf.hale.particle.CircleParticleGenerator;
import net.sf.hale.particle.DistanceDistributionBase;
import net.sf.hale.particle.DistributionBase;
import net.sf.hale.particle.EquallySpacedAngleDistribution;
import net.sf.hale.particle.FixedAngleDistribution;
import net.sf.hale.particle.FixedDistribution;
import net.sf.hale.particle.FixedDistributionWithBase;
import net.sf.hale.particle.GaussianAngleDistribution;
import net.sf.hale.particle.GaussianDistribution;
import net.sf.hale.particle.GaussianDistributionWithBase;
import net.sf.hale.particle.LineParticleGenerator;
import net.sf.hale.particle.ParticleGenerator;
import net.sf.hale.particle.RectParticleGenerator;
import net.sf.hale.particle.SpeedDistributionBase;
import net.sf.hale.particle.UniformAngleDistribution;
import net.sf.hale.particle.UniformArcDistribution;
import net.sf.hale.particle.UniformDistribution;
import net.sf.hale.particle.UniformDistributionWithBase;
import net.sf.hale.particle.VelocityTowardsPointDistribution;
import net.sf.hale.resource.ResourceManager;
import net.sf.hale.rules.Attack;
import net.sf.hale.rules.Merchant;
import net.sf.hale.util.AreaUtil;
import net.sf.hale.util.FileUtil;
import net.sf.hale.util.Logger;
import net.sf.hale.util.Point;
import net.sf.hale.view.ConversationPopup;



/*
 * Class containing methods that were implemented in the ScriptInterface class and were moved for better cohesion
 * This class contains methods that create statistical distributions and animations
 */

public class ScriptInterfaceCohesive {
	public EquallySpacedAngleDistribution getEquallySpacedAngleDistribution(float min, float max, float stepSize, float numParticles, float jitter) {
		return new EquallySpacedAngleDistribution(min, max, stepSize, numParticles, jitter);
	}
	
	public FixedDistribution getFixedDistribution(float value) {
		return new FixedDistribution(value);
	}
	
	public GaussianDistribution getGaussianDistribution(float mean, float stddev) {
		return new GaussianDistribution(mean, stddev);
	}
	
	public GaussianDistributionWithBase getGaussianDistributionWithBase(DistributionBase base, float mult, float offset, float stddevFraction) {
		return new GaussianDistributionWithBase(base, mult, offset, stddevFraction);
	}
	
	public DistanceDistributionBase getDistanceDistributionBase(Point screenPoint) {
		return new DistanceDistributionBase(screenPoint);
	}
	
	public SpeedDistributionBase getSpeedDistributionBase() {
		return new SpeedDistributionBase();
	}
	
	public AngleDistributionBase getAngleDistributionBase() {
		return new AngleDistributionBase();
	}
	
	public VelocityTowardsPointDistribution getVelocityTowardsPointDistribution(Point dest, float time) {
		return new VelocityTowardsPointDistribution(dest, time);
	}
	
	public UniformAngleDistribution getUniformAngleDistribution(float min, float max) {
		return new UniformAngleDistribution(min, max);
	}
	
	public GaussianAngleDistribution getGaussianAngleDistribution(float mean, float stddev) {
		return new GaussianAngleDistribution(mean, stddev);
	}
	
	public UniformArcDistribution getUniformArcDistribution(float magMin, float magMax, float angleMin, float angleMax) {
		return new UniformArcDistribution(magMin, magMax, angleMin, angleMax);
	}
	
	public FixedAngleDistribution getFixedAngleDistribution(float magMin, float magMax, float angle) {
		return new FixedAngleDistribution(magMin, magMax, angle);
	}
	
	public UniformDistribution getUniformDistribution(float min, float max) {
		return new UniformDistribution(min, max);
	}
	
	public UniformDistributionWithBase getUniformDistributionWithBase(DistributionBase base, float mult, float offset, float plusOrMinusFraction) {
		return new UniformDistributionWithBase(base, mult, offset, plusOrMinusFraction);
	}
	
	public FixedDistributionWithBase getFixedDistributionWithBase(DistributionBase base, float mult, float offset) {
		return new FixedDistributionWithBase(base, mult, offset);
	}
	
	public ParticleGenerator createParticleGenerator(String type, String mode, String particle, float numParticles) {
		if (type.equalsIgnoreCase("Point")) {
			return new ParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Line")) {
			return new LineParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Rect")) {
			return new RectParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else if (type.equalsIgnoreCase("Circle")) {
			return new CircleParticleGenerator(ParticleGenerator.Mode.valueOf(mode), particle, numParticles);
		} else {
			return null;
		}
	}
	
	public Animation createAnimation(String baseFrame) {
		return new Animation(baseFrame);
	}
	
	public Animation createAnimation(String baseFrame, float duration) {
		try {
			return new Animation(baseFrame, duration);
		} catch (Exception e) {
			Logger.appendToErrorLog("Error creating animation.", e);
		}
		
		return null;
	}
	
	public Animation getBaseAnimation(String id) {
		return Game.particleManager.getAnimation(id);
	}
	
	public ParticleGenerator getBaseParticleGenerator(String id) {
		return Game.particleManager.getParticleGenerator(id);
	}
	
	public void runAnimationWait(Animation animation) {
		Game.particleManager.add(animation);
		
		try {
			Thread.sleep((long) (animation.getSecondsRemaining() * 1000.0f));
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void runAnimationNoWait(Animation animation) {
		Game.particleManager.add(animation);
	}
	
	
	public void runParticleGeneratorWait(ParticleGenerator generator) {
		Game.particleManager.add(generator);
		
		try {
			Thread.sleep((long) (generator.getTimeLeft() * 1000.0f));
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void runParticleGeneratorNoWait(ParticleGenerator generator) {
		Game.particleManager.add(generator);
	}
	
	
	public void startConversation(Entity parent, Entity target, String convoScriptID) {
		String scriptContents = ResourceManager.getScriptResourceAsString(convoScriptID);
		Scriptable script = new Scriptable(scriptContents, convoScriptID, false);
		
		ConversationPopup popup = new ConversationPopup(parent, target, script);
		popup.startConversation();
	}
	

	
	public Point[] getAdjacentHexes(Point p) {
		return AreaUtil.getAdjacentTiles(p);
	}
	
	public Point[] getAdjacentHexes(int x, int y) {
		return AreaUtil.getAdjacentTiles(x, y);
	}
	

	
	
	public void showMerchant(String merchantName) {
		Merchant merchant = Game.curCampaign.getMerchant(merchantName);
		if (merchant == null) {
			Logger.appendToErrorLog("Error locating merchant: " + merchantName);
			return;
		}
		
		Game.mainViewer.setMerchant(merchant);
		Game.mainViewer.merchantWindow.setVisible(true);
	}
	
	
	public boolean rangedTouchAttack(Creature attacker, Creature defender) {
		boolean success = false;
		try {
			DelayedAttackCallback cb = Game.areaListener.getCombatRunner().creatureTouchAttack(attacker, defender, true);
			
			if (cb != null) {
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}
				
				success = cb.isAttackHit();
			}
		} catch (Exception e) {
			Logger.appendToErrorLog("Error performing ranged touch attack.", e);
		}
		
		return success;
	}
	
	public boolean meleeTouchAttack(Creature attacker, Creature defender) {
		boolean result = false;
		
		try {
			DelayedAttackCallback cb = Game.areaListener.getCombatRunner().creatureTouchAttack(attacker, defender, false);
			
			if (cb != null) {
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}
				
				result = cb.isAttackHit();
			}
			
		} catch (Exception e) {
			Logger.appendToErrorLog("Error performing melee touch attack.", e);
		}
		
		return result;
	}
	
	/**
	 * Causes the attacker to perform an animated attack against the target.  No AP is deducted,
	 * but the attack is otherwise as normal.  This function will block until the attack completes,
	 * and thus it cannot be run from the main thread
	 * @param attacker
	 * @param target
	 * @return true if the attack was completed, false if it did not complete for any reason
	 */
	
	
	public boolean singleAttackAnimate(Creature attacker, Creature target) {
		boolean result = false;

		try {
			DelayedAttackCallback cb = Game.areaListener.getCombatRunner().creatureSingleAttackAnimate(
					attacker, target, Inventory.Slot.MainHand);

			if (cb != null) {
				// wait for cb to finish
				synchronized(cb) {
					while (cb.isAlive()) {
						cb.wait();
					}
				}

				result = cb.isAttackHit();
			}
		} catch (InterruptedException e) {
			// the attack was interrupted and did not take place
			result = false;
		}

		return result;
	}
	
	/**
	 * Creates a new attack, computes whether the attack is a hit and the amount of damage and returns it
	 * No scripts are run on either the parent or target
	 * @param attacker
	 * @param defender
	 * @return the newly created attack
	 */

	public Attack getMainHandAttack(Creature attacker, Creature defender) {
		return attacker.getAttack(defender, Inventory.Slot.MainHand.toString());
	}
	
	/**
	 * Creates a new attack, computes whether the attack is a hit and the amount of damage and returns it
	 * No scripts are run on either the parent or target
	 * @param attacker
	 * @param defender
	 * @return the newly created attack
	 */
	
	public Attack getOffHandAttack(Creature attacker, Creature defender) {
		return attacker.getAttack(defender, Inventory.Slot.OffHand.toString());
	}
	
	public void singleAttack(Creature attacker, Point position) {
		singleAttack(attacker, Game.curCampaign.curArea.getCreatureAtGridPoint(position));
	}
	
	public void singleAttack(Creature attacker, Creature defender) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, Inventory.Slot.MainHand);
	}
	
	public void singleAttack(Creature attacker, Creature defender, String slot) {
		Game.areaListener.getCombatRunner().creatureSingleAttack(attacker, defender, Inventory.Slot.valueOf(slot));
	}
	
	
	/**
	 * Writes the current content of the messages box on the bottom right of the screen to a file
	 */
	
	public void writeMessageLog() {
		try {
			FileUtil.writeStringToFile(new File("message.html"), Game.mainViewer.getMessageBoxContents());
		} catch (IOException e) {
			Logger.appendToErrorLog("Error writing message log", e);
		}
	}
	
	
}
