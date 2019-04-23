var integratedDataInfoObject = {};
var alignmentsData = {};

var bdi_integrate_url = window.location.href;
//var url_prefix = bdi_integrate_url.split('/integration/')[0];
var url_suffix = bdi_integrate_url.split('/integration/')[1];

var params = url_suffix.split('&');
var ids_id = params[0];
//var ds1_id = decodeURI(params[1]);
//var ds2_id = decodeURI(params[2]);
//var ds1_name = decodeURI(params[3]);
//var ds2_name = decodeURI(params[4]);
//var align_iri = decodeURI(params[5]);
//var integrated_iri = decodeURI(params[6]);


/*paramsObject.ids_id = ids_id;
paramsObject.ds1_id = ds1_id;
paramsObject.ds2_id = ds2_id;
paramsObject.ds1_name = ds1_name;
paramsObject.ds2_name = ds2_name;
paramsObject.align_iri = align_iri;
paramsObject.integrated_iri = integrated_iri;*/

console.log(params);


function getIntegratedDataSourceInfo() {
    $.get('/bdiIntegratedDataSources/' + ids_id, function (data) {
        $("#overlay").fadeOut(100);
        console.log(data);
        integratedDataInfoObject = data;
        getAlignments(data.alignmentsIRI);
    });
}

function getAlignments(alignmentIRI) {
    $.get('/bdiAlignments/' + alignmentIRI, function (data) {
        //console.log(data);
        $("#overlay").fadeOut(100);
        var i = 1;
        data.forEach(function (val) {
            alignmentsData[i - 1] = val;
            var n = i - 1;
            //console.log(n);
            //console.log(val.s + " " + val.o + " " + val.p);
            //console.log(JSON.stringify(val));
            $('#alignments').find('tbody')
                .append($('<tr id="row' + n + '">')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(val.p)
                    ).append($('<td>')
                        .text(val.s)
                    ).append($('<td>')
                        .text(val.o)
                    )
                    .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + n + '">Accept</button> '))
                    .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                );

            ++i;
        });
    });
}

function acceptButtonClickHandler(acceptButton, i) {
    console.log("AcceptButtonClickHandler");
    alignmentsData[i].integrated_iri = integratedDataInfoObject.integratedIRI;
    console.log(alignmentsData[i]);
    $.ajax({
        type: 'POST',
        data: JSON.stringify(alignmentsData[i]),
        contentType: 'application/json',
        url: '/alignmentsAccept',
        success: function (response) {
            $("#overlay").fadeOut(100);
            console.log('Success');
            $("#row" + i).addClass("d-none");
        },
        error: function (response) {
            alert('failure' + JSON.stringify(response));
            console.log(JSON.stringify(response));
            $("#overlay").fadeOut(200);
        }
    });
}

$(function () {
    getIntegratedDataSourceInfo();
});

$(document).ready(function () {
    $(document).ajaxSend(function () {
        $("#overlay").fadeIn(100);
    });

    $(document).on('click', '#acceptAlignment', function () {
        var acceptButton = $("#acceptAlignment");
        //console.log(JSON.stringify($(this).val()));
        var index = $(this).val();
        acceptButtonClickHandler(acceptButton, index);
    });

    // Click handler for Finish Integration Button
    $("#integratedDataSourcesButton").on('click', function () {
        console.log("Inside integratedDataSourcesButton");
        console.log(integratedDataInfoObject);
        $.ajax({
            type: 'POST',
            data: JSON.stringify(integratedDataInfoObject),
            contentType: 'application/json',
            url: '/finishIntegration',
            success: function (response) {
                console.log('Success');
                window.location.href = '/';
            },
            error: function (response) {
                alert('failure' + JSON.stringify(response));
                console.log(JSON.stringify(response));
                $("#overlay").fadeOut(200);
            }
        });
    });
});


