
/* -------------------Fill Nodes-------------------- */
getDataFromDB();
function getDataFromDB(){

	$.post('/rest/page_valiables', {pageName: 'home_page'}, data=>{

		if(!data)
			return;

		data.forEach(n=>{

			switch(n.valueType){
			case 'TEXT':
				$('#' + n.nodeId).text(n.value);
				break;
			case "CLASS":
				$('#' + n.nodeId).addClass(n.value);
			}
		});
	});
}

/* -------------------carousel-------------------- */
let $slider = $('.slider');
let $sliderParent = $slider.parent();
let $slide = $('.slide');

function moveSlider() {

	if(!$slide.length)
		return;

	let slide = $slide[0];
	let style = slide.currentStyle || window.getComputedStyle(slide);
	let slideWidth = slide.offsetWidth + parseInt(style.marginLeft) + parseInt(style.marginRight);	//.outerWidth() utilisé pour obtenir la largeur de l’élément avec les padding и border

	let sum = slideWidth * $slider.children().length;	// The sum of all card width

	if($sliderParent.width() < sum)

		$slider.animate({'margin-left': -slideWidth}, "slow", ()=> {

			$slider.css({marginLeft: 0});
			$slider.find('.slide:first').appendTo($slider);
		});
}

let intervalTime = 3000;
let interval = setInterval(moveSlider, intervalTime);
//$slide.hover(
//	()=>{
//		clearInterval(interval); //Arrêtez carousel 
//	},
//	()=>{
//		clearInterval(interval);
//		interval = setInterval(moveSlider, intervalTime); //Reprendre carousel
//	}
//);
$(document).on('visibilitychange', ()=>{

    if(document.visibilityState == 'hidden')
		clearInterval(interval); //Arrêtez carousel 
    else {
		clearInterval(interval);
		interval = setInterval(moveSlider, intervalTime); //Reprendre carousel
    }
    console.log(document.visibilityState);
});
