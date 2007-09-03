var riot = {
	contextPath: '${contextPath}',
	path: '${contextPath}${riotServletPrefix}',
	language: '${language}' || 'en',
	instantPublish: window.riotInstantPublish || false
};

riot.componentEditorResource = riot.instantPublish
		? {src: 'dwr/interface/InstantComponentEditor.js', test: 'InstantComponentEditor', onload: function() {window.ComponentEditor = InstantComponentEditor}}
		: {src: 'dwr/interface/ComponentEditor.js', test: 'ComponentEditor'}

Resources.loadStyleSheet('style/toolbar.css');
Resources.loadStyleSheet('style/edit-mode.css');

Resources.loadScriptSequence([
	{src: 'prototype/prototype.js', test: 'Prototype'},
	{src: 'riot-js/inheritance.js', test: 'Class.extend'},
	{src: 'riot-js/util.js', test: 'RElement'},
	{src: 'scriptaculous/effects.js', test: 'Effect'},
	{src: 'toolbar.js', test: 'riot.toolbar'},
	{src: 'dwr/engine.js', test: 'dwr.engine'},
	{src: 'dwr/util.js', test: 'dwr.util'},
	riot.componentEditorResource,
	{src: 'dwr/interface/EntityEditor.js', test: 'EntityEditor'},
	{src: 'dwr/interface/UploadManager.js', test: 'UploadManager'},
	{src: 'swfupload/SWFUpload.js', test: 'SWFUpload'},
	{src: 'scriptaculous/dragdrop.js', test: 'Droppables'},
	{src: 'riot-js/effects.js'},
	{src: 'riot-js/viewport.js'},
	{src: 'riot-js/window-callback.js'},
	{src: 'inplace.js', test: 'riot.InplaceEditor'},
	{src: 'component.js'}
]);


