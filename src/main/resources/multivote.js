/*jslint  */
/*global $: false, AJS: false */

(function () {
    "use strict";

    function getParameters() {
        var params;
        if (AJS.params) {
            return AJS.params;
        }

        params = {};
        $('.parameters, .hidden').find('input').each(function(index, input) {
            input = $(input);
            params[input.attr('id')] = input.attr('value');
        });
        params.contextPath = $('meta[name="confluence-context-path"]').attr('content');
        return params;
    }

    function init() {
        var params = getParameters(),
            pageId = params.multivotePageId,
            interestedLink = $("input[ id ^= 'multivote']"),
            getInterestImage = function (interested) {
                if (interested) {
                    return params.interestedImage;
                }
                return params.notInterestedImage;
            };

        interestedLink.click(function () {
            var that = $(this),
                itemId = that.attr("id").replace(/^multivote\./, ""),
                interested = (that.attr("data-interest") === "true"),
                line = that.parent().parent().parent(),
                audience = line.find("td[ id ^= 'audience']"),
                tableId = line.parent().parent().attr("data-tableid"),
                contextPath = params.contextPath,
                url;

            if (contextPath === undefined) {
                contextPath = "";
            }

            // TODO use property for url
            url = contextPath + "/rest/multivote/0.1/vote/record/" + pageId + "/" + tableId +
                "?" + $.param([
                { name: "itemId", value: itemId },
                { name: "interested", value: interested }
            ]);

            $.ajax({
                type:"POST", dataType:"json", url:url, data:"",
                timeout:10000,
                beforeSend:function () {
                    that.attr("src", params.progressImage);
                },
                error:function () {
                    that.attr("src", getInterestImage(!interested));
                },
                success:function (data) {
                    that.attr("data-interest", !interested);
                    that.attr("src", getInterestImage(interested));
                    audience.text(data.userNo);
                    audience.attr("title", data.users);
                    if (interested) {
                        line.attr("class", "interested");
                    } else {
                        line.attr("class", "notInterested");
                    }
                }
            });

            return false;
        });
    }

    if (window.ConfluenceMobile) {
        window.ConfluenceMobile.contentEventAggregator.on("displayed", init);
    } else {
        AJS.toInit(init);
    }
}());
