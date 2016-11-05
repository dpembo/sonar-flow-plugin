/*
 * i8c
 * Copyright (C) 2016 i8c NV
 * mailto:contact AT i8c DOT be
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package be.i8c.codequality.sonar.plugins.sag.webmethods.flow.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import com.sonar.sslr.api.AstNode;

import be.i8c.codequality.sonar.plugins.sag.webmethods.flow.check.type.TopLevelCheck;
import be.i8c.codequality.sonar.plugins.sag.webmethods.flow.sslr.FlowGrammar;
import be.i8c.codequality.sonar.plugins.sag.webmethods.flow.sslr.FlowLexer.FlowAttTypes;

@Rule(key="S00009",name = "In the branch step if the \"switch\" property is 'null', "
		+ "then the \"evaluate labels\" property must be set to 'true'.", 
		priority = Priority.MINOR, tags = {Tags.DEBUG_CODE, Tags.BAD_PRACTICE})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("2min")
public class BranchCheck extends TopLevelCheck{
	
	final static Logger logger = LoggerFactory.getLogger(BranchCheck.class);
	String test = "test";
	
	@Override
	public void init() {
		logger.debug("++ Initializing {} ++", this.getClass().getName());
		subscribeTo(FlowGrammar.BRANCH);
	}
	
	@Override
	public void visitNode(AstNode astNode) {
		AstNode attributes = astNode.getFirstChild(FlowGrammar.ATTRIBUTES);
		AstNode switchAttNode = attributes.getFirstChild(FlowAttTypes.SWITCH);
		if (checkSwitch(switchAttNode)){
			logger.debug("++ Found an empty switch statement ++");
			AstNode labelExpNode = attributes.getFirstChild(FlowAttTypes.LABELEXPRESSIONS);
			if(checkLabelExp(labelExpNode)) {
				logger.debug("++ Found an empty or not set to true label expression ++");
				getContext().createLineViolation(this, "Set label expression to true or create a switch value.", astNode);
			}	
		}		
	}

	private boolean checkSwitch(AstNode switchAttNode) {
		if (switchAttNode == null ) return true;
		if (switchAttNode != null && switchAttNode.getTokenOriginalValue().equalsIgnoreCase("")) return true;
		return false;
	}
	private boolean checkLabelExp (AstNode labelExpNode) {
		if (labelExpNode == null ) return true;
		if (labelExpNode != null  
				&& !labelExpNode.getTokenOriginalValue().equalsIgnoreCase("true")) return true;
		return false;
	}
}