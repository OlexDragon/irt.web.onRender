
$('.product').click(e=>{

	let productId = e.currentTarget.dataset.productId;
	var win = window.open('/hidden/product/' + productId, '_blank');
	if (win) {
		win.focus();
	} else {
		alert('Please allow popups for this website');
	}
});