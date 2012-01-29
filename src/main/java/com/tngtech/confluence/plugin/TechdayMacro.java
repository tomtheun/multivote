package com.tngtech.confluence.plugin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openqa.jetty.html.Table;

import com.atlassian.confluence.cluster.ClusterManager;
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
import com.opensymphony.webwork.ServletActionContext;

/**
 * This class provides the simple functionality of the techday planning plugin.
 */
public class TechdayMacro extends BaseMacro {
    //private static final Category log = Logger.getLogger(TechdayMacro.class);
    protected ContentPropertyManager contentPropertyManager;

    protected WikiStyleRenderer wikiStyleRenderer;
    private UserAccessor userAccessor;

    private ClusterManager clusterManager;

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

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
    @SuppressWarnings("rawtypes")
    @Override
    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        ContentEntityObject contentObject = ((PageContext)renderContext).getEntity();

        String tableId = (String)params.get("0");
        if (tableId == null) {
            throw new MacroException("id is mandatory");
        } else if(!tableId.matches("^\\p{Alpha}\\p{Alnum}+$")) {
            throw new MacroException("id is only allowed to contain alphanumeric characters and has to start with a letter");
        }

        String table = wikiStyleRenderer.convertWikiToXHtml(renderContext, body);
        TechDayService techDayService = new TechDayService(table, tableId,  userAccessor, contentPropertyManager, contentObject, clusterManager);

        HttpServletRequest request = ServletActionContext.getRequest();
        if (request != null) {
            String remoteUser = request.getRemoteUser();
            String requestTalk = request.getParameter("techday.idname");
            String requestUse = request.getParameter("techday.interested");
            techDayService.recordInterest(remoteUser, requestTalk, Boolean.parseBoolean(requestUse));
        }

        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

        contextMap.put("tableId", tableId);
        contextMap.put("headers", techDayService.getHeader());
        contextMap.put("talks", techDayService.getTalks());
        contextMap.put("content", contentObject);
        contextMap.put("wikiStyleRenderer", wikiStyleRenderer);

        try {
            return VelocityUtils.getRenderedTemplate("templates/extra/techday/techdaymacro.vm", contextMap);
        } catch (Exception e) {
            throw new MacroException(e);
        }
    }

    public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
        this.contentPropertyManager = contentPropertyManager;
    }

    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }
}
