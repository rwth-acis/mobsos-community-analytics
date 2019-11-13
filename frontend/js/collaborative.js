Y({
	db: {
		name: 'memory' // use memory database adapter.
		// name: 'indexeddb' // use indexeddb database adapter instead for offline apps
	},
	connector: {
		name: 'webrtc', // use webrtc connector
		// name: 'websockets-client'
		// name: 'xmpp'
		room: 'my-room' // clients connecting to the same room share data 
	},
	sourceDir: '/bower_components', // location of the y-* modules (browser only)
	share: {
		testMap: 'Map'
	}
	}).then(function (y) {
	// The Yjs instance `y` is available
	// y.share.* contains the shared types
	// Bind `y.share.textarea` to `<textarea/>`
	window.y = y
	y.share.map.set("request", "");
	y.share.map.set("apiHost", "");
	y.share.map.set("requestType", "");
	y.share.map.set("yjsTest", "");
	y.share.map.set("vizType", "");
})