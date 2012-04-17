$(document).ready(function() {
    $("#jquery_jplayer_1").jPlayer({
        ready: function () {
            $(this).jPlayer("setMedia", {
		mp3: "http://172.16.17.17:8123",
            });
        },
	preload: "none",
        swfPath: "/js/jQuery.jPlayer.2.1.0",
        supplied: "mp3, m4a, oga"
    });

});
