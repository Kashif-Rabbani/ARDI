$(document).ready(function () {


});

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

console.log(params);

function getAlignments() {
    $.get('/bdiAlignments/' + align_iri, function (data) {
        console.log(data);
        var i = 1;
        data.forEach(function (val) {
           console.log(val.s + " " + val.o + " " + val.p) ;


            $('#alignments').find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(i)
                    ).append($('<td>')
                        .text(val.p)
                    ).append($('<td>')
                        .text(val.s)
                    ).append($('<td>')
                        .text(val.o)
                    )
                    .append($('<td>').append('<input type="button" class="btn btn-success" name="accept" value = "Accept" />'))
                    .append($('<td>').append('<input type="button" class="btn btn-danger" name="reject" value = "Reject" />'))
                );

           ++i;
        });
    });
}

$(function () {
    getAlignments();
});
