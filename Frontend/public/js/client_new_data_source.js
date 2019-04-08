/**
 * Created by snadal on 07/06/16.
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
                    dataSource.append('FILE', file, file.name);
                }

                break;

            case "xml-tab":
                dataSource.append("givenName", $("#givenName").val());
                dataSource.append("givenType", "xml");
                // Get the files from input, create new FormData.
                var files = $('#xml_path').get(0).files;

                // Append the files to the formData.
                for (var i = 0; i < files.length; i++) {
                    var file = files[i];
                    dataSource.append('FILE', file, file.name);
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
            contentType: false
        }).done(function (data) {
            //window.location.href = '/fileupload';
            console.log(data);
        }).fail(function (err) {
            alert("error " + JSON.stringify(err));
        });
    });

});