
$('.has-checkbox').on('hide.bs.dropdown', function(e){
	if(!e.clickEvent)
		return;
	
	let localName = e.clickEvent.srcElement.localName;
	let outDropdownMenu = $(e.clickEvent.target).closest('.dropdown-menu').length > 0;
	console.log(localName);

  if (localName != 'a' && outDropdownMenu) {
    e.preventDefault();
  }

});
	
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

  
  



  