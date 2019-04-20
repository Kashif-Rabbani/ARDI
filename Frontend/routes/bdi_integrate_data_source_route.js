/**
 * Created by Kashif Rabbani
 */
var config = require(__dirname + '/../config'),
    request = require('request');

exports.triggerDataSourcesIntegration = function (req, res) {
    //console.log(req.body);
    //req.body = req.body[0];

    if (!(req.body.hasOwnProperty('id1')) || req.body.id1 == null ||
        !(req.body.hasOwnProperty('id2')) || req.body.id2 == null) {
        res.status(400).json({msg: "(Bad Request) data format: {id1, id2}"});
    } else {
        var objDataSource = req.body;
        var url = config.BDI_DATA_LAYER_URL;

        console.log(objDataSource);
        res.status(200).json("ok");
    }
};
