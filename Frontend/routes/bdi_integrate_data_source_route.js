/**
 * Created by Kashif Rabbani
 */
var config = require(__dirname + '/../config'),
    request = require('request');

exports.triggerDataSourcesIntegration = function (req, res, next) {
    console.log("triggerDataSourcesIntegration");

    if (!(req.body.hasOwnProperty('id1')) || req.body.id1 == null ||
        !(req.body.hasOwnProperty('id2')) || req.body.id2 == null) {
        res.status(400).json({msg: "(Bad Request) data format: {id1, id2}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL + 'schemaIntegration';
        console.log(url);
        console.log(objDataSource);

        /*setTimeout(function(){
            res.status(200).send("DONE");
        }, 5000);*/

        request.post({
            url: url,
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode === 200) {
                console.log(body);
                res.status(200).send(body);
            } else {
                res.status(500).send("Error in the backend");
            }
        });
        //res.status(200).send("DONE");
    }
};

exports.getAlignments = function (req, res, next) {
    console.log("THIS IS iri: " + req.params.iri);
    request.get(config.BDI_DATA_LAYER_URL + "getSchemaAlignments/" + req.params.iri, function (error, response, body) {
        if (!error && response.statusCode === 200) {
            res.status(200).send(JSON.parse(body));
        } else {
            res.status(500).send("Error retrieving Alignments");
        }
    });
};


exports.acceptAlignment = function (req, res, next) {
    console.log("triggeracceptAlignment");
    console.log(req.body);
    if (!(req.body.hasOwnProperty('p')) || req.body.p == null ||
        !(req.body.hasOwnProperty('s')) || req.body.s == null ||
        !(req.body.hasOwnProperty('o')) || req.body.o == null ||
        !(req.body.hasOwnProperty('integrated_iri')) || req.body.integrated_iri == null) {
        res.status(400).json({msg: "(Bad Request) data format: {P, S, O, ID}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL + 'acceptAlignment';
        console.log(url);
        console.log(objDataSource);

        /*setTimeout(function(){
            res.status(200).send("DONE");
        }, 5000);*/

        request.post({
            url: url,
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode === 200) {
                console.log(body);
                res.status(200).send(body);
            } else {
                res.status(500).send("Error in the backend");
            }
        });
        //res.status(200).send("DONE");
    }
};