var integratedDataInfoObject = {};
var alignmentsData = {};

var bdi_integrate_url = window.location.href;
//var url_prefix = bdi_integrate_url.split('/integration/')[0];
var url_suffix = bdi_integrate_url.split('/integration/')[1];

var params = url_suffix.split('&');
// console.log(params);
var ds1_id = params[0];
var ds2_id = params[1];
var integrationType;

function integrationTypeChecker() {

    if (ds1_id.includes("INTEGRATED-") && ds2_id.includes("INTEGRATED-")) {
        console.log("Integration of a global graph vs global graph.");
        integrationType = "GLOBAL-vs-GLOBAL";
    } else if (ds1_id.includes("INTEGRATED-")) {
        console.log("Integration of a Global graph vs Local graph.");
        integrationType = "GLOBAL-vs-LOCAL";
    } else {
        console.log("Integration between local graphs");
        integrationType = "LOCAL-vs-LOCAL";
    }
}

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
    console.log("Requesting bdiAlignments");
    $.get('/bdiAlignments/' + ds1_id + '&' + ds2_id, function (data) {
        //console.log(data);
        $("#overlay").fadeOut(100);
        var i = 1;
        data.forEach(function (val) {
            alignmentsData[i - 1] = val;
            var n = i - 1;
            if (val.mapping_type === 'DATA-PROPERTY') {
                $('#alignments').find('#alignmentsBody')
                    .append($('<tr id="row' + n + '">')
                        /*.append($('<td>').text(i))*/
                        .append($('<td>')
                            .text(val.s)
                        ).append($('<td>')
                            .text(val.p)
                        ).append($('<td>').text(Math.round(val.confidence * 100) / 100)
                        )
                        .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + n + '">Accept</button> '))
                        .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                    );
            }

            if (val.mapping_type === 'OBJECT-PROPERTY') {
                $('#alignmentsObjProp').find('#alignmentsBodyObjectProperties')
                    .append($('<tr id="row' + n + '">')
                        // .append($('<td>').text(i))
                        .append($('<td>')
                            .text(val.s)
                        ).append($('<td>')
                            .text(val.p)
                        ).append($('<td>').text(Math.round(val.confidence * 100) / 100)
                        )
                        .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + n + '">Accept</button> '))
                        .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                    );
            }


            if (val.mapping_type === 'CLASS') {
                $('#alignmentsClass').find('#alignmentsBodyClasses')
                    .append($('<tr id="row' + n + '">')
                        // .append($('<td>').text(i))
                        .append($('<td>')
                            .text(val.s)
                        ).append($('<td>')
                            .text(val.p)
                        ).append($('<td>').text(Math.round(val.confidence * 100) / 100)
                        )
                        .append($('<td>').append('<button type="button" id ="acceptAlignment" class="btn btn-success" value="' + n + '">Accept</button> '))
                        .append($('<td>').append('<button type="button" id ="rejectAlignment" class="btn btn-danger">Reject</button> '))
                    );
            }
            ++i;
        });
    });
    console.log("Alignments Data");
    console.log(alignmentsData);
}

function acceptButtonClickHandler(acceptButton, i) {
    console.log("AcceptButtonClickHandler");
    alignmentsData[i].integrated_iri = params[0] + '-' + params[1];
    alignmentsData[i].ds1_id = params[0];
    alignmentsData[i].ds2_id = params[1];
    alignmentsData[i].actionType = "ACCEPTED";

    console.log(alignmentsData[i]);
    $.ajax({
        type: 'POST',
        data: JSON.stringify(alignmentsData[i]),
        contentType: 'application/json',
        url: '/alignmentsAccept',
        success: function (response) {
            $("#overlay").fadeOut(100);
            console.log(response);
            if(response === "AlignmentSucceeded"){
                $("#row" + i).addClass("d-none");
            }
        },
        error: function (response) {
            alert('failure' + JSON.stringify(response));
            console.log(JSON.stringify(response));
            $("#overlay").fadeOut(200);
        }
    });
}

$(document).ready(function () {
    /* window.addEventListener('beforeunload', (event) => {
            // Cancel the event as stated by the standard.
            console.log("beforeunload");
            event.preventDefault();
            // Chrome requires returnValue to be set.
            event.returnValue = "Please click 'Stay on this Page' and we will give you candy";
        });*/
    getAlignments();
    integrationTypeChecker();

    new Tablesort(document.getElementById('alignments'));

    $(document).on('click', '#acceptAlignment', function () {
        var acceptButton = $("#acceptAlignment");
        var index = $(this).val();
        acceptButtonClickHandler(acceptButton, index);
    });

    // Click handler for Finish Integration Button
    $("#integratedDataSourcesButton").on('click', function () {
        console.log("IntegratedDataSourcesButton Clicked");
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
                //window.location.href = '/';
            },
            error: function (response) {
                alert('failure' + JSON.stringify(response));
                console.log(JSON.stringify(response));
                $("#overlay").fadeOut(200);
            }
        });
    });
});