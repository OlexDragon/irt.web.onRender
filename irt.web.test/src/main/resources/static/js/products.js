const agent = navigator.userAgent.toLowerCase();
if(agent.search('firefox')>0 || agent.search('fxios')>0 || agent.search('focus')>0)
	$('body').css({ 'background-color' : 'rgb(0,0,0,0)'});

const $productsContent = $('#products_content');
const $filterName = $('#filter-name');
const href = window.location.href.split('?')[0];

let timer = null;
let doLoadMore=true;
let filterValue = getFilterValues();
let selectedFilters = [];
let filters = getFiltersFromUrl();
let pushState = true;

let $filterInput = $('.filter input').on('input', ()=>{
	filter();
});
$('.filter').click(e=>{

//	console.log(e.target.localName);
	if(!(e.target.localName=='label' || e.target.localName=='span'))
		return;

	let input = e.target.localName=='label' ? e.target.children[0] : e.target.previousElementSibling;
	if(input.checked){
		e.preventDefault();
		input.checked = false;
		filter();
	}
});

window.addEventListener('popstate', function(){

	pushState = false;
	let elText;
	let $filters = $filterInput.filter((i, el)=>{

		if( el.type.toUpperCase()=='TEXT'){
			elText = el;
			return false;
		}
		return true;
	});

	$filters.prop('checked', false);
	elText.value = '';

	let h = window.location.href.split('?')[1];
	if(h){
		h.split('&').forEach((f)=>{
			let param = f.split('=');
			if(param[1]){
				if(param[0]=='search')
					elText.value = param[1];
				else{
					$filters.filter((i,el)=>{
						return el.value == param[1];
					})
					.prop('checked', true);
				}
			}
		});
	}
	filter();
});

const $matte = $('.matte');
const $matteLoad = $('.matte-1');
function filter() {

	disableFilters();

	clearTimeout(timer);
	$matte.removeClass('active');
	$matteLoad.removeClass('load');

	let newFilterValue = getFilterValues();

    UpdateCurrentFilters(newFilterValue.filter);

    if (isEqual(newFilterValue)) {
        return;
    }

    $matteLoad.addClass('load');

    timer = setTimeout(
		()=> {
			$matte.addClass('active').show();

			filters = []
			if (newFilterValue.search)
            	filters.push('search=' + encodeURIComponent(newFilterValue.search));

			if (newFilterValue.filter.length > 0) {
				newFilterValue.filter.forEach(function(idAndText) {
                	filters.push('filter=' + encodeURIComponent(idAndText.checkboxId));
            	});
			}

			filterValue = newFilterValue;

			//==================load============

			let f = filters.slice();
			f.push('page=0');
			let url = '/products/search?' +  f.join('&');
			$productsContent.load(
				url,
				function(data) {

					start = -1;
					$matte.fadeOut();

					$('.product').click(function(e){ productClick(e, $(this)); return false; });

					let load = $(data).length;
					if (load < pageSize){
            			doLoadMore=false;
						return;
					}

					doLoadMore=true;
					loadMore();
			});
        
			url = href + (filters.length ? '?' + filters.join('&') : '');

			if(pushState)
        		history.pushState(null, '', url);
			else
				pushState = true;

			$matteLoad.removeClass('load');
			window.scrollTo(0, 0);

    }, 2000);

}

function getFilterValues(){

    let fValue = {};
    fValue.filter = [];

    let search = $filterName.val();
    if (search)
        fValue.search = search;

    let selectedCheckboxes = $('.filter input:checked');
    if (selectedCheckboxes.length > 0) {
        selectedCheckboxes.each(
				function(){
					let idAndText = {};
					idAndText.checkboxtext = this.parentElement.innerText;
					idAndText.checkboxId = this.value;

					let fam = this.closest('.filter-family');
					idAndText.family = fam.firstElementChild.innerText;
					fValue.filter.push(idAndText);
				});
    }
    return fValue;
}

function getFiltersFromUrl(){

	let filters = [];
    let params = new URLSearchParams(window.location.search);
    params.forEach(
		function(value, key) { 
    		filters.push(`${key}=${value}`);    
		});
    return filters;
  }

let start = -1;
function loadMore() {

	if (!isVisible())
		return;
 
 	let s = $('.product').length;
 	if(s == start)	// so as not to repeat
 		return;

	start = s;

//	console.log(`${start} cards on the page`);

	if(start%pageSize){
//		console.log('The number of products does not mapch to the "pageSize".');
		return;
	}

	//=================get============
	let pageNumber = start/pageSize;
	let f = filters.slice();
	f.push(`page=${pageNumber}`);
	let url = '/products/search?' + f.join('&');
	$.get(url, function(data) {

		const $data = $(data);
		let $card = $data.find('.card');
//		console.log(`${$card.length} cards returned`);

		if (!$card.length){
			doLoadMore = false;
			return;
		}

		$card.click(function(e){productClick(e, $(this));return false;});

		$productsContent.append($data);
//		addImage($card);

		if ($card.length < pageSize){
			doLoadMore = false;
			return;
		}

		loadMore();
	});
}

function isVisible(){
    let bb = $('#checkLoad').offset().top
    let aa = $(window).scrollTop()
    let cc = $(window).height()
	return aa >= bb - cc;
}
$(window).on('resize scroll', ()=>{ 
    if(doLoadMore)
    	loadMore();
  });

function isEqual(newFilterValue) {

    if(newFilterValue.search != filterValue.search)
    	return false;
 
    let valueNewFilter1 = $.map(

		newFilterValue.filter,
 		function(a){
			return a.checkboxId;
		})
		.sort();

    let valueNewFilter2 = $.map(

		filterValue.filter,
		function(a){
			return a.checkboxId;
		}).sort();  
 
    return JSON.stringify(valueNewFilter1) === JSON.stringify(valueNewFilter2);
}


//gestionnaire du click sur la croix du current filtres
$("#filters-selected").on("click", ".filter-cross", function() {

	$('.tooltip ').remove();
    let currenVal=this.dataset.value;
    const filterValue = $(this).parent(".filter-item").find(".filter-value").text().trim();
    // supprimer le filtre sélectionné du tableau
    selectedFilters = selectedFilters.filter((value) => value !== filterValue);

    // Supprimer la sélection de la case correspondante
    let inputlVal = $(`.filter input[value=${currenVal}]`);
    inputlVal.prop("checked", false);
    filter();
    this.parentElement.remove();
});

$('.filter-item').each(function(){
        new bootstrap.Tooltip(this);
});

let $filtersList = $(".filters-list");
// fonction pour mettre à jour les current filtres  dans le balisage
$('.filter-cross').hover(function(){
            $(this).parents(".filter-item ").addClass('active');
        }, function(){
            $(this).parents(".filter-item ").removeClass('active');
        });
function UpdateCurrentFilters(filter) {

    $filtersList.empty();

    filter.forEach((idAndText) => {
        let $filterItem = $("<li>", {class: "filter-item ", title: idAndText.family,'data-bs-toggle': "tooltip",'data-bs-placement': "bottom"});
        new bootstrap.Tooltip($filterItem);
        let $filterValue = $("<span>", {class: "filter-value", tabindex: "0", text: idAndText.checkboxtext});
        let $filterCross = $("<span>", {class: "filter-cross", 'data-value':idAndText.checkboxId, text: " \u2715"})

        .hover(function(){
            $(this).parents(".filter-item ").addClass('active');
        }, function(){
            $(this).parents(".filter-item ").removeClass('active');
        });
 
        $filterItem.append( $filterValue, $filterCross);

        $filtersList.append($filterItem);
    });
}

$(".selected-filter-wrapper").hover(function() {
	let del = $(this).find(".delete");
	if($(".filter-item").length > 0 || del.is(":visible"))
            del.toggle();
});

$(".delete-all-button").click(function() {
	let filterCurrentItem=$('.filter-item');
	filterCurrentItem.remove();
	$filterInput.prop("checked", false);
	filter();
});

//pour desactiver modal pendent le click sur un boutton detail
	$('.product').click(function(e) {
		productClick(e, $(this));
		return false;
	});

//pour remplire les donner dans modal 
function productClick(e, $product){

	let productId = $product.data('productId');
	window.open('/pdf/product/' + productId, '_blank', 'fullscreen=yes');

//	if (e.target.localName!='a'){
//		let cardLink = $product.find('a').prop('href');
//		let cardId = cardLink.split("=")[1];
//		let imgId = images.url.filter(image => {
//			let w = (image.id == parseInt(cardId));
//			return w;
//		});
//		$('#product-carousel ').empty();
//		let imgSrc = "../static/images/jpeg/no_photo.jpg";
//		if(imgId.length == 1 &&imgId[0].val && imgId[0].val.length >1) {
//			let imgAllSrc=imgId[0].val;
//			console.log(imgAllSrc);
//			var carouselInner =  $('<div>').addClass('carousel-inner');
//			for (var i = 0; i < imgAllSrc.length; i++) {
//				imgSrc = imgAllSrc[i];
//				console.log(imgSrc);
//
//				var slide = $('<div>').addClass('carousel-item');
//				if (i === 0) 
//					slide.addClass('active');
//
//				var img = $('<img>').attr('src', imgSrc);
//				carouselInner.append( slide);
//				slide.append(img);
//				  
//				$('#product-carousel ').append(carouselInner);
//			} 
//		}else {
//			var divImg =  $('<div>');
//			var img = $('<img>').attr('src', imgId.length &&imgId[0].val && imgId[0].val.length == 1 ? imgId[0].val : imgSrc);
//			divImg.append(img);
//			$('#product-carousel ').append(divImg);
//		}
//	  
//		//let cardImg = $(product).find('img').attr('src');
//		let cardName = $(product).find('.card-title').text();
//		let cardPartNumber = $(product).find('.card-text').text();
//	    
//		let $modal = $('#ipb');
//		//$modal.find('img').attr('src',cardImg );
//		$modal.find('h2.card-title').text(cardName);
//		$modal.find('h3.card-title').text(cardPartNumber);
//		$modal.find('a').prop('href', cardLink);
//		$modal.modal('show');
//
//	}
}

//=========jQuery addImage .get()==========
    //function addImage($cards){
    // $cards.each((i, o)=>{
    //     let $card = $(o);
    //     let $button = $card.find('a');
    //     let id = $button.prop('href').split("=")[1];
    //     $.get(`/images/uri/${id}`, imgId=>{
    //         let imgSrc = "/images/jpeg/no_photo.jpg";
    //         if(imgId.length) {
    //             imgSrc=imgId[0]
    //           } 
    //         $card.find('img').prop('src', imgSrc);
    //     });
    // })

//==============filter-toggle====================
// clic btn filter

const $filterToggle = $('.filter-toggle');
const $filterSction = $('.filter-section');
$filterToggle.click(function() {
	$filterSction.toggleClass('hidden'); 
	$(this).toggleClass('active');
	var arrow = $(this).find('.arrow');
	if ($(this).hasClass('active')) 
		arrow.html('&#9650;'); 
 	else 
		arrow.html('&#9660;'); 
  
});

// clic sur input
$('.filter-section input').click(function() {
	$filterSction.addClass('hidden'); 
	$filterToggle.removeClass('active'); 
	$('.filter-toggle .arrow').html('&#9660;');
});

$('.carousel-control-next').click(function() {
	var $carouselInner = $(this).siblings('.carousel-inner');
    var itemWidth = $carouselInner.children('.carousel-item').outerWidth();
    $carouselInner.animate({ 'margin-left': -itemWidth }, 500, function() { $(this).css('margin-left', 0).children('.carousel-item').first().appendTo(this); });
});

$('.carousel-control-prev').click(function() {
	var $carouselInner = $(this).siblings('.carousel-inner');
    var itemWidth = $carouselInner.children('.carousel-item').outerWidth();
    $carouselInner.css('margin-left', -itemWidth).children('.carousel-item').last().prependTo($carouselInner);
    $carouselInner.animate({ 'margin-left': 0 }, 500);
});

loadMore();

const $cbFilter = $('.filter-section input');
disableFilters();
function disableFilters(){

	$cbFilter.parent().addClass('disabled');
	let toSend = $cbFilter.filter((i,el)=>el.checked).map((i,el)=>el.value).get();

	$.post('/rest/filter/accessible', { filterIDs: toSend })
	.done(data=>data.forEach(id=>$cbFilter.filter((i,el)=>parseInt(el.value)===id).parent().removeClass('disabled')));
}
      