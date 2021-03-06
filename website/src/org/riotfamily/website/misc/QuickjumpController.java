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
package org.riotfamily.website.misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller that can be used as action for a quick-jump form.
 * 
 * The controller reads a URL from a request parameter and send a redirect to
 * that URL. If the parameter is either <code>null</code>, an empty string or 
 * starts with with a hash character ('#'), a <strong>204 No Content</strong>
 * response is sent.
 *  
 * @author Felix Gnass [fgnass at neteye dot de]
 */
public class QuickjumpController implements Controller {

	private String urlParameter = "url";
	
	public void setUrlParameter(String urlParameter) {
		this.urlParameter = urlParameter;
	}

	public ModelAndView handleRequest(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		String url = request.getParameter(urlParameter);
		if (StringUtils.hasLength(url) && !url.startsWith("#")) {
			return new ModelAndView(new RedirectView(url));
		}
		else {
			response.sendError(HttpServletResponse.SC_NO_CONTENT);
			return null;
		}
	}
}
