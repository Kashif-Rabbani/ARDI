/**
 * Created by Kashif-Rabbani
 */
$(document).ready(function () {

    $(document).ajaxSend(function () {
        $("#overlay").fadeIn(100);
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
        if (!integrateButton.hasClass('disabled')) {
            integrationButtonClickHandler(integrateButton);
        }
    });
});

function integrationButtonClickHandler(integrateButton) {
    var clickHandler = function (e) {
        e.preventDefault();
        integrateButton.prop('disabled', false);

        var dataSources = [];
        $.each($("input[name='dataSource']:checked"), function () {
            dataSources.push($(this).val());
        });
        console.log("Selected data Sources are: " + dataSources.join(", "));
        var object = {};
        object.id1 = dataSources[0];
        object.id2 = dataSources[1];
        console.log(object);


        $.ajax({
            type: 'POST',
            data: JSON.stringify(object),
            contentType: 'application/json',
            url: '/integrateDataSources',
            success: function (response) {
                console.log('Success');
                //$("#overlay").fadeOut(200);
                goToAlignmentsView(response);
            },
            error: function (response) {
                alert('failure' + JSON.stringify(response));
                console.log(JSON.stringify(response));
                $("#overlay").fadeOut(200);
            }
        });
        e.stopImmediatePropagation();
        return false;
    };
    integrateButton.one('click', clickHandler);
}

function goToAlignmentsView(data) {
    //console.log("Inside Alignments" + data + '/integration/:ids_id&ds1_id&:ds2_id&:ds1_name&:ds2_name&:align_iri');
    var d = JSON.parse(data);
    var url = '/integration/' + d.integratedDataSourceID + '&' + d.dataSourceID1 + '&' + d.dataSourceID2 + '&' +
        d.dataSource1Name + '&' + d.dataSource2Name + '&' + d.alignmentsIRI + '&' + d.integratedIRI;

    console.log(url);

    $("#overlay").fadeOut(300);
    window.location.href = url;
}


