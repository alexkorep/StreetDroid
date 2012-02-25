function updateProgress() {
	var images = [
	      	    "img/progress1.png",
	      	    "img/progress2.png",
	      	    "img/progress3.png",
	      	    "img/progress4.png"
	      	];
	
	var image = document.getElementById(_imgId);
	//image.src = "img/progress1.png";
	image.src = images[_progress];
	
	_progress++;
	if (_progress >= images.length) {
		_progress = 0;
	}
}

function startProgress(imgId) {
	_progress = 0;
	_imgId = imgId;
	
	var image = document.getElementById(_imgId);
	image.style.display = 'inline';
	
	_intervalId = setInterval('updateProgress()', 250); 
}

function stopProgress() {
	var image = document.getElementById(_imgId);
	image.style.display = 'none';
	clearInterval(_intervalId);
}