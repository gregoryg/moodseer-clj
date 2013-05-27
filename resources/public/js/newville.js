var moodseer_api_url = "http://smeagol:8080/api";


function imagerollover() {
    $("img[data-over]", "#playbuttons,#controlbuttons").mouseover(function() {
	$(this).attr("src", $(this).attr("data-over"));
    });
    $("img[data-over]", "#playbuttons,#controlbuttons").mouseout(function() {
	$(this).attr("src", $(this).attr("data-out"));
    });
} // end function imagerollover

function sendMoodseer(cmd, successCallback, errorCallBack) {
    cmd = (typeof cmd == "undefined") ? "getzones" : cmd;
    successCallback = (typeof successCallback == "undefined") ? function() {} : successCallback;
    errorCallback = (typeof errorCallback == "undefined") ? function() {} : errorCallback;

    $.getJSON(moodseer_api_url,
	      "cmd=" + cmd + "&garbage=whatever",
	      successCallback);
}

function updateNowplaying() {
    sendMoodseer("nowplaying",
		 function(data, stat) {
		     $("div#nowplaying .artist").html(data.nowplaying.artist);
		     $("div#nowplaying .album").html(data.nowplaying.album);
		     $("div#nowplaying .song").html(data.nowplaying.song);
		 });
}

function populateUpcoming(data, str) {
    var items = [];
    $.each(data.upcoming.songlist, function(i, item) {
	items.push("<li>" + item + "</li>");
    });
    $("div#upcoming ol.songlist").empty().append(items.join('\n'));
}

function updateVolume() {
    $.getJSON(moodseer_api_url,
	      "cmd=volume",
	      function(data, stat) {
		  $("#volume").text(data.volume);
		  console.log(data);
	      });
}

function updateUpcoming() {
    $.getJSON(moodseer_api_url,
	      "cmd=upcoming",
	      populateUpcoming);
}

$(document).ready(function() {
    $("#controlbuttons #mute img.mute-on").hide();
    $("#playbuttons #play img.paused").hide();
    imagerollover();
    
    $("div#playbuttons #really-quit").click(function() {
	sendMoodseer("quit")
    });

    $("div#playbuttons #play").click(function() {
	sendMoodseer("play", function(data, stat) {
	    updateNowplaying();
	    updateUpcoming();
	});
    });

    $("div#playbuttons #stop").click(function() {
	sendMoodseer("stop")
    });
    
    $("div#playbuttons #forward").click(function() {
	sendMoodseer("next", function(data, stat) {
	    updateNowplaying();
	    updateUpcoming();
	    // setTimeout(updateNowplaying, 1000);
	});
    });
    
    $("div#playbuttons #back").click(function() {
	sendMoodseer("prev", function(data, stat) {
	    updateNowplaying();
	    updateUpcoming();

	    // setTimeout(updateNowplaying, 1000);
	});
    });

    $("div#controlbuttons #volume-up").click(function() {
	sendMoodseer("louder", function(data, stat) {
	    updateVolume();
	});
	
    });

    $("div#controlbuttons #volume-down").click(function() {
	sendMoodseer("softer", function(data, stat) {
	    updateVolume();
	});
	
    });

    $("div#controlbuttons #pause").click(function() {
	sendMoodseer("pause");
    });
	
});
