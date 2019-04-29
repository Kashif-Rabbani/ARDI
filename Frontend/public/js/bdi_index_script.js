/**
 * Created by Kashif-Rabbani
 */
function getIntegratedFileDetails() {
    $.get("/bdiIntegratedDataSources", function (data) {
        console.log(data);
        var i = 1;
        $.each((data), function (key, value) {
            var dataSource = JSON.parse(value);
            $('#integratedDataSources').find('tbody')
                .append($('<tr>')
                    .append($('<td>').append('<input type="checkbox" class="dataSourceCheckbox" name="dataSource" value = "' + dataSource.dataSourceID + '" /> '))
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>').text(dataSource.name))
                    .append($('<td>')
                        .text(dataSource.iri)
                    ).append($('<td>')
                        .text( dataSource.parsedFileAddress)
                    )//.append($('<td>').append($('<a href="/view_data_source?dataSourceID=' + (dataSource.dataSourceID) + '">').append($('<span class="fa fa-search"></span>'))))
                    .append($('<td>').append($('<a href="/view/' + (dataSource.integratedVowlJsonFileName) + '&Integrated' + '">').append($('<span class="fa fa-search"></span>')))
                    )
                );

            ++i;
        });
    });
}

function getParsedFileDetails() {
    $.get("/bdiDataSources", function (data) {
        console.log(data);
        var i = 1;
        $.each((data), function (key, value) {
            var dataSource = JSON.parse(value);
            $('#dataSources').find('tbody')
                .append($('<tr>')
                    .append($('<td>').append('<input type="checkbox" class="dataSourceCheckbox" name="dataSource" value = "' + dataSource.dataSourceID + '" /> '))
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(dataSource.name)
                    ).append($('<td>')
                        .text(dataSource.type)
                    ).append($('<td>')
                        .text(dataSource.parsedFileAddress)
                    )//.append($('<td>').append($('<a href="/view_data_source?dataSourceID=' + (dataSource.dataSourceID) + '">').append($('<span class="fa fa-search"></span>'))))
                    .append($('<td>').append($('<a href="/view/' + (dataSource.vowlJsonFileName) + '&' + dataSource.name + '">').append($('<span class="fa fa-search"></span>')))
                    )
                );

            ++i;
        });
    });
}

function goToAlignmentsView(data) {
    //console.log("Inside Alignments" + data + '/integration/:ids_id&ds1_id&:ds2_id&:ds1_name&:ds2_name&:align_iri');
    var d = JSON.parse(data);
    // var url = '/integration/' + d.integratedDataSourceID + '&' + d.dataSourceID1 + '&' + d.dataSourceID2 + '&' +
    //     d.dataSource1Name + '&' + d.dataSource2Name + '&' + d.alignmentsIRI + '&' + d.integratedIRI;
    var url = '/integration/' + d.integratedDataSourceID + '&' +
        d.dataSource1Name + '&' + d.dataSource2Name;
    console.log(url);

    $("#overlay").fadeOut(300);
    window.location.href = url;
}

$(function () {
    getParsedFileDetails();
    getIntegratedFileDetails();
});
$(document).ready(function () {

    $(document).ajaxSend(function () {
        $("#overlay").fadeIn(100);
    });

    $(document).ajaxStop(function () {
        $("#overlay").fadeOut(100);
    });
    $("body").on('change', 'input[type=checkbox]', function () {
        var checkedCheckedBoxes = $('input[type=checkbox]:checked').length;
        var integrateButton = $("#integrateDataSourcesButton");
        if (checkedCheckedBoxes > 2) {
            $(this).prop('checked', false);
            console.log("You can maximum select two sources at a time.");
        }

        if (checkedCheckedBoxes === 2) {
            integrateButton.removeClass("disabled");
            integrateButton.prop('disabled', false);
        }
        if (checkedCheckedBoxes < 2) {
            integrateButton.addClass("disabled");
            integrateButton.prop('disabled', true);
        }
        /*if (!integrateButton.hasClass('disabled')) {

        }*/
    });


});

$(function () {
    $('#integrateDataSourcesButton').on("click", function (e) {
        e.preventDefault();
        if (!$("#integrateDataSourcesButton").hasClass('disabled')) {
            console.log("Clicked #integrateDataSourcesButton");

            var dataSources = [];
            $.each($("input[name='dataSource']:checked"), function () {
                dataSources.push($(this).val());
            });
            console.log("Selected data Sources are: " + dataSources.join(", "));
            var object = {};
            object.id1 = dataSources[0];
            object.id2 = dataSources[1];
            console.log(object);

            if(object.id1.includes("INTEGRATED_") && object.id2.includes("INTEGRATED_")){
                console.log("Integration of Global Graphs not allowed yet.");
            } else {
                window.location.href = '/integration/' + dataSources[0] + '&' + dataSources[1];
            }



            /*$.ajax({
                url: '/integrateDataSources',
                method: "POST",
                data: object
            }).done(function (data) {
                console.log('Success');
                console.log(data);
                goToAlignmentsView(data);
            }).fail(function (err) {
                alert("Error Integrating sources " + JSON.stringify(err));
            });*/
        }
    });

});
