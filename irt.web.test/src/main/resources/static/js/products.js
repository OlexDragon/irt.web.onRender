
let timer = null;
let doScroll=true;
let href = window.location.href.split('?')[0];
let filterValue = {};
filterValue.filter = [];
let selectedFilters = [];
let size = 8;
let array = getFiltersFromUrl();
let $productsContent = $('#products_content');


$('.filter input')
    .on('input', function() {
        filter();
    });

function filter() {
    let newFilterValue = {};
    newFilterValue.filter = [];
    let $matte = $('.matte');
    let $matteLoad = $('.matte-1');
    $matte.removeClass('active');
    $matteLoad.removeClass('load');
    // const nothingFound = document.querySelector('#nothing-found');
    let ourProduct = $('.our-product');
    ourProduct.removeClass('dnone');
    

    if (timer !== null) {
        clearTimeout(timer);
    }

    let search = $('#filter-name').val();
    if (search) {
        newFilterValue.search = search;
    }

    let selectedCheckboxes = $('.filter input:checked');
    if (selectedCheckboxes.length > 0) {
        selectedCheckboxes.each(function() {
            let idAndText = {};
            idAndText.checkboxtext = this.parentElement.innerText;
            idAndText.checkboxId = this.value;

            let fam = this.closest('.filter-family');
            idAndText.family = fam.firstElementChild.innerText;
            newFilterValue.filter.push(idAndText);

        });
    }

    UpdateCurrentFilters(newFilterValue.filter);

    if (isEqual(newFilterValue)) {
        return;
    }

    $matteLoad.addClass('load');

    timer = setTimeout(function() {

        $matte.addClass('active').show();

		let array = []
        if (newFilterValue.search) {
            array.push('search=' + encodeURIComponent(newFilterValue.search));
        }
        if (newFilterValue.filter.length > 0) {
            newFilterValue.filter.forEach(function(idAndText) {
                array.push('filter=' + encodeURIComponent(idAndText.checkboxId));
            });
        }

        filterValue = newFilterValue;

        //==================load============

		let f = array.slice();
		f.push(`size=${size}`);
		f.push('start=0');
		let url = '/products/search?' +  f.join('&');
        $productsContent.load(url, function(data) {

            addImage($('.our-product .card'));
            $matte.fadeOut();
            $('.product').click(function(e) {

                if (e.target.localName != 'a')
                    $('#ipb').modal('show');
            });
            let load = $(data).length;
            if (load < size){
            	doScroll=false;
                return;
            }

            doScroll=true;
            loadMore();
        });
        
    	url = href + (array.length ? '?' + array.join('&') : array.join('&'));
        history.pushState(null, '', url);
        $matteLoad.removeClass('load');
        window.scrollTo(0, 0);

    }, 2000);

}

function getFiltersFromUrl(){

	let array = [];
    let params = new URLSearchParams(window.location.search);
    params.forEach(function(value, key) { 
    	array.push(`${key}=${value}`);    
    });
    return array;
  }


function loadMore() {

    if (!isVisible())
	    return;
 
 	let start = $('.product').length;
	console.log(`${start} cards on the page`);
 
 	if(start<size)
 		return;

	//=================get============
	let f = array.slice();
	f.push(`size=${size}`);
	f.push(`start=${start}`);
	let url = '/products/search?' + f.join('&');
	$.get(url, function(data) {

		let $card = $(data).find('.card');
		console.log(`${$card.length} cards returned`);

		if (!$card.length){
			doScroll = false;
			return;
		}

		$productsContent.append(data);
		addImage($card);

		if ($card.length < size){
			doScroll = false;
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
$(window).on('resize scroll', function() { 
    if(doScroll)
    	loadMore();
  });

function isEqual(newFilterValue) {
    if(newFilterValue.search != filterValue.search)
    return false;
 
    let valueNewFilter1= $.map(newFilterValue.filter,function(a,i){
        return a.checkboxId;
      }).sort();
    let valueNewFilter2= $.map(filterValue.filter,function(a,i){
        return a.checkboxId;
    }).sort();  
    return JSON.stringify(valueNewFilter1) === JSON.stringify(valueNewFilter2);

}


//gestionnaire du clic sur la croix du current filtres
$("#filters-selected").on("click", ".filter-cross", function() {

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


// fonction pour mettre à jour les current filtres  dans le balisage
function UpdateCurrentFilters(filter) {

    $(".filters-list").empty();
    filter.forEach((idAndText) => {
        let $filterItem = $("<li>", {
            'class': "filter-item "
        });
        const $filterValue = $("<span>", {
            'class': "filter-value",
            'title': idAndText.family,
            'data-toggle': "tooltip",
            'data-placement': "top",
            'tabindex': "0"
        }).text(idAndText.checkboxtext);
        const $filterCross = $("<span>", {
            class: "filter-cross",
            'data-value':idAndText.checkboxId 
        }).text(" \u2715").hover(function() {
            $(this).toggleClass('active')
            var $filterValue = $(this).parent(".filter-item").find(".filter-value");
            $filterValue.css('color', 'red');
        }, function() {
            var $filterValue = $(this).parent(".filter-item").find(".filter-value");
            $filterValue.css('color', '#0f4178');
        });
        $filterItem.append( $filterValue, $filterCross);

        $(".filters-list").append($filterItem);
        $filterValue.tooltip();
    });

}

$(".selected-filter-wrapper").hover(function() {
    let del = $(this).find(".delete");
      if($(".filter-item").length > 0 || del.is(":visible")){
            del.toggle();
        }
        });
      
      $(".delete-all-button").click(function() {
        console.log(this);
        let filterCurrent= this.closest(".filters-current");
        let filterCurrentItem=$('.filter-item');
        filterCurrentItem.remove();
        $(".filter input").prop("checked", false);
      });

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

//=========JS addImage fetch()==========
function addImage(cards){
 for (let i = 0; i < cards.length; i++) {
    let card=cards[i];
    let button = card.querySelector('a');
    let id = button.href.split("=")[1];
    fetch(`/images/uri/${id}`)
     .then(response => response.json())
     .then((imgId) => {
        let imgSrc = "/images/jpeg/no_photo.jpg";
        if(imgId.length) {
            imgSrc=imgId[0]
        } 
        card.querySelector('img').src= imgSrc;
     });
 }
}

addImage($('.our-product .card'));
loadMore();


      