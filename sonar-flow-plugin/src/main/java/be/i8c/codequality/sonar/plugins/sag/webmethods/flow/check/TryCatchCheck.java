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

@Rule(key = "S00001", name = "Try-catch should be the top implementation", priority = Priority.MINOR, tags = {
		Tags.ERROR_HANDLING })
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EXCEPTION_HANDLING)
@SqaleConstantRemediation("2min")
public class TryCatchCheck extends TopLevelCheck {

	final static Logger logger = LoggerFactory.getLogger(TryCatchCheck.class);

	@Override
	public void init() {
		logger.debug("++ Initializing " + this.getClass().getName() + " ++");
		subscribeTo(FlowGrammar.FLOW);
	}

	@Override
	public void visitNode(AstNode astNode) {
		AstNode mainNode = getContent(astNode).getFirstChild(FlowGrammar.SEQUENCE);
		String mainType = getSequenceType(mainNode);
		if (mainType != null && mainType.equalsIgnoreCase("SUCCESS")) {
			AstNode tryNode = getContent(mainNode).getFirstChild(FlowGrammar.SEQUENCE);
			String tryType = getSequenceType(tryNode);
			if (tryType != null && tryType.equalsIgnoreCase("FAILURE")) {
				AstNode catchNode = getContent(mainNode).getLastChild(FlowGrammar.SEQUENCE);
				String catchType = getSequenceType(catchNode);
				if (catchType != null && catchType.equalsIgnoreCase("DONE")) {
					return;
				}
			}
		}
		getContext().createLineViolation(this, "Create try-catch sequence", astNode);
	}

	public String getSequenceType(AstNode sequenceNode) {
		if (sequenceNode != null) {
			AstNode attributes = sequenceNode.getFirstChild(FlowGrammar.ATTRIBUTES);
			if (attributes != null) {
				AstNode exitOn = attributes.getFirstChild(FlowAttTypes.EXITON);
				if (exitOn != null) {
					return exitOn.getTokenValue();
				}
			}
		}
		return null;
	}

	public AstNode getContent(AstNode sequenceNode) {
		if (sequenceNode != null) {
			return sequenceNode.getFirstChild(FlowGrammar.CONTENT);
		}
		return null;
	}

}