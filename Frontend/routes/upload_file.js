var formidable = require('formidable'),
    fs = require('fs'),
    config = require(__dirname + '/../config'),
    upload_path = config.BDI_FILES_PATH;

exports.uploadFile = function (req, res) {

    var uploadedFile = [],
        form = new formidable.IncomingForm(),
        givenFileName = '',
        fileType = '',
        sql_JDBC = '';
    form.uploadDir = config.BDI_FILES_PATH;

    form.parse(req, function (err, fields, files) {
        givenFileName = fields.givenName;
        fileType = fields.givenType;
        if (fields.sql_jdbc) {
            //console.log(fields.sql_jdbc);
            sql_JDBC = fields.sql_jdbc;
            uploadedFile.push({
                status: true,
                filename: givenFileName,
                type: fileType,
                filePath: sql_JDBC
            });
        }
    });


    // Invoked when a file has finished uploading.
    form.on('file', function (name, file) {
        var filename = '';
        // Check the file type, must be xml or json
        if (file.type === 'text/xml' || file.type === 'application/json') {
            // Assign new file name

            filename = Date.now() + '-' + file.name;

            // Move the file with the new file name
            fs.rename(file.path, upload_path + "/" + filename);

            // Add to the list of photos
            uploadedFile.push({
                status: true,
                filename: filename,
                type: file.type,
                filePath: upload_path + '/' + filename
            });
        } else {
            uploadedFile.push({
                status: false,
                filename: file.name,
                message: 'INVALID'
            });
            fs.unlink(file.path);
        }
    });

    form.on('error', function (err) {
        console.log('Error occurred during processing - ' + err);
    });

    // Invoked when all the fields have been processed.
    form.on('end', function () {
        console.log('All the request fields have been processed.');
    });

    // Parse the incoming form fields.
    form.parse(req, function (err, fields, files) {
        res.status(200).json(uploadedFile);
    });
};
