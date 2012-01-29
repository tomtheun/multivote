/*jslint  */
/*global jQuery: false, AJS: false */

(function ($) {
    "use strict";
    $(function () {
        var pageId = AJS.params.multivotePageId,
            interestedLink = $("a[ id ^= 'multivote']"),
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
                audience = that.parent().parent().find("td[ id ^= 'audience']"),
                tableId = that.parent().parent().parent().parent().get(0).id,
                contextPath = AJS.params.contextPath,
                img = that.find("img"),
                url;

            if (typeof(contextPath) === undefined) {
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
                    img.attr("src", AJS.params.progressImage);
                },
                error:function () {
                    img.attr("src", getInterestImage(!interested));
                },
                success:function (data) {
                    that.attr("data-interest", !interested);
                    img.attr("src", getInterestImage(interested));
                    audience.text(data.userNo);
                    audience.attr("title", data.users);
                    if (interested) {
                        that.parent().parent().attr("class", "interested");
                    } else {
                        that.parent().parent().attr("class", "notInterested");
                    }
                }
            });

            return false;
        });
    });
}(jQuery));
