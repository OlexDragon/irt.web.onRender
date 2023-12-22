
  var images={}
  images.url = [];
  images.url.push({id:20, val : ["../static/images/jpeg/FemtoBUC65W6.jpg","../static/images/jpeg/L_to_IF_IFC-1.jpg","../static/images/jpeg/img6.jpg","../static/images/jpeg/satellit1.jpg"]});
  images.url.push({id:22, val : ["../static/images/jpeg/ipb_2U.jpg"]});
  images.url.push({id:23, val : ["../static/images/jpeg/LNB_C_band_1+1_Red_1.jpg","../static/images/jpeg/img6.jpg","../static/images/jpeg/L_to_IF_IFC-1.jpg","../static/images/jpeg/satellit1.jpg"]});
  images.url.push({id:24, val : ["../static/images/jpeg/L_to_IF_IFC-1.jpg","../static/images/jpeg/LNB_C_band_1+1_Red_1.jpg","../static/images/jpeg/LNB_C_band_1+1_Red_1.jpg","../static/images/jpeg/satellit1.jpg"]});
  images.url.push({id:1, val : ["../static/images/jpeg/ipb_2U.jpg","../static/images/jpeg/img6.jpg","../static/images/jpeg/LNB_C_band_1+1_Red_1.jpg","../static/images/jpeg/satellit1.jpg"]});
  images.url.push({id:13, val : []});
  images.url.push({id:14});
  images.url.push({val : ["../static/images/jpeg/ipb_2U.jpg","../static/images/jpeg/img6.jpg","../static/images/jpeg/img6.jpg","../static/images/jpeg/satellit1.jpg"]});

// Tracking "hide.bs.dropdown" event on the elements of the "has-checkbox" page
// $('.has-checkbox').on('hide.bs.dropdown', function(e){
// 	if(!e.clickEvent)
// 		return;
// 	let localName = e.clickEvent.srcElement.localName;
// 	if(localName != 'button' )
// 		e.preventDefault();

// })

$('.has-checkbox ').on('hide.bs.dropdown', function(e){
	if(!e.clickEvent)
		return;
	
	let localName = e.clickEvent.srcElement.localName;
	let outDropdownMenu = $(e.clickEvent.target).closest('.dropdown-menu').length > 0;
	console.log(localName);

  if (localName != 'a' && outDropdownMenu) {
    e.preventDefault();
  }

})

//pour desactiver modal pendent le clic sur un boutton detail


	$('.product').click(function(e) {
		productClick(e, this);
	});

//pour remplire les donner dans modal 
function productClick(e, product){
	

	if (e.target.localName!='a'){
		let cardLink = $(product).find('a').prop('href');
		let cardId = $(product).find('a').prop('href').split("=")[1];
		let imgId = images.url.filter(image => {
			let w = (image.id == parseInt(cardId));
			return w});
			$('#product-carousel ').empty();
			let imgSrc = "../static/images/jpeg/no_photo.jpg";
			if(imgId.length == 1 &&imgId[0].val && imgId[0].val.length >1) {
				let imgAllSrc=imgId[0].val;
				console.log(imgAllSrc);
				var carouselInner =  $('<div>').addClass('carousel-inner');
				for (var i = 0; i < imgAllSrc.length; i++) {
				   imgSrc = imgAllSrc[i];
				  console.log(imgSrc);
				  
				  var slide = $('<div>').addClass('carousel-item');
				  if (i === 0) {
					slide.addClass('active');
				  }
				  
				  var img = $('<img>').attr('src', imgSrc);
				  carouselInner.append( slide);
				  slide.append(img);
				  
				  $('#product-carousel ').append(carouselInner);
				} 
			  }else {
				var divImg =  $('<div>');
				var img = $('<img>').attr('src', imgId.length &&imgId[0].val && imgId[0].val.length == 1 ? imgId[0].val : imgSrc);
				divImg.append(img);
				$('#product-carousel ').append(divImg);
			  }
			  
			  
		//let cardImg = $(product).find('img').attr('src');
		let cardName = $(product).find('.card-title').text();
		let cardPartNumber = $(product).find('.card-text').text();
	    
		let $modal = $('#ipb');
		//$modal.find('img').attr('src',cardImg );
		$modal.find('h2.card-title').text(cardName);
		$modal.find('h3.card-title').text(cardPartNumber);
		$modal.find('a').prop('href', cardLink);
		$modal.modal('show');

	}
}




$('.carousel-control-next').click(function() {
    var $carouselInner = $(this).siblings('.carousel-inner');
    var itemWidth = $carouselInner.children('.carousel-item').outerWidth();
    $carouselInner.animate({ 'margin-left': -itemWidth }, 500, function() {
      $(this).css('margin-left', 0).children('.carousel-item').first().appendTo(this);
    });
  });
  
  $('.carousel-control-prev').click(function() {
    var $carouselInner = $(this).siblings('.carousel-inner');
    var itemWidth = $carouselInner.children('.carousel-item').outerWidth();
    $carouselInner.css('margin-left', -itemWidth).children('.carousel-item').last().prependTo($carouselInner);
    $carouselInner.animate({ 'margin-left': 0 }, 500);
  });

  /* -------------------carousel-------------------- */

	
  var slider = $('.slider');
  var slideWidth = $('.slide').outerWidth();//.outerWidth() utilisé pour obtenir la largeur de l’élément avec les padding и border
  
  function moveSlider() {
    slider.animate({'margin-left': -slideWidth}, "slow", function() {
      slider.css({marginLeft: 0});
      slider.find('.slide:first').appendTo(slider);
    });
  }

  	var interval = setInterval(moveSlider, 3000);

  $('.slide').hover(
	  function() {
	    clearInterval(interval); //Arrêtez carousel 
	  },
	  function() {
	    interval = setInterval(moveSlider, 3000); //Reprendre carousel
	  }
  );
	
//==============scrollToSection  ===== behavior со значением 'smooth' - плавная прокрутка===========
window.addEventListener('DOMContentLoaded', function() {
	var sectionId = window.location.hash;
  
	if (sectionId) {
	  var section = document.querySelector(sectionId);
  
	  if (section) {
		section.scrollIntoView({ behavior: 'smooth' });
	  }
	}
  });
//==============filter-toggle====================
	// clic btn filter
	$('.filter-toggle').click(function() {
	  $('.filter-section').toggleClass('hidden'); 
	  $(this).toggleClass('active');
	  var arrow = $(this).find('.arrow');
    	if ($(this).hasClass('active')) {
    	  arrow.html('&#9650;'); 
    	} else {
    	  arrow.html('&#9660;'); 
    	}
  
	});
  
	// clic sur input
	$('.filter-section input').click(function() {
	  $('.filter-section').addClass('hidden'); 
	  $('.filter-toggle').removeClass('active'); 
      $('.filter-toggle .arrow').html('&#9660;');
	});

//=============Post map
// $.post("/rest/get?url=http://oleksandr:8081/about", function(data) {
// 	let $asd=$(data);
// 	let d = $.grep($asd, a=>a.className=='about');
	
// 	$("#map").html(d);
//   });
 


// $('.has-checkbox').on('hide.bs.dropdown', function(e){

// 	if(!e.clickEvent)
// 		return;

// 	let parent = $(e.clickEvent.srcElement).parents('.m_filter');
// 	if(parent.length)
// 		e.preventDefault();
// })

//forming the URL  with certain filtering parameters
// $(".m_brend a").click(function(e){
//     e.preventDefault();
// 	let linkProduct = $(this).data('link-product');
// 	let parentProducts = $(this).parents('.m_products');
// 	let mtype= parentProducts.find('div.m-type');
// 	let typeChecked = mtype.find('input').filter(':checked');
// 	// let type = typeChecked.map(function(){
// 	// 	return  this.dataset.idType
// 	// }).get().join(',')
// 	let type = typeChecked.map(function(){
// 		return   $(this).data('id-type')
// 	}).get().join(',')
	
// 	let mpower= parentProducts.find('div.m_power');
// 	let powerChecked = mpower.find('input').filter(':checked');
// 	let power =  powerChecked.map(function(){
// 		return  this.dataset.idPower
// 	}).get().join(',') 
// 	let queryString = '?type='+ encodeURIComponent(type) + '&power='+ encodeURIComponent(power)+ '&link=' +encodeURIComponent(linkProduct);
// 	 window.location.href = 'products.html' + queryString;

	 
// 	 type.forEach(function(filter) {
// 		$('[data-id-type="' + filter + '"]').prop('checked', true);
// 	  }); 
	 
// 	})

	// var3
	// ============
	// $(".m_products a").click(function(e){
	// 	//alert('Hello')
	// 	e.preventDefault();
	// 	let environment = $(this).data('environment'),
	// 		family= $(this).data('family'),
	// 		type = $(this).data('type'),
	// 		power = $(this).data('power'),
	// 		voltage=$(this).data('voltage'),
	// 		device = $(this).data('device'),
	// 		queryString ="";
	// 		console.log(family);
	// 	if(environment){
	// 		queryString ='?environment='+ encodeURIComponent(environment)
	// 	}
	// 	if(family){
	// 		queryString ='?family='+ encodeURIComponent(family)
	// 	}
	// 	if(type){
	// 		queryString ='?type='+ encodeURIComponent(type)
	// 	}
	// 	if(power){
	// 		queryString ='?power='+ encodeURIComponent(power)
	// 	}
	// 	if(voltage){
	// 		queryString ='?voltage='+ encodeURIComponent(voltage)
	// 	}
	// 	if(device){
	// 		queryString ='?device='+ encodeURIComponent(device)
	// 	}
		
		
	// 	window.location.href = 'products.html' + queryString;
		 
	// 	})




	// processing URL parameters and setting the properties of elements on the page according to these parameters
  
    //get the URL settings
    // let urlParams = new URLSearchParams(window.location.search);
  	// //console.log(urlParams);
   
    //  let type = urlParams.get('type');
	//  //console.log(type);
    //  if(type){
    //    //convert parameter string to array
    //    let typeArr = type.split(',');
    //    //set 'checked' for corresponding elements
    //    typeArr.forEach(function(typeVal){
	//  	//console.log(typeVal);
    //      $('input[data-type="' + typeVal + '"]').prop('checked', true);
    //    });
    //  }
	// let family = urlParams.get('family')
	// if(family){
	// 	let familyArr=family.split(',');
	// 	familyArr.forEach(function(familyVal){
	// 		$('input[data-family="'+familyVal+'"]').prop('checked',true);
	// 	})
	// }
	// let power = urlParams.get('power');
	// //console.log(power);
	// if(power){
	// 	let powerArr = power.split(',');
	// 	console.log(powerArr);
	// 	powerArr.forEach(function(powerVal){
	// 		$('input[data-power="' + powerVal + '"]').prop('checked', true);
	// 	})
	// }

    // let linkProduct = urlParams.get('environment');
    // if(linkProduct){
    //   $('input[data-environment="' + linkProduct + '"]').prop('checked', true);
    // }
 


  


//   $('.section1').css('height', ($(window).height() - 300));



//pour retourner vers la page precedent
//========================
// var win = window.open("details.html", "_blank");
// win.focus();
// function goBack() {
// 	window.history.back();
//   }

  
  

  
  



  