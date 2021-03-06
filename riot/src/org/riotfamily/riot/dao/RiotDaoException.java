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
 *   Carsten Woelk [cwoelk at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.riot.dao;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataAccessException;

/**
 * @author Carsten Woelk [cwoelk at neteye dot de]
 * @since 6.5
 */
public class RiotDaoException extends DataAccessException
		implements MessageSourceResolvable {

	private String code;

	private Object[] arguments;

	public RiotDaoException(String code, String msg) {
		this(code, new Object[] {}, msg);
	}

	public RiotDaoException(String code, Object[] arguments, String msg) {
		this(code, arguments, msg, null);
	}

	public RiotDaoException(String code, Object[] arguments, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
		this.arguments = arguments;
	}

	public Object[] getArguments() {
		return this.arguments;
	}

	public String getCode() {
		return this.code;
	}

	public String[] getCodes() {
		return new String[] { code };
	}

	public String getDefaultMessage() {
		return getMessage();
	}

}
