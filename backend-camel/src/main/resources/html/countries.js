"use strict";

var pse = {
    prefix: "pse",
    layer: null,
    good: 0.5,
    bad: 0.5
};

var isr = {
    prefix: "isr",
    layer: null,
    good: 0.5,
    bad: 0.5
};

function showLayers(yes) {
    if (yes) {
        isr.layer.setStyle({opacity: 1, fill: true});
        pse.layer.setStyle({opacity: 1, fill: true});
    } else {
        isr.layer.setStyle({opacity: 0, fill: false});
        pse.layer.setStyle({opacity: 0, fill: false});
    }
}


function accountWeight(dest, weight) {
    weight = Math.log(weight);
    function applyWeight(where) {
        if (weight < 0) where.bad -= weight;
        else where.good += weight;

        updateLayers();
    }

    if (pse.layer.getBounds().contains(dest))
        applyWeight(pse);
    else if (isr.layer.getBounds().contains(dest))
        applyWeight(isr);
    else console.log("Dest not in regions: " + dest);
}

function updateLayers() {
    function updateLayer(which) {
        var total = Math.abs(which.good) + Math.abs(which.bad);
        var goodPct = which.good / total;
        // Wondering whether this has an effect given the partial colouring sometimes.
        var badPct = Math.abs(which.bad / total);

        d3.select("#" + which.prefix + "Bad").attr("offset", badPct);
        d3.select("#" + which.prefix + "Good").attr("offset", goodPct);

    }

    updateLayer(pse);
    updateLayer(isr);
}

function setupCountries() {

    d3.json("geo/ISR.geo.json", function (err, json) {
        isr.layer = L.geoJson(json, {
            style: {
                color: "#000",
                fill: false,
                opacity: 0,
                stroke: 0,
                fillColor: "url(#isrGradient)"
            }
        });

        isr.layer.addTo(map);
    });


    d3.json("geo/PSE.geo.json", function (err, json) {
        pse.layer = L.geoJson(json, {
            style: {
                color: "#FFF",
                fill: false,
                opacity: 0,
                fillColor: "url(#pseGradient)"
            }
        });

        pse.layer.addTo(map);
    });
}


