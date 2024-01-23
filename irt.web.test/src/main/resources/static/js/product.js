
var urlParams = new URLSearchParams(window.location.search);
var productId = parseInt(urlParams.get('productId'));
similarsProd = [];
console.log(productId);
function prod(el, ind,array){
   return el.id === productId
}
var product = products.find(prod);
console.log(product);
if (product) {
    var productNameElement = document.querySelector('.nameProd');
    var partNumberElement = document.querySelector('.partNumber');
  
    productNameElement.textContent = product.name;
    partNumberElement.textContent = product.partNumber;

    similarsProd = products.filter(function(item) {
        if (item.id === product.id) {
            return false;
        }
        var similarsFilterCount = 0;
    
        product.filterIds.forEach(function(filterId) {
          if (item.filterIds.includes(filterId)) {
            similarsFilterCount++;
          }
        });
    
        
        return similarsFilterCount >= 2;
      });

  }
  console.log(similarsProd);


let similarProductContainer = document.querySelector('.similar-products .row');
similarProductContainer.innerHTML = ''; 

similarsProd.slice(0, 3).forEach(makeSimilarProduct);
function makeSimilarProduct(similarProduct) {
    var cardDiv = document.createElement('div');
    cardDiv.classList.add('col-md-4', 'mb-4');

    var card = `
    <div class="card">
        <img src="${img.url.val[1]}" class="card-img-top my-2">
      <div class="card-body">
        <h5 class="card-title fw-bold similar-name">${similarProduct.name}</h5>
        <p class="card-text similar-number">${similarProduct.partNumber}</p>
        <a href="detail.html?productId=${similarProduct.id}" class="btn shine-button">in more detail</a>
      </div>
    </div>
  `;
  cardDiv.innerHTML = card;
  similarProductContainer.appendChild(cardDiv);
}


