/**
 * Created by snadal on 07/06/16.
 * Updated by Kashif-Rabbani on 08-04-2018
 */

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(function () {

    $('#dataSourceForm').on("submit", function (e) {
        e.preventDefault();
        var dataSource = new FormData();
        switch ($('.nav-tabs .active').attr('id')) {

            case "json-tab":
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "json");
                // Get the files from input, create new FormData.
                var files = $('#file_path').get(0).files;

                // Append the files to the formData.
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    dataSource.append('JSON_FILE', file, file.name);
                }

                break;

            case "xml-tab":
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "xml");
                // Get the files from input, create new FormData.
                var filesXML = $('#xml_path').get(0).files;

                // Append the files to the formData.
                for (var x = 0; x < filesXML.length; x++) {
                    var fileXML = filesXML[x];
                    dataSource.append('XML_FILE', fileXML, fileXML.name);
                }

                break;

            case "sqldatabase-tab":
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "SQL");
                dataSource.append("sql_jdbc", $("#sql_path").val());
                break;
        }
        $.ajax({
            url: '/fileupload',
            method: "POST",
            data: dataSource,
            processData: false,
            contentType: false,
            xhr: function () {
                var xhr = new XMLHttpRequest();
                if (dataSource.get("givenType") !== 'SQL') {
                    // Add progress event listener to the upload.
                    xhr.upload.addEventListener('progress', function (event) {
                        var progressBar = $('.progress-bar');

                        if (event.lengthComputable) {
                            var percent = (event.loaded / event.total) * 100;
                            progressBar.width(percent + '%');

                            if (percent === 100) {
                                progressBar.removeClass('active');
                            }
                        }
                    });

                }

                return xhr;
            }

        }).done(function (data) {
            //window.location.href = '/fileupload';
            console.log(data);
        }).fail(function (err) {
            alert("error " + JSON.stringify(err));
        });


        // Set the progress bar to 0 when a file(s) is selected.
        $('#json-tab').on('click', function () {
            $('.progress-bar').width('0%');
        });

        // Set the progress bar to 0 when a file(s) is selected.
        $('#xml-tab').on('click', function () {
            $('.progress-bar').width('0%');
        });

        // Set the progress bar to 0 when a file(s) is selected.
        $('#sqldatabase-tab').on('click', function () {
            $('.progress-bar').width('0%');
        });


        // Set the progress bar to 0 when a file(s) is selected.
        $('#json_pathForm').on('click', function () {
            $('.progress-bar').width('0%');
        });

        // Set the progress bar to 0 when a file(s) is selected.
        $('#xml_pathForm').on('click', function () {
            $('.progress-bar').width('0%');
        });

        // Set the progress bar to 0 when a file(s) is selected.
        $('#sql_jdbcForm').on('click', function () {
            $('.progress-bar').width('0%');
        });


    });


});