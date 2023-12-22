
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
let $slide = $('.slide');
let slideWidth = $slide.outerWidth();//.outerWidth() utilisé pour obtenir la largeur de l’élément avec les padding и border

function moveSlider() {
	$slider.animate({'margin-left': -slideWidth-10}, "slow", function() {
			$slider.css({marginLeft: 0});
			$slider.find('.slide:first').appendTo($slider);
	});
}

var interval = setInterval(moveSlider, 3000);

$slide
.hover(
	function() {
		clearInterval(interval); //Arrêtez carousel 
	},
	function() {
		interval = setInterval(moveSlider, 3000); //Reprendre carousel
	}
);
$(window).on('resize',e=>{
	slideWidth = $slide.outerWidth()
});
