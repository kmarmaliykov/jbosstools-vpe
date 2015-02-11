'use strict';

/* Controllers */

var xyz = {};
xyz.foo = function(a, b) {
	return "";
}

var qwerty = {};
qwerty.foo = function(c, d) {
	return "";
}

xyz.str = qwerty;

function PhoneListCtrl($scope) {
  $scope.phones = [
    {"name": "Nexus S",
     "snippet": "Fast just got faster with Nexus S.",
      "test": xyz},
    {"name": "Motorola XOOM™ with Wi-Fi",
     "snippet": "The Next, Next Generation tablet."},
    {"name": "MOTOROLA XOOM™",
     "snippet": "The Next, Next Generation tablet."}
  ];
}

function TabletCtrl($scope) {
	  $scope.tablets = [
	    {"name": "Nexus S",
	     "snippet": "Fast just got faster with Nexus S."},
	    {"name": "Motorola XOOM™ with Wi-Fi",
	     "snippet": "The Next, Next Generation tablet."},
	    {"name": "MOTOROLA XOOM™",
	     "snippet": "The Next, Next Generation tablet."}
	  ];
	}
