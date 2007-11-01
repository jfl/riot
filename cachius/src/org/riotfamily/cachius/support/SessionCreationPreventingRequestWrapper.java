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
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 * 
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.cachius.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * @author Felix Gnass [fgnass at neteye dot de]
 * @since 6.5
 */
public class SessionCreationPreventingRequestWrapper 
		extends HttpServletRequestWrapper {
	
	private boolean sessionExists;
	
	public SessionCreationPreventingRequestWrapper(HttpServletRequest request) {
		super(request);
		sessionExists = request.getSession(false) != null;
	}
	
	public HttpSession getSession() {
		return getSession(true);
	}
	
	public HttpSession getSession(boolean create) {
		if (create && !sessionExists) {
			throw new IllegalStateException("CacheableControllers must not " +
					"create new HTTP sessions. Make sure that the session " +
					"exists before you invoke the Controller.");
		}
		return super.getSession(create);
	}

}
