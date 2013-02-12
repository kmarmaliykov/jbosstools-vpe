XMLHttpRequest.prototype.oldOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
	var a = document.createElement('a');
	a.href = url;
	var absoluteUrl = a.href;
	if (absoluteUrl.indexOf(document.location.origin) != 0) {
		url = document.location.origin + '/proxy/' + absoluteUrl;
	}
	this.oldOpen(method, url, async, user, password);
}
