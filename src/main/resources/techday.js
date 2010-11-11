;(function($) {

    $(function () {
        var id = AJS.params.TechdayPluginPageId;

        var interestedLink =  $("a[ id ^= 'techday']");
        interestedLink.click( function(event){
            var that = $(this);
            var talkId = that.attr("id").replace(/[^0-9]/g,"");
            var interested = (that.attr("data-interest") === "true");
            var audience = that.parent().parent().find("td[ id ^= 'audience']");
            var interestImage = getInterestImage(interested);
            var contextPath = AJS.params.contextPath;

            // TODO use property for url
            if (typeof(contextPath) == undefined) {
                contextPath = "";
            }
            var url = contextPath + "/rest/techday/0.1/vote/" + id
                 + "?"
                 + $.param([{name: "talkId", value: talkId},
                     {name: "interested", value: interested}]);

            $.ajax({type: "POST", dataType: "json", url: url, data: "",
            success: function(data) {
                that.attr("data-interest", !interested);
                var img = that.find("img")
                img.attr("src", interestImage)
                audience.text(data.userNo);
                audience.attr("title", data.users);
                if (interested) {
                    that.parent().parent().attr("class", "interested")
                } else {
                    that.parent().parent().attr("class", "notInterested")
                }
            }});

            return false;
        });

        var getInterestImage = function(interested) {
        	if (interested) {
        		return AJS.params.interestedImage;
            } else {
            	return AJS.params.notInterestedImage;
            }
        }
    })

})(jQuery);
