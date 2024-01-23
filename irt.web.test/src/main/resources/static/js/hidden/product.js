let $imagesList = $('#imagesList');
let $pImage = $('#pImage');
let $pdfList = $('#pdfList');

$.post('/images/hidden/product/list', {productId: productId})
.done(data=>{
	data.forEach(path=>{

		let filename = path.replace(/^.*[\\/]/, '');
		let $a = $('<a>', {class: 'col', href: path, text: filename});
		let $btn = $('<button>', {class: 'col-auto btn btn-outline-secondary btn-sm', text: 'Delete'})

		$('<p>').append($('<div>', {class: 'row border-bottom'}).append($a).append($btn)).appendTo($imagesList);

		$a.click(e=>{
			e.preventDefault();
			$pImage.attr("src", '/images/product?path=' + path);
		});

		$btn.click(()=>{

			if(!confirm('Are you sure you want to delete this file?'))
				return;

			$.post(`/images/hidden/delete`, {path: path})
			.done(response=>{
				alert(response);
				location.reload();
			})
			.fail(console.error);
		})
	});
})
.fail(console.lerror);

$.post('/pdf/hidden/product/list', {productId: productId})
.done(data=>{
	data.forEach(path=>{

		let href = '/pdf/product?path=' + path;
		let filename = path.replace(/^.*[\\/]/, '');
		let $a = $('<a>', {class: 'col', href: href, text: filename, target: '_blank'});
		let $btn = $('<button>', {class: 'col-auto btn btn-outline-secondary btn-sm', text: 'Delete'})

		$('<p>').append($('<div>', {class: 'row border-bottom'}).append($a).append($btn)).appendTo($pdfList);

		$btn.click(()=>{
			
			if(!confirm('Are you sure you want to delete this file?'))
				return;

			$.post(`/pdf/hidden/delete`, {path: path})
			.done(response=>{
				alert(response);
				location.reload();
			})
			.fail(console.error);
		})
	});
})
.fail(console.lerror);

$('#pAddFile').change(e=>{

	let files = e.currentTarget.files;
	if(!files.length)
		return;

	if(!confirm('Are you sure you want to upload this file?'))
		return;

	var form_data = new FormData();
	form_data.append('productId', productId);
	form_data.append('file', files[0]);

	$.ajax({

		url: '/images/hidden/product/add', // point to server-side controller method
		dataType: 'text', // what to expect back from the server
		cache: false,
		contentType: false,
		processData: false,
		data: form_data,
		type: 'post'
	})
	.done(response=>{
//		alert(response);
		location.reload();
	})
	.fail(console.error);
});

$('#pAddPDF').change(e=>{

	let files = e.currentTarget.files;
	if(!files.length)
		return;

	if(!confirm('Are you sure you want to upload this file?'))
		return;

	var form_data = new FormData();
	form_data.append('productId', productId);
	form_data.append('file', files[0]);

	$.ajax({

		url: '/pdf/hidden/product/add', // point to server-side controller method
		dataType: 'text', // what to expect back from the server
		cache: false,
		contentType: false,
		processData: false,
		data: form_data,
		type: 'post'
	})
	.done(response=>{
//		alert(response);
		location.reload();
	})
	.fail(console.error);
});
