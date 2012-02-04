package com.tngtech.confluence.plugin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;

public class MultivoteMacro extends BaseMacro {
    //private static final Category log = Logger.getLogger(MultiVoteMacro.class);
    protected ContentPropertyManager contentPropertyManager;

    protected WikiStyleRenderer wikiStyleRenderer;
    private MultiVote multiVote;

    public void setMultiVote(MultiVote multiVote) {
        this.multiVote = multiVote;
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

        HttpServletRequest request = ServletActionContext.getRequest();
        if (request != null) {
            String remoteUser = request.getRemoteUser();
            String requestItem = request.getParameter("multivote.idname");
            String requestUse = request.getParameter("multivote.interested");
            if (tableId.equals(request.getParameter("multivote.tableId"))) {
	            multiVote.recordInterest(remoteUser, Boolean.parseBoolean(requestUse), contentObject, tableId, requestItem);
            }
        }

        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

        String table = wikiStyleRenderer.convertWikiToXHtml(renderContext, body);
        contextMap.put("tableId", tableId);
        contextMap.put("headers", multiVote.buildHeadersFromBody(tableId, table));
        contextMap.put("items", multiVote.buildItemsFromBody(contentObject, tableId, table));
        contextMap.put("content", contentObject);
        contextMap.put("wikiStyleRenderer", wikiStyleRenderer);
        contextMap.put("multiVote", multiVote);

        try {
            return VelocityUtils.getRenderedTemplate("templates/extra/multivote.vm", contextMap);
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
