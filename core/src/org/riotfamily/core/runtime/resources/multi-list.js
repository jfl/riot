var RiotMultiList = Class.create({

	initialize: function(controlList) {
		this.controlList = controlList;
	},

	wire: function(target) {
		this.mainListFrame = $(target);
		this.mainListFrame.update = this.updateControlList.bind(this);
		this.mainListFrame.resize = this.resizeMainListFrame.bind(this);
		this.controlList.table.observe('list:selectionChanged',
			this.handleControlSelectionChanged.bind(this));
	},

	getMainList: function() {
		return this.mainListFrame.contentWindow.list;
	},

	resizeMainListFrame: function(dimensions) {
		this.mainListFrame.setStyle({ height: dimensions.height + 'px' });
	},

	updateControlList: function(parentId, state) {
		var row = ListRow.get(parentId);
		if (row) {
			row.expand.call(row);
		}
	},

	handleControlSelectionChanged: function(ev) {
		if (ev.memo.length > 0) {
			var mainList = this.getMainList();
			ListService.gotoItemScreenUrl(this.controlList.key, ev.memo, true,
				mainList.processCommandResult.bind(mainList));
		}
	}

});
