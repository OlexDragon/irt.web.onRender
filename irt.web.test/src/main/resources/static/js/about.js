$('#navAbout').addClass('active');

let $first = $('#first');
let $last = $('#last');
let $phone = $('#phone');
let $email = $('#email');
let $company = $('#company');
let $industry= $('#industry');
let $toSend = $('#toSend');
let $toastContaner = $('#toastContaner');
let sent = false;
let $send = $('#send')
.click(e=>{

	if(sent || !validate() || 	$send.prop('disabled'))
		return;

	let toSend = {};

	toSend.firstName = $first.prop('readonly', true).val().trim();
	toSend.lastName = $last.prop('readonly', true).val().trim();
	toSend.phone = $phone.prop('readonly', true).val().trim();
	toSend.email = $email.prop('readonly', true).val().trim();
	toSend.company = $company.prop('readonly', true).val().trim();
	toSend.industry = $industry.prop('readonly', true).val().trim();
	toSend.message = $toSend.prop('readonly', true).val().trim();
	$send.prop('disabled', true);
	sent = true;

	var json = JSON.stringify(toSend);

	$.ajax({
		url: '/rest/email/send',
		type: 'POST',
		contentType: "application/json",
		data: json,
        dataType: 'json'
    })
	.done(function(data){
		showToast('Response', data.message, data.cssClass);
	})
	.fail(function(error) {

		if(error.statusText!='abort'){
			if(error.responseText)
				showToast('Error', error.responseText, 'text-bg-danger');
			else
				showToast('Error', "Server error. Status = " + error.status, 'text-bg-danger');
		}
	});
});

let onFocus = false;
$first.parents('.container').find('input, textarea')
.focus(e=>{
	onFocus = true;
})
.focusout(e=>{
	onFocus = false;
})
.on('input', e=>{
	e.currentTarget.classList.remove("bg-danger-subtle");
	$send.prop('disabled', true);

	let max = 1000;
	if(e.currentTarget.value.length < max)
		return;

		$toSend.addClass('bg-danger-subtle');
		showToast('Required fields', `The maximum number of text characters is ${max}. You have entered ${e.currentTarget.value.length} characters`, 'text-bg-danger');
});

$send.parent().mouseenter(e=>{

	if(!onFocus)
		return;

	validate();
});

function validate(){
	
	if(!($send.prop('disabled') || sent)){
		return true;
	}

	$send.prop('disabled', true);

	if(sent){
		return false;
	}

	if($toastContaner.children().length)
		return false;

	//First Name
	let disable = false;
	let first = $first.val().trim();
	if(!first){
		$first.addClass('bg-danger-subtle');
		showToast('Required fields', 'Name field must be filled in', 'text-bg-danger');
		disable = true;
	}else if(first.length>50){
		$first.addClass('bg-danger-subtle');
		showToast('Length Error', 'The First Name is too long.', 'text-bg-danger');
		disable = true;
	}

	// Larst Name
	let last = $last.val().trim();
	if(last.length>50){
		$last.addClass('bg-danger-subtle');
		showToast('Length Error', 'The Last Name is too long.', 'text-bg-danger');
		disable = true;
	}

	// Phone Numbere
	let phone = $phone.val().trim();
	if(phone.length>50){
		$phone.addClass('bg-danger-subtle');
		showToast('Length Error', 'The Phone Number is too long.', 'text-bg-danger');
		disable = true;
	}

	// EMail
	let email = $email.val().trim();
	if(!email){
		$email.addClass('bg-danger-subtle');
		showToast('Required fields', 'Email field must be filled in', 'text-bg-danger');
		disable = true;
	}
	else if(email.length>320){
		$email.addClass('bg-danger-subtle');
		showToast('Length error', 'The email address is too long. Please enter a valid email address.', 'text-bg-danger');
		disable = true;	
	}
	else if(!email.toLowerCase().match(/^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)){
		$email.addClass('bg-danger-subtle');
		showToast('Required fields', 'Please enter a valid email address.', 'text-bg-danger');
		disable = true;	
	}

	// Company
	let company = $company.val().trim();
	if(company.length>50){
		$company.addClass('bg-danger-subtle');
		showToast('Length Error', 'The Company name is too long.', 'text-bg-danger');
		disable = true;
	}

	// Industry
	let industry = $industry.val().trim();
	if(industry.length>50){
		$industry.addClass('bg-danger-subtle');
		showToast('Length Error', 'The Industry is too long.', 'text-bg-danger');
		disable = true;
	}

	// Message
	let toSend = $toSend.val().trim();
	if(!toSend){
		$toSend.addClass('bg-danger-subtle');
		showToast('Required fields', 'Text field must be filled in', 'text-bg-danger');
		disable = true;
	}else if($toSend.length>1000){
		$toSend.addClass('bg-danger-subtle');
		showToast('Length Error', 'The Message is too long.', 'text-bg-danger');
		disable = true;
	}

	if(disable)
		return false;

	$send.prop('disabled', false)
	return true;
}
function showToast(title, message, headerClass){

	let $toast = $('<div>', {class: 'toast', role: 'alert', 'aria-live': 'assertive', 'aria-atomic': true})
		.append(
			$('<div>', {class: 'toast-header'})
			.append(
				$('<strong>', {class: 'me-auto', text: title})
			)
			.append(
				$('<button>', {class: 'btn-close', type: 'button', 'data-bs-dismiss': 'toast', 'aria-label': 'Close'})
			)
		)
		.append(
			$('<div>', {class: 'toast-body', text: message})
		)
	.appendTo($toastContaner)
	.on('hide.bs.toast', function(){this.remove();});

	if(headerClass)
		$toast.find('.toast-header').addClass(headerClass);

	new bootstrap.Toast($toast).show();
}
