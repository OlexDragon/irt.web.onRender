$('.has-checkbox').on('hide.bs.dropdown', function(e){

	if(!e.clickEvent)
		return;
	
	let localName = e.clickEvent.srcElement.localName;
	let outDropdownMenu = $(e.clickEvent.target).closest('.dropdown-menu').length > 0;

	if (localName != 'a' && outDropdownMenu)
		e.preventDefault();
});
	
//==============scrollToSection  ===== behavior со значением 'smooth' - плавная прокрутка===========
window.addEventListener('DOMContentLoaded', function() {
	var sectionId = window.location.hash;
  
	if (sectionId) {
		var section = document.querySelector(sectionId);
  
		if (section) 
			section.scrollIntoView({ behavior: 'smooth' });
	}
});
let $navLabel = $('nav label').click(e=>{
	let span = e.currentTarget.previousElementSibling;
	let input = span.previousElementSibling;
	if(input.checked){
		e.preventDefault();
		input.checked = false;
		filterSelected();
	}
});
let $navInput = $('nav input').change(filterSelected);
let $btnGetSelected = $('#btnGetSelected');

function filterSelected(){

	$navLabel.addClass('disabled').before().addClass('disabled');

	let toSend = $navInput.filter((i,el)=>el.checked).map((i,el)=>el.value).get();
	$.post('/rest/filter/accessible', { filterIDs: toSend })
	.done(data=>{
//		console.log(data);
		$navInput.each((i,el)=>{

			let exists = data.filter(id=>id===parseInt(el.value)).length>0;
			if(exists)
				$(el).next().removeClass('disabled').next().removeClass('disabled').parents('.sous-link').removeClass('visually-hidden');
			else
				$(el).parents('.sous-link').addClass('visually-hidden');
		});

		let $checked = $navInput.filter((i,el)=>el.checked);
		$btnGetSelected.addClass('nav-blink');
		let f = $checked.map((i,el)=>'filter=' + el.value).get().join('&');
		$btnGetSelected.attr('href', '/products?' + f);
		setTimeout(()=>$btnGetSelected.removeClass('nav-blink'), 3000);
	});
}
function postObject(url, object){
	const toSend = JSON.stringify(object);
	return $.ajax({
	        url: url,
	        type: 'POST',
	        contentType: 'application/json',
	        data: toSend,
	        dataType: 'json'
	    });
}