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
package org.riotfamily.riot.form.ui;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.riotfamily.forms.Form;
import org.riotfamily.forms.factory.FormRepository;
import org.riotfamily.riot.editor.EditorRepository;
import org.riotfamily.riot.editor.FormChooserDefinition;
import org.riotfamily.riot.editor.ObjectEditorDefinition;
import org.riotfamily.riot.list.ui.ListService;
import org.springframework.transaction.PlatformTransactionManager;

public class FormChooserController extends FormController {

	public FormChooserController(EditorRepository editorRepository,
			FormRepository formRepository,
			PlatformTransactionManager transactionManager,
			ListService listService) {

		super(editorRepository, formRepository, transactionManager, listService);
	}

	protected String getFormId(HttpServletRequest request) {
		String formId = request.getParameter(getFormIdParam());
		if (formId == null) {
			formId = super.getFormId(request);
		}
		return formId;
	}
	
	protected Map<String, Object> createModel(Form form, ObjectEditorDefinition editorDefinition,
			HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> model = super.createModel(form, editorDefinition, request, response);
		FormChooserDefinition chooser =	(FormChooserDefinition) editorDefinition;
		model.put("formId", form.getId());
		if (form.isNew()) {
			model.put("options", chooser.createOptions(
					FormUtils.getParentId(form),
					form.getFormContext().getMessageResolver()));
		}
		return model;
	}


}
