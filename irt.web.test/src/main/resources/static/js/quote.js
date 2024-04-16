const $quoteName	= $('#quoteName');
const $quoteCompany	= $('#quoteCompany');
const $quoteEmail	= $('#quoteEmail');
const $cards		= $('.card');

const redYes = 'redYes';
const cbRedType = 'cbRedType'

// Redundancy
const $redNeed	= $('input[name=cbRedundancy]').on('input', function(e){

	switch(this.id){

		case redYes:

			let $input = $('<input>', {type: 'number', class: 'form-control', id: 'redQuantity', placeholder: 'Number of Systems', value: 1})
			.on('input', checkCard);

			$(this).parents('.card-body')
			.append($('<div>', {class: 'row  row-cols-2'})
				.append($('<div>', {class: 'col'}).append(createFormCheck('oneToOne', cbRedType, '1:1', '', 'ONE_TO_ONE')))
				.append($('<div>', {class: 'col'}).append(createFormCheck('oneToTwo', cbRedType, '1:2', '', 'ONE_TO_TWO')))
				.append($('<div>', {class: 'col text-truncate'}).append(createFormCheck('redConverter',cbRedType, '1:1 1RU', '1:1 Frequency Converter in  single 1RU shelf', 'CONVERTER'))))
			.append($('<div>', {class: 'row'})
				.append($('<div>', {class: 'form-floating'}).append($input).append($('<label>', {for: 'redQuantity', text: 'Number of Systems'}))));
			break;

		default:
			$(this).parents('.card-body').children().not(':first').remove();
	}
});

$('.card input').on('input', checkCard);

function checkCard(){

	let $card = $(this).parents('.card');
	if(this.id==redYes){
		$card.removeClass('done').addClass('bg-transparent');
		return;
	}

	if(filled($card)){
		$card.removeClass('bg-transparent').addClass('done');
	}else
		$card.removeClass('done').addClass('bg-transparent');
}
function filled($card){
	let $inputs = $card.find('input');
	let byName = {};
	let $filter = $inputs.filter((i,el)=>{

						if(el.type == 'email')
							return el.value.toLowerCase().match(/^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|.(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/);

						if(el.type == 'number')
							return parseInt(el.value) > 0;

						if(el.type == 'radio'){
							
							if(!byName[el.name])
								byName[el.name] = [];

							if(el.checked)
								byName[el.name].push(el);

							return true;
						}

						return el.value;
					});
	return $filter.length==$inputs.length;
}
function createFormCheck(id, name, text, title, value){
	let $checkBox = $('<input>',{id:id,name:name,type:'radio',class:'form-check-input visually-hidden', value: value}).on('input', checkCard);
	return $('<div>',{class:'form-check col'}).append($checkBox).append($('<span>',{class:'checkbox-custom'})).append($('<label>',{for:id,class:'form-check-label text-info',text:text,title:title}));
}
const $cbType	= $('input[name=cbType]');
const $cbFrequency = $('input[name=cbFrequency]');
