var integratedDataInfoObject = {};
var alignmentsData = {};

var bdi_integrate_url = window.location.href;
//var url_prefix = bdi_integrate_url.split('/integration/')[0];
var url_suffix = bdi_integrate_url.split('/integration/')[1];

var params = url_suffix.split('&');
var ds1_id = params[0];
var ds2_id = params[1];
var integrationType;

function integrationTypeChecker(){
    if(ds1_id.includes("INTEGRATED_") || ds2_id.includes("INTEGRATED_")){
        console.log("Integration of a local graph with global graph.");
        integrationType = "GLOBAL-vs-LOCAL";
    }
    else {
        console.log("Integration between local graphs");
        integrationType = "LOCAL-vs-LOCAL";
    }
}
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


/*function getIntegratedDataSourceInfo() {
    $("#overlay").fadeIn(100);
    $.get('/bdiIntegratedDataSources/' + ids_id, function (data) {
        //$("#overlay").fadeOut(100);
        console.log(data);
        integratedDataInfoObject = data;
        var dataSources = integratedDataInfoObject.dataSources;
        var constructedAlignmentsIRI;
        var allDataSources = {};
        dataSources.forEach(function (val) {
           console.log(val);
        });
        //getAlignments(data.alignmentsIRI);
    });
}*/

function getAlignments() {
    $("#overlay").fadeIn(100);
    $.get('/bdiAlignments/' + ds1_id + '&' + ds2_id, function (data) {
        console.log("Requesting bdiAlignments");
        //console.log(data);
        $("#overlay").fadeOut(100);
        var i = 1;
        data.forEach(function (val) {
            alignmentsData[i - 1] = val;
            var n = i - 1;
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
    alignmentsData[i].integrated_iri = params[0] + '-' + params[1];
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
    //getIntegratedDataSourceInfo();

});


$(document).ready(function () {
    /* $(document).ajaxSend(function () {
         $("#overlay").fadeIn(100);
     });*/

    getAlignments();
    integrationTypeChecker();

    $(document).on('click', '#acceptAlignment', function () {
        var acceptButton = $("#acceptAlignment");
        var index = $(this).val();
        acceptButtonClickHandler(acceptButton, index);
    });

    // Click handler for Finish Integration Button
    $("#integratedDataSourcesButton").on('click', function () {
        console.log("Inside integratedDataSourcesButton");
        var postData = {};
        postData.iri = params[0] + "-" + params[1];
        postData.integrationType = integrationType;
        postData.ds1_id = ds1_id;
        postData.ds2_id = ds2_id;
        console.log(postData);
        $.ajax({
            type: 'POST',
            data: JSON.stringify(postData),
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


