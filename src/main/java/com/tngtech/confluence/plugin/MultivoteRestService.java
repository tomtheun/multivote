package com.tngtech.confluence.plugin;

import java.security.Principal;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;
import com.tngtech.confluence.plugin.data.VoteResponse;

@Path("/vote")
public class MultivoteRestService {
    private static final Logger log = Logger.getLogger(MultivoteRestService.class);

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/record/{pageId}/{tableId}")
    public Response voteInterested(
                         @PathParam("pageId") String pageId,
                         @PathParam("tableId") String tableId,
                         @QueryParam("interested") Boolean interested,
                         @QueryParam("itemId") String itemId,
                         @Context AuthenticationContext authenticationContext) {
        String user = getUser(authenticationContext);
        AbstractPage abstractPage = pageManager.getAbstractPage((long) Integer.parseInt(pageId));

        if (userNotPermitted(user, abstractPage)) {
            log.error("Request from unauthenticated/unauthorized user");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        ItemKey itemKey = new ItemKey(abstractPage, tableId, itemId);

        VoteItem item = multiVote.recordInterest(user, interested, itemKey);
        String userFullNamesAsString = multiVote.getUserFullNamesAsString(item.getAudience());

        return Response.ok(new VoteResponse(itemId, userFullNamesAsString, item.getAudienceCount())).build();
    }

    private boolean userNotPermitted(String user, AbstractPage abstractPage) {
        return user == null || !permissionManager.hasPermission(userAccessor.getUser(user), Permission.VIEW, abstractPage);
    }

    private String getUser(AuthenticationContext context) {
        final Principal principal = context.getPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }

    /*
     * injected Services
     */
    private PageManager pageManager;
    private UserAccessor userAccessor;
    private PermissionManager permissionManager;
    private MultiVoteService multiVote;

    public void setMultiVote(MultiVoteService multiVote) {
        this.multiVote = multiVote;
    }

    public void setUserAccessor(UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }
}
