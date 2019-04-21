/**
 * Created by Kashif Rabbani
 */
var config = require(__dirname + '/../config'),
    request = require('request');

exports.triggerDataSourcesIntegration = function (req, res) {
    console.log("triggerDataSourcesIntegration");
    var globalResponse = false;
    if (!(req.body.hasOwnProperty('id1')) || req.body.id1 == null ||
        !(req.body.hasOwnProperty('id2')) || req.body.id2 == null) {
        res.status(400).json({msg: "(Bad Request) data format: {id1, id2}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL + 'schemaIntegration';
        //console.log(url);
        //console.log(objDataSource);

        request.post({
            url: url,
            body: JSON.stringify(objDataSource)
        }, function done(error, response, body) {
            if (!error && response.statusCode === 200) {
                console.log(body);
                res.status(200).json("Successful Response from the backend!");
                globalResponse = true;
                //res.end();
            } else {
                response.status(500).send("Error in the backend");
                globalResponse = false;
            }
        });

    }
};
