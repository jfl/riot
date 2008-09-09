/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Riot.
 *
 * The Initial Developer of the Original Code is
 * Neteye GmbH.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.components.config;

import org.riotfamily.common.beans.xml.GenericBeanDefinitionParser;
import org.riotfamily.common.beans.xml.GenericNamespaceHandlerSupport;
import org.riotfamily.common.beans.xml.NestedPropertyDecorator;
import org.riotfamily.components.controller.ComponentListController;

/**
 * NamespaceHandler that handles the <code>controller</code> namspace as
 * defined in <code>controller.xsd</code> which can be found in the same package.
 */
public class ControllerNamespaceHandler extends GenericNamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("list", new GenericBeanDefinitionParser(
				ComponentListController.class)
				.addReference("locator")
				.addTranslation("valid", "validComponentTypes")
				.addTranslation("initial", "initialComponentTypes")
				.addTranslation("min", "minComponents")
				.addTranslation("max", "maxComponents"));
		
		registerSpringBeanDefinitionParser("locator", 
				new NestedPropertyDecorator("locator"));
	}

}