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

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.atlassian.spring.container.ContainerManager;
import com.tngtech.confluence.plugin.data.VoteItem;
import com.tngtech.confluence.plugin.data.VoteResponse;

@Path("/vote")
public class MultivoteRestService {
    private static final Logger log = Logger.getLogger(MultivoteRestService.class);
    private PageManager pageManager;
    private ContentPropertyManager contentPropertyManager;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;
    private PermissionManager permissionManager;

    public MultivoteRestService () {
        this.userAccessor = (UserAccessor) ContainerManager.getInstance().getContainerContext().getComponent("userAccessor");
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/record/{techdayMacroPageId}/{tableId}")
    public Response voteInterested(
			             @PathParam("techdayMacroPageId") String techdayMacroPageId,
			             @PathParam("tableId") String tableId,
                         @QueryParam("interested") Boolean interested,
                         @QueryParam("itemId") String itemId,
                         @Context AuthenticationContext authenticationContext) {
        String user = getUser(authenticationContext);
        Page page = pageManager.getPage((long)Integer.parseInt(techdayMacroPageId));

        if (user == null || !permissionManager.hasPermission(userAccessor.getUser(user), Permission.VIEW, page)) {
            log.error("Request from unauthenticated/unauthorized user");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        MultiVote multiVote = new MultiVote(tableId, userAccessor, contentPropertyManager, clusterManager, page);
        VoteItem item = multiVote.retrieveItem(itemId);
        multiVote.recordInterest(user, itemId, interested);

        String userFullNamesAsString = item.getUserFullNamesAsString();
        return Response.ok(new VoteResponse(itemId, userFullNamesAsString, item.getTotalAudience())).build();
    }

    private String getUser(AuthenticationContext context) {
        final Principal principal = context.getPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }
}
