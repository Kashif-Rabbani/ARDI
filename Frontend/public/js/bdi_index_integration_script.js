/**
 * Created by Kashif-Rabbani
 */
$(document).ready(function () {

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
        if (integrateButton.hasClass('disabled')) {
        } else {
            integrationButtonClickHandler(integrateButton);
        }
    });
});

function integrationButtonClickHandler(integrateButton) {
    var clickHandler = function (e) {
        e.preventDefault();
        e.stopImmediatePropagation();

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
                console.log('success');
                console.log(JSON.stringify(response));
                //window.location.href = '/';
            },
            error: function (response) {
                alert('failure' + response.toString());
                console.log(JSON.stringify(response));
            }
        });
        return false;
    };
    integrateButton.one('click', clickHandler);
}

