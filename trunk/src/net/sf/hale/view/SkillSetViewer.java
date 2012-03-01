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

package net.sf.hale.view;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.rules.Skill;
import net.sf.hale.widgets.ExpandableWidget;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.ScrollPane;

/**
 * A widget for viewing the set of skills possessed by a given Creature
 * @author Jared Stephen
 *
 */

public class SkillSetViewer extends ScrollPane {
	private Creature parent;
	
	private final List<SkillViewer> viewers;
	private final DialogLayout content;
	
	public SkillSetViewer() {
		setFixed(ScrollPane.Fixed.HORIZONTAL);
		
		viewers = new ArrayList<SkillViewer>();
		
		content = new DialogLayout();
		content.setTheme("content");
		setContent(content);
	}
	
	public void updateContent(Creature parent) {
		if (parent != this.parent) {
			this.parent = parent;
			this.viewers.clear();

			content.removeAllChildren();

			DialogLayout.Group mainH = content.createParallelGroup();
			DialogLayout.Group mainV = content.createSequentialGroup();

			for (Skill skill : Game.ruleset.getAllSkills()) {
				if (!skill.canUse(parent)) continue;

				SkillViewer viewer = new SkillViewer(skill);
				viewers.add(viewer);
				mainH.addWidget(viewer);
				mainV.addWidget(viewer);
			}

			content.setHorizontalGroup(mainH);
			content.setVerticalGroup(mainV);
		}
		
		for (SkillViewer viewer : viewers) {
			viewer.update();
		}
	}
	
	private class SkillViewer extends ExpandableWidget {
		private Skill skill;
		
		private SkillViewer(Skill skill) {
			super(skill.getIcon());
			this.skill = skill;
		}
		
		@Override protected void appendDescriptionMain(StringBuilder sb) {
			sb.append("<div style=\"font-family: vera-bold;\">");
			sb.append(skill.getName()).append("</div>");
			
			int total = parent.getSkillModifier(skill);
			int ranks = parent.getSkillSet().getRanks(skill);
			int modifier = total - ranks;
			
			sb.append("<div style=\"font-family: vera\"><span style=\"font-family: vera-blue;\">").append(ranks);
			sb.append("</span> + <span style=\"font-family: vera-green;\">").append(modifier);
			sb.append("</span> = <span style=\"font-family: vera-bold;\">").append(total).append("</span>");
			sb.append("</div>");
		}
		
		@Override protected void appendDescriptionDetails(StringBuilder sb) {
			if (skill.isRestricted()) {
				sb.append("<p>Restricted to <span style=\"font-family: red;\">");
				sb.append(skill.getRestrictToRole()).append("</span></p>");
			}
			
			sb.append("<p>Key Attribute: <span style=\"font-family: blue;\">");
			sb.append( skill.getKeyAttribute().name );
			sb.append("</span></p>");
			
			if (!skill.usableUntrained()) {
				sb.append("<div style=\"font-family: green;\">Requires training</div>");
			}
			
			if (skill.hasArmorPenalty()) {
				sb.append("<div style=\"font-family: green;\">Armor Penalty applies</div>");
			}
			
			sb.append("<div style=\"margin-top: 1em;\">");
			sb.append(skill.getDescription()).append("</div>");
		}
	}
}
