package com.tngtech.confluence.techday;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.webwork.ServletActionContext;
import com.tngtech.confluence.techday.data.Talk;
import com.tngtech.confluence.techday.data.TalkType;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This class provides the simple functionality of the techday planning plugin.
 */
public class TechdayMacro extends BaseMacro {

    private static final Category log = Logger.getLogger(TechdayMacro.class);

    protected ContentPropertyManager contentPropertyManager;
    
    protected WikiStyleRenderer wikiStyleRenderer;
    private UserAccessor userAccessor;

    public boolean isInline() {
        return false;
    }

    public boolean hasBody() {
        return true;
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.NO_RENDER;
    }

    public TechdayMacro() {
        this.userAccessor = (UserAccessor) ContainerManager.getInstance().getContainerContext().getComponent("userAccessor");
    }

    /**
     * This method returns XHTML to be displayed on the final page.
     */
    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        ContentEntityObject contentObject = ((PageContext)renderContext).getEntity();
        List<Talk> talks = buildTalksFromBody(params, body, contentObject);

        HttpServletRequest request = ServletActionContext.getRequest();
        if (request != null) {
            recordUsage(talks, request, contentObject, (String)params.get("users"));
        }

        Collections.sort(talks, new Comparator<Talk>() {
            public int compare(Talk o1, Talk o2) {
                int audience2 = o2.getAudience().size();
                int audience1 = o1.getAudience().size();
                if (audience1 < audience2) return 1;
                if (audience1 == audience2) return 0;
                return -1;
            }
        });
        Collections.sort(talks, new Comparator<Talk>() {
            public int compare(Talk o1, Talk o2) {
                return o1.getType().compareTo(o2.getType());
            }
        });

        // now create a simple velocity context and render a template for the output
        Map contextMap = MacroUtils.defaultVelocityContext();
        contextMap.put("talks", talks);
        contextMap.put("content", contentObject);
        contextMap.put("wikiStyleRenderer", wikiStyleRenderer);

        try {
            return VelocityUtils.getRenderedTemplate("templates/extra/techday/techdaymacro.vm", contextMap);
        } catch (Exception e) {
            throw new MacroException(e);
        }
    }


    /**
     * This method parses the body of the macro.
     * It assumes that the format is:
     * <pre>idName|name|speaker|type|description|comment</pre>
     * Where type is the text version of the {@link TalkType} values.
     */
    private List<Talk> buildTalksFromBody(Map params, String body, ContentEntityObject contentObject) {

        List<Talk> talks = new ArrayList<Talk>();

        String idName;
        String name;
        String speaker;
        String type;
        String description;
        String comment;


        //Reconstruct all of the licenses that have been used until now
        for (StringTokenizer stringTokenizer = new StringTokenizer(body, "\r\n"); stringTokenizer.hasMoreTokens();) {
            String line = stringTokenizer.nextToken().trim();
            if (TextUtils.stringSet(line)) {
                StringTokenizer lineTokenizer = new StringTokenizer(line, "|");
                int numberOfTokens = lineTokenizer.countTokens();
                if (numberOfTokens == 6) {
                    idName = lineTokenizer.nextToken().trim();
                    name = lineTokenizer.nextToken().trim();
                    speaker = lineTokenizer.nextToken().trim();
                    type = lineTokenizer.nextToken().trim();
                    description = lineTokenizer.nextToken().trim();
                    comment = lineTokenizer.nextToken().trim();
                    Talk talk = new Talk(idName, name, speaker, description, comment, TalkType.valueOf(type), userAccessor);
                    String users = contentPropertyManager.getTextProperty(contentObject, buildPropertyString(idName));
                    if (users == null) users = "";
                    StringTokenizer userTokenizer = new StringTokenizer(users, ",");
                    while (userTokenizer.hasMoreTokens()) {
                        talk.addAudience(userTokenizer.nextToken().trim());
                    }
                    talks.add(talk);
                }
            }
        }
        return talks;
    }

    private String buildPropertyString(String idName) {
        return "techday." + idName;
    }

    private void recordUsage(List<Talk> talks, HttpServletRequest request, ContentEntityObject contentObject, String users) {
        String remoteUser = request.getRemoteUser();
        String requestTalk = request.getParameter("techday.idname");
        String requestUse = request.getParameter("techday.interested");

        for (Talk talk : talks) {
            if (talk.getIdName().equalsIgnoreCase(requestTalk)) {
                if ("yes".equalsIgnoreCase(requestUse)) {
                    talk.addAudience(remoteUser);
                } else {
                    talk.removeAudience(remoteUser);
                }
                String property = buildPropertyString(talk.getIdName());
                contentPropertyManager.setTextProperty(contentObject, property, talk.getUsersAsString());
            }
        }
    }

    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }

    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }
}