// (temporary) globals
var moodseer_host = "http://172.16.17.17";
var moodseer_port = 8000;
var moodseer_station = 1;
var moodseer_server_url = moodseer_host + "/lib/ajaxresults.php";
var moodseer_stream_url = moodseer_host + ":" + moodseer_port + "/stream" + moodseer_station;

function imagerollover() {
    $("img[data-over]", "#playbuttons,#controlbuttons").mouseover(function() {
	$(this).attr("src", $(this).attr("data-over"));
    });
    $("img[data-over]", "#playbuttons,#controlbuttons").mouseout(function() {
	$(this).attr("src", $(this).attr("data-out"));
    });
} // end function imagerollover

function sendMoodseerCommand(cmd, successCallback, errorCallBack) {
    cmd = (typeof cmd == "undefined") ? "getzones" : cmd;
    successCallback = (typeof successCallback == "undefined") ? function() {} : successCallback;
    errorCallback = (typeof errorCallback == "undefined") ? function() {} : errorCallback;
    
    $.getJSON("http://172.16.17.17/lib/ajaxresults.php",
	      "cmd=" + cmd + "&zoneid=1&station=1&play_window=1",
	      successCallback);

}

function populateSongData(data, stat) {
    $("div#nowplaying .artist").html(data.nowplaying.artist);
    $("div#nowplaying .album").html(data.nowplaying.album);
    $("div#nowplaying .song").html(data.nowplaying.song);
    $("div#nowplaying .album-image img").attr("src", "http://172.16.17.17/" + data.nowplaying.coverart_url).attr("alt", "Album cover art");
}

function populateUpcoming(data, stat) {
    var items = [];
    $.each(data.upcoming, function(i, item) {
	items.push("<li>" + item.song + " - " + item.artist + "</li>");
    });
    $("#experimental #upcoming ol.songlist").empty().append(items.join('\n'));
}

function updateSongData() {
    $.getJSON(moodseer_server_url,
	      "cmd=nowplaying&stationNum=1&station=1&window=1",
	      populateSongData);
}

function updateUpcoming() {
    $.getJSON(moodseer_server_url,
	      "cmd=upcoming&stationNum=1&station=1&window=10",
	      populateUpcoming);
}

function toggleMute() {
    sendMoodseerCommand("toggle_mute", function(data, stat) {
	if (data.volume.muted == "true") {
	    $("#controlbuttons #mute img.mute-off").hide();
	    $("#controlbuttons #mute img.mute-on").show();
	} else {
	    $("#controlbuttons #mute img.mute-off").show();
	    $("#controlbuttons #mute img.mute-on").hide();
	}
    });
}

$(document).ready(function() {
    $("#controlbuttons #mute img.mute-on").hide();
    $("#playbuttons #play img.paused").hide();
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
	    updateUpcoming();
	});

    });


    $("div#controlbuttons #mute").click(function() {
	toggleMute();
    });

    $("div#playbuttons #back").click(function() {
	sendMoodseerCommand("prev", function(data, stat) {
	    populateSongData(data, stat);
	});

    });

    updateSongData();
    updateUpcoming();
    setInterval(function() { updateSongData(); updateUpcoming();
			   }, 20000);

});

