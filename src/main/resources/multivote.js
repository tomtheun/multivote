/*jslint  */
/*global jQuery: false, AJS: false */

(function ($) {
    "use strict";
    $(function () {
        var pageId = AJS.params.multivotePageId,
            interestedLink = $("input[ id ^= 'multivote']"),
            getInterestImage = function (interested) {
                if (interested) {
                    return AJS.params.interestedImage;
                } else {
                    return AJS.params.notInterestedImage;
                }
            };

        interestedLink.click(function () {
            var that = $(this),
                itemId = that.attr("id").replace(/[^0-9]/g, ""),
                interested = (that.attr("data-interest") === "true"),
                line = that.parent().parent().parent(),
                audience = line.find("td[ id ^= 'audience']"),
                tableId = line.parent().parent().attr("data-tableid"),
                contextPath = AJS.params.contextPath,
                url;

            if (typeof contextPath === undefined) {
                contextPath = "";
            }

            // TODO use property for url
            url = contextPath + "/rest/multivote/0.1/vote/record/" + pageId + "/" + tableId +
                "?" + $.param([
                {name:"itemId", value:itemId},
                {name:"interested", value:interested}
            ]);

            $.ajax({
                type:"POST", dataType:"json", url:url, data:"",
                timeout:10000,
                beforeSend:function () {
                    that.attr("src", AJS.params.progressImage);
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
    });
}(jQuery));
