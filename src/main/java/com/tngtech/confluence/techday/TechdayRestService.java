package com.tngtech.confluence.techday;

import com.atlassian.confluence.cluster.ClusterManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.atlassian.spring.container.ContainerManager;
import com.tngtech.confluence.techday.data.Talk;
import com.tngtech.confluence.techday.data.VoteResponse;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;

@Path("/vote")
public class TechdayRestService {
    private static final Logger log = Logger.getLogger(TechdayRestService.class);
    private PageManager pageManager;
    private ContentPropertyManager contentPropertyManager;
    private UserAccessor userAccessor;
    private ClusterManager clusterManager;

    public TechdayRestService () {
        this.userAccessor = (UserAccessor) ContainerManager.getInstance().getContainerContext().getComponent("userAccessor");
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
    @Produces({MediaType.APPLICATION_JSON /*, MediaType.APPLICATION_XML*/})
    @Path("/{techdayMacroPageId}")
    public Response voteInterested(@PathParam("techdayMacroPageId") String techdayMacroPageId,
                         @QueryParam("interested") Boolean interested,
                         @QueryParam("talkId") String talkId,
                         @Context AuthenticationContext authenticationContext) {
        String user = getUser(authenticationContext);
        if (user == null) {
            log.error("Request from unauthenticated user");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Page page = pageManager.getPage((long)Integer.parseInt(techdayMacroPageId));
        TechDayService techDayService = new TechDayService(userAccessor, contentPropertyManager, clusterManager, page);
        Talk talk = techDayService.addTalk(talkId);
        techDayService.recordInterest(user, talkId, interested);

        String userFullNamesAsString = talk.getUserFullNamesAsString();
        return Response.ok( new VoteResponse(talkId, userFullNamesAsString, talk.getTotalAudience())).build();
    }

    private String getUser(AuthenticationContext context) {
        final Principal principal = context.getPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }
}
