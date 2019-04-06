
var config = require(__dirname+'/../config'),
    request = require('request');


exports.getGraph = function (req, res, next) {
    request.get(config.BDI_DATA_LAYER_URL + "json/", function (error, response, body) {
        if (!error && response.statusCode == 200) {
            res.status(200).json(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving graph content");
        }
    });
};


