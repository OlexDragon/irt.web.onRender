
//New GUI v.4
$('#newGuiFile').change(e=>{

	let files = e.currentTarget.files;
	if(!files.length){
		alert('No files are selected for upload.');
		return;
	}

	if(!confirm('New GUI v.4 - Are you sure you want to upload this file?\nThe old file will be deleted.'))
		return;

	const form_data = new FormData();
	form_data.append('folder', 'gui4');
	form_data.append('file', files[0]);
	const url = `/hidden/files/upload/gui`;
	ajax(url, form_data);
});



// Old GUI v.3
$('#guiFile').change(e=>{

	let files = e.currentTarget.files;
	if(!files.length){
		alert('No files are selected for upload.');
		return;
	}

	if(!confirm('Old GUI v.3 - Are you sure you want to upload this file?\nThe old file will be deleted.'))
		return;

	const form_data = new FormData();
	form_data.append('folder', 'gui');
	form_data.append('file', files[0]);
	const url = `/hidden/files/upload/gui`;
	ajax(url, form_data);
});

const $modal = $('#modalAddDoc');
const $docDescription = $('#docDescription');
const $btmSaveFile = $('#btmSaveFile').click(()=>{
	const files = $docFile[0].files;
	if(!files || files.length==0){
		alert('No files are selected for upload.');
		$btmSaveFile.prop('disabled', true);
		return;
	}
	const form_data = new FormData();
	form_data.append('folder', 'docs');
	form_data.append('subfolder', $docDescription.val());
	form_data.append('file', files[0]);
	const url = `/hidden/files/upload/doc`;

	ajax(url, form_data);
});
$('#btnAddDoc').click(()=>$modal.modal('show'));
const $docFile = $('#docFile').change(e=>{

	if(!e.target.files || e.target.files.length==0){
		alert('No files are selected for upload.');
		$btmSaveFile.prop('disabled', true);
		return;
	}
	if(!$docDescription.val()){
		let name = e.target.files[0].name.split('.')[0];
		$docDescription.val(name)
	}

	$btmSaveFile.prop('disabled', false);
})
function ajax(url, form_data){

	$.ajax({

		url: url, 
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
}

const $modalRename = $('#modalRename');
const $renameField = $('#renameField');
const $btnRename = $('#btnRename').click(e=>{

	const val = $renameField.val();
	if(!val){
		alert("The text field cannot be empty.");
		return;
	}

	$.post(e.currentTarget.dataset.url, {renameTo: val})
	.done(response=>{
		alert(response);
		location.reload();
	})
	.fail(console.error);
});
$('.rename').click(e=>{

	let url = e.currentTarget.dataset.name;
	$renameField.val(url);

	if(e.currentTarget.dataset.parent)
		url = e.currentTarget.dataset.parent + '/' + url;

	$btnRename.attr('data-url', '/hidden/files/rename/docs/' + url);
	$modalRename.modal('show');
});
$('.delete').click(e=>{
	if(confirm('Are you sure you want to delete this file?')){
		$.ajax({
			url: e.currentTarget.dataset.url,
			type: 'DELETE',
			success: function(response) {
						alert(response);
						location.reload();
					},
    		error: console.error
		});
	}
})