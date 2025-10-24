const $pkgName = $('#pkgName').change(({currentTarget:{value}})=>{

	if($pkgUpgrade.prop('files').length && value)
		$btnUploadPkg.prop('disabled', false);
	else
	    $btnUploadPkg.prop('disabled', true);
});
const $pkgUpgrade = $('#pkgUpgrade').change(({currentTarget:{files}})=>{
	if($pkgUpgrade.prop('files').length && $pkgName.val())
		$btnUploadPkg.prop('disabled', false);
	else
	    $btnUploadPkg.prop('disabled', true);
});
const $btnUploadPkg = $('#btnUploadPkg').click(()=>{

	const files = $pkgUpgrade.prop('files');
	if(!files.length){
		alert('No package file selected');
		return;
	}

	const fd = new FormData();
	for(const f of files)
		fd.append('files[]', f);
	fd.append("pkgName", $pkgName.val());

	$.ajax({
		url: '/pkg/upload',
		data: fd,
		cache: false,
		contentType: false,
		processData: false,
		method: 'POST',
		type: 'POST', // For jQuery < 1.9
		success: function(data){
			if (data !== 'Done')
				alert(data);
	 		location.reload();
		},
	    error: function(error) {
            alert('Error uploading package:\n' + error.responseText);
	    }
	});
});

$('table button.btn-delete-pkg').click(({currentTarget:el})=>{
	if (!confirm(`Are you sure you want to delete package\n"${el.value}"?`))
		return;

	$.post('/pkg/delete', { path: el.value })
	.done((data)=>{
		switch(data) {

		case 'Done':
			$(el).parents('tr').get(0).remove();
			break;

		case 'Folder':
			$(el).parents('tr').get(1).remove();
			break;

		default:
			console.warn(data);
		}
	})
	.fail((error)=>{
		alert('Error deleting package:\n' + error.responseText);
	});
});