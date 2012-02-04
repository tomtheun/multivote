package com.tngtech.confluence.plugin;

import static jodd.lagarto.dom.jerry.Jerry.jerry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.jerry.Jerry;
import jodd.lagarto.dom.jerry.JerryFunction;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class MultivoteMacro extends BaseMacro {
    //private static final Category log = Logger.getLogger(MultiVoteMacro.class);
    private WikiStyleRenderer wikiStyleRenderer;
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
        ContentEntityObject page = ((PageContext)renderContext).getEntity();

        String tableId = (String)params.get("0");
        String shouldSort = (String)params.get("sort");
        checkValidityOf(tableId);
        recordVote(page, tableId);

        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

        String table = wikiStyleRenderer.convertWikiToXHtml(renderContext, body);

        List<VoteItem> items = buildItemsFromBody(page, tableId, table);
        sort(items, shouldSort);

        contextMap.put("items", items);
        contextMap.put("tableId", tableId);
        contextMap.put("headers", buildHeadersFromBody(table));
        contextMap.put("content", page);
        contextMap.put("multiVote", multiVote);

        try {
            return VelocityUtils.getRenderedTemplate("templates/extra/multivote.vm", contextMap);
        } catch (Exception e) {
            throw new MacroException(e);
        }
    }

    private void sort(List<VoteItem> items, String sort) {
        if ( "true".equals( sort ) ) {
            Collections.sort(items);
        }
    }

    private void checkValidityOf(String tableId) throws MacroException {
        if (tableId == null) {
            throw new MacroException("id is mandatory");
        } else if(!tableId.matches("^\\p{Alpha}\\p{Alnum}+$")) {
            throw new MacroException("id is only allowed to contain alphanumeric characters and has to start with a letter");
        }
    }

    private void recordVote(ContentEntityObject contentObject, String tableId) {
        HttpServletRequest request = ServletActionContext.getRequest();
        if (request != null) {
            String remoteUser = request.getRemoteUser();
            String requestItem = request.getParameter("multivote.idname");
            String requestUse = request.getParameter("multivote.interested");
            if (tableId.equals(request.getParameter("multivote.tableId"))) {
                multiVote.recordInterest(remoteUser, Boolean.parseBoolean(requestUse), new ItemKey(contentObject, tableId, requestItem));
            }
        }
    }

    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }

    /**
     * parse the table-header of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * </pre>
     * @param body of the Macro
     *
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    List<String> buildHeadersFromBody(String body) {
        List<String> header = new ArrayList<String>();

        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        for (Node node: lines.first().children().gt(0).get()) {
            header.add(node.getInnerHtml());
        }

        return header;
    }

    /**
     * parse the table-body of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * | idName | column_1 | ( column_n | )+
     * </pre>
     *
     * @param body of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    List<VoteItem> buildItemsFromBody(final ContentEntityObject page, final String tableId, String body) {
        final List<VoteItem> items = new ArrayList<VoteItem>();
        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        lines.gt(0).each(new JerryFunction() {
            private String innerHtml(Jerry it, int index) {
                return it.get(index).getInnerHtml().trim();
            }

            @Override
            public boolean onNode(Jerry me, int index) {
                Jerry children = me.children();
                List<String> fields = new ArrayList<String>();

                String itemId = children.get(0).getTextContent().trim();
                for (int i=1; i<children.length(); i++) {
                    fields.add(innerHtml(children, i));
                }

                VoteItem item = new VoteItem(itemId, fields, multiVote.retrieveAudience(new ItemKey(page, tableId, itemId)));
                items.add(item);
                return true;
            }
        });
        return items;
    }
}
