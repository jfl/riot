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
package org.riotfamily.website.css;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.riotfamily.cachius.spring.AbstractCacheableController;
import org.riotfamily.cachius.spring.Compressible;
import org.riotfamily.common.util.Generics;
import org.riotfamily.common.web.compressor.YUICssCompressor;
import org.riotfamily.common.web.filter.ResourceStamper;
import org.riotfamily.common.web.util.ServletUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Controller that serves dynamic CSS files.
 * <p>
 * It's sometimes desirable to use constants in CSS files or to perform
 * arithmetic operations. This controller allows you to do so, by using
 * FreeMarker to process the stylesheets.
 * </p>
 * <p>
 * You can place a <code>css.ini</code> file in the same directory where your
 * stylesheets are located. All properties defined in that file will be
 * available within the FreeMarker template.
 * </p>
 * <p>
 * Additionally the controller allows you to create styles that look different
 * in various contexts. A good example would be a website that uses multiple
 * color schemes. Therefore you can add named sections to your your ini file
 * and request <code>&lt;file>_&lt;section-name>.css</code> instead of
 * <code>&lt;file>.css</code>. This will cause the controller to expose the
 * properties of the requested section, possibly overriding any default
 * values with the same name.
 * </p>
 * <p>
 * You can access the properties from all sections at any time by using
 * <code>&lt;section_name>.&lt;property_name></code> in the FreeMarker template.
 * If a default value has been overridden by a section value you can still
 * access the original default, by using <code>global.&lt;property_name></code>.
 * </p>
 */
public class CssTemplateController extends AbstractCacheableController
		implements ServletContextAware, InitializingBean, 
		LastModified, Compressible {

	public static final String KEY_PROPERTY = "key";

	public static final String CONTEXT_PATH_PROPERTY = "contextPath";

	private static final String DEFAULT_INI_FILE_NAME = "css.ini";

	private static final String CSS_SUFFIX = ".css";

	private ServletContext servletContext;

	private Pattern keyPattern = Pattern.compile("(/[^/]+?)_(.*?)(\\.css)");

	private String contentType = "text/css";

	private Configuration freeMarkerConfig;

	private IniFile iniFile;

	private Pattern urlPattern = Pattern.compile(
			"(url\\s*\\(\\s*[\"']?)(.*?)(['\"]?\\s*\\))");

	private ResourceStamper stamper;

	private ColorTool colorTool = new ColorTool();
	
	private boolean addContextPathToUrls = false;
	
	private YUICssCompressor compressor = new YUICssCompressor();

	/**
	 * @param compressor the compressor to set
	 */
	public void setCompressor(YUICssCompressor compressor) {
		this.compressor = compressor;
	}
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void setFreeMarkerConfig(Configuration configuration) {
		this.freeMarkerConfig = configuration;
	}

	/**
	 * Sets the ResourceStamper that should be used to add timestamps to
	 * URLs specified within the template.
	 *
	 * @see ResourceStamper
	 * @since 6.4
	 */
	public void setStamper(ResourceStamper stamper) {
		this.stamper = stamper;
	}

	/**
	 * Sets whether the contextPath should be added to absolute URLs
	 * specified within the template. Defaults to <code>false</code>.
	 *
	 * @since 6.4
	 */
	public void setAddContextPathToUrls(boolean addContextPathToUrls) {
		this.addContextPathToUrls = addContextPathToUrls;
	}

	public void setIniFileLocation(Resource resource) throws IOException {
		iniFile = new IniFile(resource.getFile());
	}

	public void afterPropertiesSet() throws Exception {
		if (freeMarkerConfig == null) {
			freeMarkerConfig = new Configuration();
		}
		freeMarkerConfig.setDirectoryForTemplateLoading(
				new File(servletContext.getRealPath("/")));
	}

	public boolean gzipResponse(HttpServletRequest request) {
		return true;
	}
	
	public long getLastModified(HttpServletRequest request) {
		DynamicStylesheet stylesheet = lookup(request);
		return stylesheet.lastModified();
	}
	
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		DynamicStylesheet stylesheet = lookup(request);
		stylesheet.serve(request, response);
		return null;
	}

	protected DynamicStylesheet lookup(HttpServletRequest request) {
		String path = ServletUtils.getPathWithinApplication(request);
		String key = null;
		File file = new File(servletContext.getRealPath(path));
		if (!file.exists()) {
			Matcher matcher = keyPattern.matcher(path);
			if (matcher.find()) {
				key = matcher.group(2);
				path = matcher.replaceFirst("$1$3");
				file = new File(servletContext.getRealPath(path));
			}
		}
		return new DynamicStylesheet(file, path, key);
	}

	protected class DynamicStylesheet {

		private File file;

		private String path;

		private String key;

		public DynamicStylesheet(File file, String path, String key) {
			this.file = file;
			this.path = path;
			this.key = key;
		}

		public long lastModified() {
			long lastModified = -1;
			if (file != null) {
				lastModified = file.lastModified();
			}
			if (iniFile != null) {
				lastModified = Math.max(lastModified, iniFile.lastModified());
			}
			return lastModified;
		}

		public void serve(HttpServletRequest request,
				HttpServletResponse response)
				throws IOException, TemplateException {

			if (file == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if (!file.getName().endsWith(CSS_SUFFIX)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
			if (!file.canRead()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Can't read file " + file.getAbsolutePath());

				return;
			}

			if (iniFile == null) {
				File f = new File(file.getParentFile(),	DEFAULT_INI_FILE_NAME);
				if (f.canRead()) {
					iniFile = new IniFile(f);
				}
			}

			response.setContentType(contentType);

			Map<String, Object>	model = buildModel();
			model.put(KEY_PROPERTY, key);
			model.put(CONTEXT_PATH_PROPERTY, request.getContextPath());

			Template template = freeMarkerConfig.getTemplate(path);
			StringWriter sw = new StringWriter();
			template.process(model, sw);
			
			StringReader in = new StringReader(processUrls(sw.toString(), request));
			compressor.compress(in, response.getWriter());
		}

		private Map<String, Object> buildModel() {
			HashMap<String, Object> model = Generics.newHashMap();
			model.put("color", colorTool);
			if (iniFile != null) {
				Map<String, Map<String,Object>> sections = iniFile.getSections();
				model.putAll(sections);
				model.putAll(sections.get(IniFile.GLOBAL_SECTION));
				if (key != null) {
					Map<String, Object> current = sections.get(key);
					if (current != null) {
						model.putAll(current);
					}
				}
			}
			return model;
		}

		private String processUrls(String css, HttpServletRequest request) {
			String basePath = null;
			if (!ServletUtils.isDirectRequest(request)) {
				basePath = ServletUtils.getRequestUri(request);
			}
			if (stamper == null && basePath == null && !addContextPathToUrls) {
				return css;
			}
			StringBuffer sb = new StringBuffer();
			Matcher matcher = urlPattern.matcher(css);
			while (matcher.find()) {
				String url = matcher.group(2);
				if (stamper != null) {
					url = stamper.stamp(url);
				}

				if (url.startsWith("/")) {
					if (addContextPathToUrls) {
						url = request.getContextPath() + url;
					}	
				}
				else {
					if (basePath != null) {
						url = StringUtils.applyRelativePath(basePath, url);
					}
				}
				matcher.appendReplacement(sb, "$1" + url + "$3");
			}
			matcher.appendTail(sb);
			return sb.toString();
		}

	}
}
