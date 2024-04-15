$('#guiFile').change(e=>{

	let files = e.currentTarget.files;
	if(!files.length)
		return;

	if(!confirm('Are you sure you want to upload this file?\nThe old file will be deleted.'))
		return;

	var form_data = new FormData();
	form_data.append('folder', 'gui');
	form_data.append('file', files[0]);

	$.ajax({

		url: `/hidden/files/upload`, // point to server-side controller method
		dataType: 'text', // what to expect back from the server
		cache: false,
		contentType: false,
		processData: false,
		data: form_data,
		type: 'post'
	})
	.done(response=>{
		alert(response);
		location.reload();
	})
	.fail(console.error);
});
