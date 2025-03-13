const $quoteName	= $('#quoteName');
const $quoteCompany	= $('#quoteCompany');
const $quoteEmail	= $('#quoteEmail');
const $cards		= $('.card');
const $cbType	= $('input[name=cbType]');
const $cbFrequency = $('input[name=cbFrequency]');
const $modal = $('#modal');
const $customBand = $('#customBand');
const $typeBUCs = $('#typeOutdoor').add($('#typeIndoor'));
const $typeConverter = $('#typeConverter');
const $typeLNB = $('#typeLNB');

const redYes = 'redYes';
const cbRedType = 'cbRedType'

// Redundancy
const $redNeed	= $('input[name=cbRedundancy]').on('input', function(e){

	switch(this.id){

		case redYes:

			const $input = $('<input>', {type: 'number', class: 'form-control', id: 'redQuantity', placeholder: 'Number of Systems', value: 1, disabled: true}).on('input', redundChange).on('input', checkCard);

			const $oneToOne = createFormCheck('oneToOne', cbRedType, '1:1', '', 'ONE_TO_ONE');
			$oneToOne.find('input').attr('data-factor', '2').on('input', redundChange);

			const $oneToTwo = createFormCheck('oneToTwo', cbRedType, '1:2', '', 'ONE_TO_TWO');
			$oneToTwo.find('input').attr('data-factor', '3').on('input', redundChange);

			const $redConverter = createFormCheck('redConverter',cbRedType, '1:1 1RU', '1:1 Frequency Converter in  single 1RU shelf', 'CONVERTER');
			$redConverter.find('input').attr('data-factor', '1').on('input', redundChange);		

			$(this).parents('.card-body')
			.append($('<div>', {class: 'row  row-cols-2'})
				.append($('<div>', {class: 'col'}).append($oneToOne))
				.append($('<div>', {class: 'col'}).append($oneToTwo))
				.append($('<div>', {class: 'col text-truncate'}).append($redConverter)))
			.append($('<div>', {class: 'row'})
				.append($('<div>', {class: 'form-floating'}).append($input).append($('<label>', {for: 'redQuantity', text: 'Number of Systems'}))));

			$('#redConverter').change(e=>{
				$typeBUCs.prop('disabled', e.currentTarget.checked).prop('checked', false);
				$typeConverter.prop('checked', true).trigger('input');
				const $card = $typeLNB.prop('disabled', e.currentTarget.checked).prop('checked', false).parents('.card');
				checkCard($card);
			});
			break;

		default:
			$(this).parents('.card-body').children().not(':first').remove();
	}
});

const $unitQuantity = $('#unitQuantity').on('input', e=>{
	if(e.currentTarget.value<1)
		e.currentTarget.value = 1;
});

function redundChange(e){

	const $parent = $(e.currentTarget).parents('.card-body');
	const $numbers = $parent.find('input[type=number]');

	let val = $numbers.val();
	if(val<1){
		$numbers.val(1);
		val = 1;
	}

	const factor = $parent.find('input:checked').filter((i,el)=>el.dataset.factor).data('factor');
	if(factor)
		$numbers.prop('disabled', false);

	$unitQuantity.val(factor*val);
}
$('.card input').on('input', checkCard);

function checkCard(e){

	const $card = e.currentTarget ? $(e.currentTarget).parents('.card') : e.length ? e : $(e);
	const next = getNextCard($card[0]);
	if(this.id==redYes){
		$card.removeClass('done').addClass('bg-transparent');
		if(next)
			disableCard(next);
		return;
	}

	if(filled($card)){
		$card.removeClass('bg-transparent').addClass('done');
		if($card.prop('id') === 'typeCard'){

			enableFreqBands($card);
			return;
		}
		if(next){
			enableCard(next);
			$card.find('input:checked').change();
		}
	}else{
		$card.removeClass('done').addClass('bg-transparent');

	if(next)
		disableCard(next);
	}
}
const $lBand = $('#freqL').parents('.text-truncate');
const $kaBand = $('#freqKa').parents('.text-truncate');
function enableFreqBands($typeCard){

	const $freqCard = $('#freqCard');
	const typeCardId = $typeCard.find('input:checked').val();
	const toSend = {};

	$freqCard.removeClass('disabled').addClass('bg-transparent');
	$freqCard.find('input').prop('disabled', true).prop('checked', false).each((i,el)=>{
		toSend[el.id] = el.value + '_' + typeCardId;
	});
//	$customBand.val('');
	const next = getNextCard($freqCard[0]);
	if(next)
		disableCard(next);

	postObject('/rest/quote/name-exists', toSend)
	.done(data=>{
		Object.keys(data).forEach(key=>$('#' + key).prop('disabled', !data[key]));

		if(data.freqL){
			$lBand.removeClass('visually-hidden');
			$kaBand.addClass('visually-hidden');
		}else{
			$lBand.addClass('visually-hidden');
			$kaBand.removeClass('visually-hidden');
		}

		$customBand.prop('disabled', false);
	});
}

function filled($card){

	// Card not used if all input disabled
	const $inputs = $card.find('input');
	const textFields = [];
	const checkBoxes = []

	if($inputs.length === 1 && $inputs.prop('type') === 'text'){
		$inputs.prop('disabled', true);
		return true;
	}

	$inputs.each((i,el)=>{

		if(el.disabled)
			return;

		if(['number', 'text', 'email'].includes(el.type))
			textFields.push(el);

		else if(['checkbox', 'radio'].includes(el.type))
			checkBoxes.push(el);
	});

	let txtFields = true;
	let txtHasText = false;
	if(textFields.length){
		txtFields = textFields.every(el=>{

					if(el.type == 'email')
						return el.value.match(/^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/);

					if(el.type == 'number')
						return el.value && parseInt(el.value) > 0;

					txtHasText = true;					

					return el.value;
				});
	}

	let chBoxes = true;
	if(!txtHasText && checkBoxes.length)
		chBoxes = checkBoxes.some(el=>el.checked);

	return chBoxes && txtFields;
}

function createFormCheck(id, name, text, title, value){
	let $checkBox = $('<input>',{id:id,name:name,type:'radio',class:'form-check-input visually-hidden', value: value}).on('input', checkCard);
	return $('<div>',{class:'form-check col'}).append($checkBox).append($('<span>',{class:'checkbox-custom'})).append($('<label>',{for:id,class:'form-check-label text-info',text:text,title:title}));
}
function enableCard(card){
	const $card = $(card);
	$card.removeClass('disabled').addClass('bg-transparent');
	if(!($card.hasClass('set-enable') && $typeConverter.prop('checked')))
		$card.find('input').prop('disabled', false);
	checkCard(card);
}
function disableCard(card){

	const $card = $(card);
	if($card.hasClass('disabled'))
		return;
	
	$card.addClass('disabled').removeClass('bg-transparent');
	$card.find('input').prop('disabled', true).prop('checked', false).filter((i,el)=>el.type==='text').val('');
	const next = getNextCard(card);
	if(next)
		disableCard(next);
}
function getNextCard(card){
	for(let i=0; i<$cards.length; i++){
		if($cards.get(i)==card){
			return $cards.get(++i);
		}
	}
	return null;
}

const $notLnb = $('.not-lnb');
const $typeCard = $('#typeCard');
const $productName = $('#productName');
const $productNames = $('#productNames');

$('#freqCard').find('input').filter((i,el)=>el.type === 'radio').on('input', e=>{

//	$customBand.val('');

	const $checkedtype = $typeCard.find('input:checked');
	const type = $checkedtype.val();

	fillProductNames(e.currentTarget.value, type);
})
function fillProductNames(band, type){

	$productNames.empty();
//	$customPower.val('');

	if(type==='CONVERTER' || type==='LNB'){
		return;
	}

	const toSend = {band: band, type: type};

	$.post('/rest/quote/product-name', toSend)
	.done(data=>{

		const pNames = {};
		const keys = Object.keys(pNames);
		const limit = keys.length>3 ? 2 : keys.length>2 ? 5 : 7;

		data.forEach(a=>{
			
		});

		keys.forEach(name=>{

			const bands = Object.keys(pNames[name]);

			bands.forEach((band,index)=>{

				const $sizeRow = $('<div>', {class: 'row row-cols-1'});
				const b = pNames[name][band];
				const length = b.length;
				b.every((size, i)=>{
					if(length>limit && i===limit){
						$sizeRow.append($('<div>', {class: 'col', text: '. . .'}))
						return false;
					}
					$sizeRow.append($('<div>', {class: 'col text-truncate', text: size}))
					return true;
				})

				$productNames.append(
					$('<div>', {class: 'col'})
					.append(
						$('<div>', {class: 'form-check'})
						.append($('<input>', {id: 'cb' + name + index, name: 'cbBucName', type: 'radio', class: 'form-check-input visually-hidden', value: band, disabled: true}).on('input', checkCard))
						.append($('<span>', {class: 'checkbox-custom'}))
						.append($('<label>', {for: 'cb' + name + index, class: 'form-check-label', text: name}).click(productNameClick))
					)
				).append(
					$('<div>', {class: 'col text-primary pt-2'})
					.append($sizeRow)
				)
				.append($('<div>', {class: 'col-12 border-bottom'}));
			});
		})
	});

}

const $freqCard = $('#freqCard');
$freqCard.find('label').click(e=>{

	if($(e.currentTarget).parent().children('input').prop('disabled'))
		return;

	const band = $('#' + e.currentTarget.htmlFor).val();
	const type = $typeCard.find('input:checked').val();

	$modal.empty().append($('<div>', {class: 'modal-dialog modal-sm'}));
	$modal.modal('show');
	$modal.load('/quote/modal', {name: band + '_' + type}, (body, error)=>{
		if(error=='error'){
			console.log(error);
			return;
		}
		$modal.find('button').click(freqCansel);
		const $inputs = $modal.find('input').change(modalBandChange);
		if($inputs.length===1)
			setTimeout(()=>$inputs.prop('checked', true).change(), 500);
	});
});
function freqCansel(){
	if(!$customBand.val())
		$freqCard.find('input:checked').prop('checked', false);
	checkCard($freqCard);
	$modal.find('button').off('click');
}
function modalBandChange(e){
	const vals = $modal.find('input:checked').map((i,el)=>el.value).get();
	$customBand.val(vals);

	const $checkedtype = $typeCard.find('input:checked');
	const type = $checkedtype.val();

	fillProductNames(e.currentTarget.value, type);

	setTimeout(()=>{ if(!$modal.prop('aria-hidden'))$modal.find('button').eq(0).click(); }, 1000);
}

const $cbSupplyAC = $('#cbSupplyAC');
const $customPower = $('#customPower').on('input', e=>{
	const power = e.currentTarget.value.replace(/\D/g, '');
	if(power>40){
		$supplyVoltages.find('input').filter((i,el)=>el.id!='cbSupplyAC').prop('disabled', true).prop('ckecked', false);
		$cbSupplyAC.prop('checked', true);
		checkCard($supplyVoltages[0]);
	}else{
		$supplyVoltages.find('input').filter((i,el)=>el.id!='cbSupplyAC').prop('disabled', false);
	}
});
const $supplyVoltages = $('#supplyVoltages');
function productNameClick(e){

	const $input = $(e.currentTarget).parent().find('input');
	if($input.prop('disabled'))
		return;

	const band = $input.val();
	const productName = e.currentTarget.innerText;

	$modal.empty().append($('<div>', {class: 'modal-dialog modal-sm'}));
	$modal.modal('show');
	$modal.load('/quote/modal/name', {subtype: productName + '_' + band}, (body, error)=>{
		if(error=='error'){
			console.log(error);
			return;
		}
//		$customPower.val('');
		$modal.find('input').change(modalPowerChange);
		$modal.find('button').click(()=>{
			if(!$customPower.val())
				$productName.find('input:checked').prop('checked', false);
			checkCard($supplyVoltages);
			$modal.find('button').off('click');
		});
	});

}

function modalPowerChange(e){
	$customPower.val(e.currentTarget.value);
	checkCard($productName);

	const type = $modal.find('.modal-title').text().split('_')[0];
	$.post('/rest/quote/supply', {productName: type, power: e.currentTarget.value})
	.done(enableSupply);

	setTimeout(()=>{ if(!$modal.prop('aria-hidden'))$modal.find('button').eq(0).click(); }, 1000);
}

const $amplifireBuc = $('#amplifireBuc');
$amplifireBuc.find('input').change(()=>$referenceClock.find('input').prop('disabled', false));
const $referenceClock = $('#referenceClock');
function enableSupply(data){

	const $inputs = $supplyVoltages.find('input');

	if(!data || !data.length){
		$inputs.prop('disabled', false);
		return;
	}

	$supplyVoltages.find('input').prop('disabled', true).prop('ckecked', false);
	data[0].content.forEach(supply=>{
		$supplyVoltages.find(`input[value=${supply}]`).prop('disabled', false);
	});

	const $enabled = $inputs.filter((i,inp)=>!inp.disabled);
	if($enabled.length===1)
		$enabled.prop('checked', true);
	if($enabled.length)
		$amplifireBuc.add($referenceClock).find('input').prop('disabled', false);
}

$customBand.change(e=>{
	const $checkedtype = $typeCard.find('input:checked');
	const type = $checkedtype.val();

	fillProductNames(null, type);
});





function NewCssCal(e, t, a, o, r, n, l) {
	if (
			dtToday = new Date,
			Cal = new Calendar(dtToday),
			void 0 !== o && (o ? Cal.ShowTime = !0 : Cal.ShowTime = !1,
				r && (r = parseInt(r, 10)),
				TimeMode = 12 === r || 24 === r ? r : 24,
				void 0 !== n && n ? Cal.ShowSeconds = !0 : Cal.ShowSeconds = !1),
			void 0 !== e && (Cal.Ctrl = e),
			void 0 !== t && "" !== t ? Cal.Format = t.toUpperCase() : Cal.Format = "MMDDYYYY", void 0 !== a && "" !== a && ("ARROW" === a.toUpperCase() ? Cal.Scroller = "ARROW" : Cal.Scroller = "DROPDOWN"), void 0 === l || "future" !== l && "past" !== l || (Cal.EnableDateMode = l), exDateTime = document.getElementById(e).value) { var s, i, d, p, c, u, h, C, m, g, y = exDateTime.indexOf(DateSeparator, 0), M = exDateTime.indexOf(DateSeparator, parseInt(y, 10) + 1), D = parseInt(Cal.Format.toUpperCase().lastIndexOf("M"), 10) - parseInt(Cal.Format.toUpperCase().indexOf("M"), 10) - 1, S = ""; "DDMMYYYY" === Cal.Format.toUpperCase() || "DDMMMYYYY" === Cal.Format.toUpperCase() ? "" === DateSeparator ? (d = exDateTime.substring(2, 4 + D), p = exDateTime.substring(0, 2), c = exDateTime.substring(4 + D, 8 + D)) : -1 !== exDateTime.indexOf("D*") ? (d = exDateTime.substring(8, 11), p = exDateTime.substring(0, 2), c = "20" + exDateTime.substring(11, 13)) : (d = exDateTime.substring(y + 1, M), p = exDateTime.substring(0, y), c = exDateTime.substring(M + 1, M + 5)) : "MMDDYYYY" === Cal.Format.toUpperCase() || "MMMDDYYYY" === Cal.Format.toUpperCase() ? "" === DateSeparator ? (d = exDateTime.substring(0, 2 + D), p = exDateTime.substring(2 + D, 4 + D), c = exDateTime.substring(4 + D, 8 + D)) : (d = exDateTime.substring(0, y), p = exDateTime.substring(y + 1, M), c = exDateTime.substring(M + 1, M + 5)) : "YYYYMMDD" === Cal.Format.toUpperCase() || "YYYYMMMDD" === Cal.Format.toUpperCase() ? "" === DateSeparator ? (d = exDateTime.substring(4, 6 + D), p = exDateTime.substring(6 + D, 8 + D), c = exDateTime.substring(0, 4)) : (d = exDateTime.substring(y + 1, M), p = exDateTime.substring(M + 1, M + 3), c = exDateTime.substring(0, y)) : "YYMMDD" !== Cal.Format.toUpperCase() && "YYMMMDD" !== Cal.Format.toUpperCase() || ("" === DateSeparator ? (d = exDateTime.substring(2, 4 + D), p = exDateTime.substring(4 + D, 6 + D), c = exDateTime.substring(0, 2)) : (d = exDateTime.substring(y + 1, M), p = exDateTime.substring(M + 1, M + 3), c = exDateTime.substring(0, y))), u = isNaN(d) ? Cal.GetMonthIndex(d) : parseInt(d, 10) - 1, parseInt(u, 10) >= 0 && parseInt(u, 10) < 12 && (Cal.Month = u), h = /^\d{4}$/, h.test(c) && parseInt(c, 10) >= StartYear && parseInt(c, 10) <= dtToday.getFullYear() + EndYear && (Cal.Year = parseInt(c, 10)), parseInt(p, 10) <= Cal.GetMonDays() && parseInt(p, 10) >= 1 && (Cal.Date = p), Cal.ShowTime === !0 && (12 === TimeMode && (S = exDateTime.substring(exDateTime.length - 2, exDateTime.length), Cal.AMorPM = S), s = exDateTime.indexOf(":", 0), i = exDateTime.indexOf(":", parseInt(s, 10) + 1), s > 0 ? (C = exDateTime.substring(s, s - 2), Cal.SetHour(C), m = exDateTime.substring(s + 1, s + 3), Cal.SetMinute(m), g = exDateTime.substring(i + 1, i + 3), Cal.SetSecond(g)) : -1 !== exDateTime.indexOf("D*") && (C = exDateTime.substring(2, 4), Cal.SetHour(C), m = exDateTime.substring(4, 6), Cal.SetMinute(m))) } selDate = new Date(Cal.Year, Cal.Month, Cal.Date), RenderCssCal(!0)
}
