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
package org.riotfamily.components.editor;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.riotfamily.cachius.CacheService;
import org.riotfamily.common.log.RiotLog;
import org.riotfamily.common.util.FormatUtils;
import org.riotfamily.common.web.util.CapturingResponseWrapper;
import org.riotfamily.components.cache.ComponentCacheUtils;
import org.riotfamily.components.config.ContentFormRepository;
import org.riotfamily.components.dao.ComponentDao;
import org.riotfamily.components.model.Component;
import org.riotfamily.components.model.ComponentList;
import org.riotfamily.components.model.Content;
import org.riotfamily.components.model.ContentContainer;
import org.riotfamily.components.render.component.ComponentRenderer;
import org.riotfamily.components.render.component.EditModeComponentDecorator;
import org.riotfamily.riot.security.AccessController;
import org.riotfamily.riot.security.auth.RiotUser;
import org.riotfamily.riot.security.session.LoginManager;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Service bean to edit ComponentLists and ComponentVersions.
 */
@Transactional
public class ComponentEditorImpl implements ComponentEditor,
		MessageSourceAware, ServletContextAware {

	private RiotLog log = RiotLog.get(ComponentEditorImpl.class);

	private ComponentDao componentDao;
	
	private CacheService cacheService;
	
	private ComponentRenderer renderer;

	private Map<String, Map<String, Object>> tinyMCEProfiles;

	private MessageSource messageSource;
	
	private ServletContext servletContext;
	
	public ComponentEditorImpl(ComponentDao componentDao, 
			CacheService cacheService, ComponentRenderer renderer, 
			ContentFormRepository formRepository) {
		
		this.componentDao = componentDao;
		this.cacheService = cacheService;
		this.renderer = new EditModeComponentDecorator(renderer, formRepository);
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	public Map<String, Map<String, Object>> getTinyMCEProfiles() {
		return this.tinyMCEProfiles;
	}

	public void setTinyMCEProfiles(Map<String, Map<String, Object>> tinyMCEProfiles) {
		this.tinyMCEProfiles = tinyMCEProfiles;
	}

	/**
	 * Returns the value of the given property.
	 */
	public String getText(Long contentId, String property) {
		Content content = componentDao.loadContent(contentId);
		Object value = content.getValue(property);
		return value != null ? value.toString() : null;
	}

	/**
	 * Sets the given property to a new value.
	 */
	public void updateText(Long contentId, String property, String text) {
		Content content = componentDao.loadContent(contentId);
		content.setValue(property, text);
	}

	/**
	 *
	 */
	public String[] updateTextChunks(Long componentId, String property,
			String[] chunks) {

		String[] html = new String[chunks.length];
		Component component = componentDao.loadComponent(componentId);
		component.setValue(property, chunks[0]);
		html[0] = renderComponent(component);
		
		ComponentList list = component.getList();
		int offset = list.indexOf(component);
		for (int i = 1; i < chunks.length; i++) {
			html[i] = insertComponent(list.getId(), offset + i, 
					component.getType(),
					Collections.singletonMap(property, (Object) chunks[i]));
		}
		return html;
	}

	public String[] getComponentLabels(String[] types, HttpServletRequest request) {
		Locale locale = RequestContextUtils.getLocale(request);
		String[] labels = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			labels[i]= messageSource.getMessage("component." + types[i], null, 
					FormatUtils.xmlToTitleCase(types[i]), locale);
		}
		return labels;
	}
	
	/**
	 * Creates a new Component and inserts it in the list identified
	 * by the given id.
	 */
	@SuppressWarnings("unchecked")
	public String insertComponent(Long listId, int position, String type, String properties) {
		// NOTE: The initial properties are passed as JSON String because DWR
		// currently doesn't support Map<String, ?>.
		return insertComponent(listId, position, type, JSONObject.fromObject(properties));
	}
	
	private String insertComponent(Long listId, int position, String type,
			Map<String, ?> properties) {

		ComponentList componentList = componentDao.loadComponentList(listId);
		Component component = createComponent(type, properties);
		componentList.insertComponent(component, position);
		return renderComponent(component);
	}

	/**
	 * Creates a new Component of the given type.
	 *
	 * @param type The type of the version to create
	 * @param properties Properties of the version to create
	 * @return The newly created component
	 */
	private Component createComponent(String type, Map<String, ?> properties) {
		Component component = new Component(type);
		component.wrap(properties);
		componentDao.saveContent(component);
		return component;
	}
	
	@SuppressWarnings("unchecked")
	public String setType(Long componentId, String type, String properties) {
		Component component = componentDao.loadComponent(componentId);
		component.setType(type);
		if (properties != null) {
			component.wrap(JSONObject.fromObject(properties));
		}
		return renderComponent(component);
	}
	
	public String renderComponent(Long componentId) {
		Component component = componentDao.loadComponent(componentId);
		return renderComponent(component);
	}

	private String renderComponent(Component component) {

		try {
			ComponentList list = component.getList();
			StringWriter sw = new StringWriter();
			HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
			HttpServletResponse response = getCapturingResponse(sw);
			renderer.render(component, list.indexOf(component), list.getSize(), request, response);
			return sw.toString();
		}
		catch (Exception e) {
			log.error("Error rendering component.", e);
			throw new RuntimeException(e);
		}
	}

	public void moveComponent(Long componentId, Long nextComponentId) {
		Component component = componentDao.loadComponent(componentId);
		ComponentList componentList = component.getList();
		List<Component> components = componentList.getComponents();
		components.remove(component);
		if (nextComponentId != null) {
			for (int i = 0; i < components.size(); i++) {
				if (components.get(i).getId().equals(nextComponentId)) {
					components.add(i, component);
					break;
				}
			}
		}
		else {
			components.add(component);
		}
	}

	public void deleteComponent(Long componentId) {
		Component component = componentDao.loadComponent(componentId);
		ComponentList componentList = component.getList();
		List<Component> components = componentList.getComponents();
		components.remove(component);
	}

	public void markAsDirty(Long containerId) {
		ContentContainer container = componentDao.loadContentContainer(containerId);
		container.setDirty(true);
		ComponentCacheUtils.invalidatePreviewVersion(cacheService, container);
		nofifyUsers();
	}
	
	public void publish(Long[] containerIds) {
		if (containerIds != null) {
			for (Long id : containerIds) {
				if (id != null) {
					ContentContainer container = componentDao.loadContentContainer(id);
					componentDao.publishContainer(container);
				}
			}
		}
		nofifyUsers();
	}

	public void discard(Long[] containerIds) {
		if (containerIds != null) {
			for (Long id : containerIds) {
				if (id != null) {
					ContentContainer container = componentDao.loadContentContainer(id);
					componentDao.discardContainer(container);
				}
			}
		}
		nofifyUsers();
	}

	/**
	 * Performs a logout.
	 */
	public void logout() {
		WebContext ctx = WebContextFactory.get();
		LoginManager.logout(ctx.getHttpServletRequest(), 
				ctx.getHttpServletResponse());
	}

	/* Utility methods */
	
	private HttpServletResponse getCapturingResponse(StringWriter sw) {
		WebContext ctx = WebContextFactory.get();
		return new CapturingResponseWrapper(ctx.getHttpServletResponse(), sw);
	}

	@SuppressWarnings("unchecked")
	private void nofifyUsers() {
		Locale locale = RequestContextUtils.getLocale(WebContextFactory.get().getHttpServletRequest());

		RiotUser user = AccessController.getCurrentUser();
		String userName = "";
		if (user.getName() != null) {
			userName = " (" + user.getName() + ")";
		}
		
		String message = messageSource.getMessage(
				"components.concurrentModification", 
				new Object[] {
					userName, "javascript:location.reload()" 
				},
				"The page has been modified by another user{0}. Please "
				+ "<a href=\"{1}\">reload</a> the page in order "
				+ "to see the changes.", locale);
				
		ScriptBuffer script = new ScriptBuffer();
		script.appendScript("riot.showNotification(").appendData(message).appendScript(");");
		String page = WebContextFactory.get().getCurrentPage();
		ScriptSession currentSession = WebContextFactory.get().getScriptSession();
		Collection<ScriptSession> sessions = ServerContextFactory.get(servletContext).getScriptSessionsByPage(page);
		for (ScriptSession session : sessions) {
			if (!session.equals(currentSession)) {
				log.info("Notifying session %s: %s", session.getId(), message);
				session.addScript(script);
			}
		}
	}

}
