$(document).ready(function () {
    $("#integrateDataSourcesButton").click(function () {
        var dataSources = [];
        $.each($("input[name='dataSource']:checked"), function () {
            dataSources.push($(this).val());
        });
        alert("My favourite colors are: " + dataSources.join(", "));
    });

    $("tbody").on('change', 'input[type=checkbox]', function () {
        if ($('input[type=checkbox]:checked').length > 2) {
            $(this).prop('checked', false);
            alert("You can maximum select two sources at a time.");
        } else if ($('input[type=checkbox]:checked').length === 2) {
            $("#integrateDataSourcesButton").removeClass("disabled");
        } else {
            $("#integrateDataSourcesButton").addClass("disabled");
        }
    });
});

