const clientIP = Cookies.get('clientIP');
if(!clientIP)
	$.get('https://api.ipify.org', data=>Cookies.set('clientIP', data));

const $modal = $('#modalFaq');
const $answer = $('#modalFaq .pre-line');
const $modalTitle = $('#modalFaq .modal-title');

$('.faq h4').click(e=>{

	$.post('/rest/suport/answer', {faqID: e.currentTarget.dataset.id})
	.done(answer=>{

		$modalTitle.text(e.currentTarget.textContent);
		$answer.text(answer);
		$modal.modal('show');
	})
	.fail(function(error) {
		window.console.error(error);
  	});
})

const $nav = $('.navbar');
const $guiDownload = $('.gui-download');
const $faq = $('.faq');
const $docs = $('.docs');
const $modalRMA = $('#modalRMA');

$('.justify-content-md-center div').click(e=>{

	let height = $nav.height();
	let offset;
	switch(e.currentTarget.dataset.name){

		case 'download':
			offset = $guiDownload.offset();
			window.scrollTo({ top: offset.top - height , behavior: 'smooth'});
			break;

		case 'faq':
			offset = $faq.offset();
			window.scrollTo({ top: offset.top - height , behavior: 'smooth'});
			break;

		case 'rma':
			let split = window.location.href.split('#');

			if(split.length==1 || split[1] !== '#rma')
				history.pushState(null, '', split[0] + '#rma');

			$modalRMA.modal('show');
			break;

		case "docs":
			offset = $docs.offset();
			window.scrollTo({ top: offset.top - height , behavior: 'smooth'});
			break;
	}
});

if(window.location.hash == '#rma')
	setTimeout(()=>$modalRMA.modal('show'), 500);

const $rmaName = $('#rmaName');
const $lblRmaName = $('label[for=rmaName]');
const $rmaEmail = $('#rmaEmail');
const $lblRmaEmail = $('label[for=rmaEmail]');
const $rmaSN = $('#rmaSN');
const $lblRmaSN = $('label[for=rmaSN]');
const $rmaCause = $('#rmaCause');
const $lblRmaCause = $('label[for=rmaCause]');
const $btnCreateRMA = $('#btnCreateRMA');

let btnText;
$btnCreateRMA.parent().hover(()=>{

	if(btnText)
		return;

	const nameLength = $rmaName.val().length;
	const isName = nameLength>0 && nameLength<=255;
	if(!isName)
		$lblRmaName.addClass('blink');

	const isSN = $rmaSN.val().replace(/[^0-9]/g,"").length===7;
	if(!isSN)
		$lblRmaSN.addClass('blink');

	let causeLength = $rmaCause.val().length;
	let isCause = causeLength>0;
	if(!isCause)
		$lblRmaCause.addClass('blink');
	else{
		isCause = causeLength<=1000;
		if(!isCause)
			alert(`The maximum length for a fault description is 1000 characters, but you entered ${causeLength} characters.`);
	}
	const emailValue = $rmaEmail.val();
	const match = emailValue.length<=255 && emailValue.toLowerCase().match(/^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/)
	if(!match)
		$lblRmaEmail.addClass('blink');

	if(isName && match && isSN && isCause){
		$btnCreateRMA.prop('disabled', false);
		return;
	}
	$btnCreateRMA.prop('disabled', true);

	btnText = $btnCreateRMA.text();
	$btnCreateRMA.text('All fields must be filled.');

	setTimeout(()=>{

		if(btnText){
			$btnCreateRMA.text(btnText);
			btnText = '';
		}

		$lblRmaName.removeClass('blink');
		$lblRmaEmail.removeClass('blink');
		$lblRmaSN.removeClass('blink');
		$lblRmaCause.removeClass('blink');
	}, 3000);
});

const $modalAnswer = $('#modalAnswer');
const $result = $('#modalAnswer .pre-line');
const $title = $('#modalAnswer .modal-title');

$btnCreateRMA.click(()=>{

	let toSend	 = {}
	toSend.name	 = $rmaName.val();
	toSend.email = $rmaEmail.val();
	toSend.sn	 = $rmaSN.val();
	toSend.cause = $rmaCause.val();
	$modalRMA.modal('hide');

	$title.text('Please wait');
	$result.text("This shouldn't take long.");
	$modalAnswer.modal('show');

	$.get('/rest/serial-number/exists', {sn: toSend.sn})
	.done(data=>{

		if(data == true)
			createRMA(toSend);

		else{
			$.get('/rest/serial-number/ends-with', {sn: toSend.sn.replace(/\D/g,'')})
			.done(d=>{

				if(d && d.serialNumber){
					if(confirm("We did not manufacture the device with serial number " + toSend.sn + '.\nMaybe you meant ' + d.serialNumber +'.\n' + d.partNumber.description)){
						toSend.sn = d.serialNumber;
						$rmaSN.val(d.serialNumber);
						createRMA(toSend);

					}
				}else{
					$title.text('PMA number generation has stopped.');
					$result.text("We did not manufacture the device with serial number " + toSend.sn);
				}
			})
			.fail(console.lerror);
		}
	})
	.fail(console.lerror);
});
$('#btnCopy').click(()=>{
	if(navigator.clipboard){

		let text = $result.text();
		navigator.clipboard.writeText(text);

	}else{
		if (document.body.createTextRange) {
 			var range = document.body.createTextRange();
			range.moveToElementText($result[0]);
			range.select();
		} else if (window.getSelection) {
			var selection = window.getSelection();
			var range = document.createRange();
			range.selectNodeContents($result[0]);
			selection.removeAllRanges();
			selection.addRange(range);
		} else {
			alert("Could not select text in node: Unsupported browser.");
			return;
		}
		document.execCommand('copy');
	}
});
function createRMA(toSend){

		$.ajax({
    	type: "POST",
    	url: "rest/rma/create",
    	data: JSON.stringify(toSend),
    	contentType: "application/json; charset=utf-8",
    	dataType: "json",
    	success: function(data){
			switch(data.cssClass){

				case "text-bg-success":
					let split = data.message.split('<>');
					let rma = JSON.parse(split[0]);
					$title.text(rma.rmaNumber);

					let text = rma.rmaNumber
					if(split.length>1)
						text += '\n\nShipping address:\n' + split[1]
					$result.text(text);
					break;

				default:
					$title.text('Something went wrong.');
					$result.text(data.message);
			}
		},
    	error: function(errMsg) {
      	  alert(errMsg);
    	}
	});

}