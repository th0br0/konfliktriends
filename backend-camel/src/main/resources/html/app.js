"use strict";

// Setup map.
var map = L.mapbox.map('map', 'fcc.map-toolde8w', {
    accessToken: "pk.eyJ1IjoidGgwYnIwIiwiYSI6IjYwdHpBYWsifQ.qNw3Epcuw_AkvqNNXNCgng"
}).setView([31.5171, 34.6715], 9);

function updateTimestamp() {
    var el = document.getElementById("timestamp");
    var time = new Date().toTimeString().replace(/.*(\d{2}:\d{2}:\d{2}).*/, "$1");
    el.innerHTML = time;

    setTimeout(updateTimestamp, 100);
}


map.featureLayer.on('ready', function () {
    updateTimestamp();
    setupCountries();
    updateLayers();


    map.legendControl.addLegend(document.getElementById('legend').innerHTML);
    // Setup websocket!
    var ws = new WebSocket("ws://" + window.location.hostname + ":9292/websocket");
    ws.onopen = function (evt) {
        console.log(evt);
    };

    ws.onmessage = function (evt) {
        var data = JSON.parse(evt.data);

        var color = 'red';
        if (data.weight > 0) color = 'green';
        else if (data.weight == 0) color = 'blue';
        var source = L.latLng(data.from.lat, data.from.lng);
        for (var idx in data.to) {
            var to = data.to[idx];
            var dest = L.latLng(to.lat, to.lng);

            accountWeight(dest, data.weight);

            var b = new R.BezierAnim([source, dest], {'stroke': color}, function () {
                var p = new R.Pulse(
                    dest, 20 * Math.abs(data.weight),
                    {
                        'stroke': color,
                        'fill': color
                    }, {
                        'stroke': color,
                        'fill': color
                    });


                map.addLayer(p);
                setTimeout((function (bez, pul) {
                    return function () {
                        map.removeLayer(bez).removeLayer(pul)
                    }
                })(b, p));
            }, 1000);
        }
        map.addLayer(b);
    }
});


//    L.geoJson(data).addTo(map);
