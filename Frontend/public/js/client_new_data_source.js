/**
 * Author Kashif-Rabbani
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
                if ($("#file_path").get(0).files.length === 0) {
                    console.log("jsonfile");
                    return false;
                }
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
                if ($("#xml_path").get(0).files.length === 0) {
                    console.log("xmltab");
                    return false;
                }
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

                if ($("#sql_path").val() === '') {
                    console.log("sqldb");
                    return false;
                }
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "SQL");
                dataSource.append("sql_jdbc", $("#sql_path").val());
                break;
        }
        handler(dataSource);
    });
    handleProgressBar();
});

function handler(dataSource) {
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
        parseSource(data);
    }).fail(function (err) {
        alert("Error " + JSON.stringify(err));
    });
}

function parseSource(data) {
    console.log(JSON.stringify(data));
    toggleModal();

    var clickHandler = function (e) {
        e.preventDefault();
        toggleModal();
        $.ajax({
            type: 'POST',
            data: JSON.stringify(data),
            contentType: 'application/json',
            url: '/triggerExtraction',
            cache: false,
            success: function (response) {
                console.log('success');
                console.log(JSON.stringify(response));
                window.location.href = '/';
            },
            error: function (response) {
                console.log('failure');
                console.log(JSON.stringify(response));
            }
        });
        e.stopImmediatePropagation();
        return false;
    }
    $('#ModalProceedButton').one('click', clickHandler);
}

function getParsedFileDetails() {
    $.get("/dataSource", function (data) {
        console.log("getParsedFileDetails: " + data);
        var i = 1;
        $.each((data), function (key, value) {
            var dataSource = JSON.parse(value);
            $('#dataSources').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(dataSource.name)
                    ).append($('<td>')
                        .text(dataSource.type)
                    ).append($('<td>')
                        .text(dataSource.address)
                    ).append($('<td>').append($('<a href="/view_data_source?dataSourceID=' + (dataSource.dataSourceID) + '">').append($('<span class="fa fa-search"></span>')))
                    ).append($('<td>').append($('<a href="/view_source_graph?iri=' + (dataSource.iri) + '">').append($('<span class="fa fa-search"></span>')))
                    )
                );

            ++i;
        });
    });
}

$(function () {
    getParsedFileDetails();
});

function toggleModal() {
    $('#confirmationModal').modal('toggle');
}

function handleProgressBar() {
    $('#json-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#xml-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#sqldatabase-tab').on('click', function () {
        $('.progress-bar').width('0%');
        $(this).closest('form').find("input[type=file],input[type=text]").val("");
    });

    $('#json_pathForm').on('click', function () {
        $('.progress-bar').width('0%');
    });

    $('#xml_pathForm').on('click', function () {
        $('.progress-bar').width('0%');
    });

    $('#sql_jdbcForm').on('click', function () {
        $('.progress-bar').width('0%');
    });
}