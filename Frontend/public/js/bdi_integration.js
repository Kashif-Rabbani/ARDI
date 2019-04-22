console.log(window.location.href);
var bdi_integrate_url = window.location.href;
var url_prefix = bdi_integrate_url.split('/integration/')[0];
var url_suffix = bdi_integrate_url.split('/integration/')[1];

var params = url_suffix.split('&');
var ids_id = decodeURI(params[0]);
var ds1_id = decodeURI(params[1]);
var ds2_id = decodeURI(params[2]);
var ds1_name = decodeURI(params[3]);
var ds2_name = decodeURI(params[4]);
var align_iri = decodeURI(params[5]);
var integrated_iri = decodeURI(params[6]);
var alignmentsData = {};

console.log(params);

function getAlignments() {
    $.get('/bdiAlignments/' + align_iri, function (data) {
        //console.log(data);
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


});

function acceptButtonClickHandler(acceptButton, i) {
    console.log("Inside acceptButtonClickHandler");
    console.log("inside clickHandler");

    alignmentsData[i].integrated_iri = integrated_iri;

    $.ajax({
        type: 'POST',
        data: JSON.stringify(alignmentsData[i]),
        contentType: 'application/json',
        url: '/alignmentsAccept',
        success: function (response) {
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
    getAlignments();
});


