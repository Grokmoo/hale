/*
 * Hale is highly moddable tactical RPG.
 * Copyright (C) 2011 Jared Stephen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.sf.hale.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Skill;
import net.sf.hale.rules.SkillSet;
import net.sf.hale.widgets.ExpandableScrollPane;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleIntegerModel;

public class SkillEditor extends ExpandableScrollPane implements Updateable {
	private Creature parent;
	private SkillSet skills;
	
	private final List<Label> skillLabels;
	private final List<ValueAdjusterInt> skillAdjusters;
	
	private final Widget skillsContent;
	
	public SkillEditor() {
		super();
		this.setTheme("/scrollpane");
		
		skillsContent = new Widget();
		skillsContent.setTheme("");
		this.setContent(skillsContent);		
		
		skillLabels = new ArrayList<Label>();
		skillAdjusters = new ArrayList<ValueAdjusterInt>();
	}
	
	@Override public void update() {
		skillsContent.removeAllChildren();
		skillLabels.clear();
		skillAdjusters.clear();
		
		if (skills == null) return;
		
		int index = 0;
		
		for (Skill skill : Game.ruleset.getAllSkills()) {
			if (!skill.canUse(parent)) continue;
			
			int ranks = skills.getRanks(skill);
			
			Label skillName = new Label(skill.getName());
			skillName.setTheme("/labelblack");
			skillName.setPosition(80, 10 + index * 25);
			skillsContent.add(skillName);
			
			ValueAdjusterInt skillRanks = new ValueAdjusterInt(new SimpleIntegerModel(0, 999, ranks));
			skillRanks.setTheme("/valueadjuster");
			skillRanks.setSize(70, 20);
			skillRanks.setPosition(0, index * 25);
			skillRanks.getModel().addCallback(new SkillAdjusterCallback(skill, skillRanks));
			skillsContent.add(skillRanks);
			
			index++;
		}
	}
	
	public void setCreature(Creature parent) {
		this.parent = parent;
		this.skills = parent.getSkillSet();
		
		update();
	}
	
	private class SkillAdjusterCallback implements Runnable {
		private final Skill skill;
		private final ValueAdjusterInt adjuster;
		
		public SkillAdjusterCallback(Skill skill, ValueAdjusterInt adjuster) {
			this.skill = skill;
			this.adjuster = adjuster;
		}
		
		@Override public void run() {
			skills.setRanks(skill.getID(), adjuster.getValue());
		}
	}
}
