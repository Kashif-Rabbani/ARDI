/**
 * Created by Kashif-Rabbani
 */
$(document).ready(function () {

    $(document).ajaxSend(function() {
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
            async: true,
            success: function (response) {
                console.log('success');
                console.log(JSON.stringify(response));
                //window.location.href = '/';
                $("#overlay").fadeOut(200);
            },
            error: function (response) {
                alert('failure' + JSON.stringify(response));
                console.log(JSON.stringify(response));
                $("#overlay").fadeOut(200);
            }
        });
        return false;
    };
    integrateButton.one('click', clickHandler);
}

