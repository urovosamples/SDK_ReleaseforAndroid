$(function() {
	$('#gwj ul li').click(function() {
		var _this = $(this);
		_this.addClass('cur').siblings('li').removeClass('cur');
	});
});

$(function() {
	$('#menu ul li').click(function() {
		var _this = $(this);
		_this.addClass('cur').siblings('li').removeClass('cur');
	});
	
//	$('#ytable11 ul li').click(function() {
//		var _this = $(this);
//		_this.addClass('cur').siblings('li').removeClass('cur');
//	});
});
