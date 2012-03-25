function imagerollover() {
    $("img[data-over]", "#playbuttons").mouseover(function() {
	$(this).attr("src", $(this).attr("data-over"));
    });
    $("img[data-over]", "#playbuttons").mouseout(function() {
	$(this).attr("src", $(this).attr("data-out"));
    });
} // end function imagerollover

function sendMoodseerCommand(cmd, successCallback, errorCallBack) {
    cmd = (typeof cmd == "undefined") ? "getzones" : cmd;
    successCallback = (typeof successCallback == "undefined") ? function() {} : successCallback;
    errorCallback = (typeof errorCallback == "undefined") ? function() {} : errorCallback;
    
    // $.ajax({
    // 	url: "http://172.16.17.17/lib/ajaxresults.php",
    // 	data: "cmd=" + cmd + "&zoneid=1&play_window=10",
    // 	dataType: "jsonp",
    // 	success: successCallback,
    // 	error: errorCallback
    // 	});

    $.getJSON("http://172.16.17.17/lib/ajaxresults.php?cmd=stop&zoneid=1&play_window=10",
	      "cmd=" + cmd + "&zoneid=1&play_window=1",
	      successCallback);

}

function populateSongData(data, stat) {
    $("div#nowplaying .artist").html(data.nowplaying.artist);
    $("div#nowplaying .album").html(data.nowplaying.album);
    $("div#nowplaying .song").html(data.nowplaying.song);
    $("div#nowplaying .album-image img").attr("src", "http://172.16.17.17/" + data.nowplaying.coverart_url).attr("alt", "Album cover art");
}

$(document).ready(function() {
    imagerollover();
    $("div#playbuttons #play").click(function() {
	sendMoodseerCommand("play", function(data, stat) {
	    populateSongData(data, stat);
	});
    });

    $("div#playbuttons #stop").click(function() {
	sendMoodseerCommand("stop", function(data, stat) {
	    $("div#nowplaying .text").html("")
	    $("div#nowplaying .album-image img").attr("src", "").attr("alt", "");
	});
    });

    $("div#playbuttons #forward").click(function() {
	sendMoodseerCommand("next", function(data, stat) {
	    populateSongData(data, stat);
	});

    });

    $("div#playbuttons #back").click(function() {
	sendMoodseerCommand("prev", function(data, stat) {
	    populateSongData(data, stat);
	});

    });
});
    
