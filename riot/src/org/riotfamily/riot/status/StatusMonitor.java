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
package org.riotfamily.riot.status;

import java.util.Collection;
import java.util.Locale;


/**
 * Interface that enables classes to display status messages on the Riot 
 * dashboard.
 *   
 * @author Felix Gnass [fgnass at neteye dot de]
 */
public interface StatusMonitor {

	/**
	 * Implementors may return a Collection of {@link StatusMessage} instances. 
	 * Make sure that you properly escape all parts of the message that could 
	 * contain user-generated content to prevent XSS attacks.  
	 */
	public Collection<StatusMessage> getMessages(Locale locale);
		
}
