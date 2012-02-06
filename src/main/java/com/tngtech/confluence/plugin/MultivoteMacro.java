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
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class MultivoteMacro extends BaseMacro {
    private static final String TEMPLATE = "templates/extra/multivote.vm";
    //private static final Category log = Logger.getLogger(MultiVoteMacro.class);
    
    /*
     * config
     */
    public boolean isInline() {
        return false;
    }

    public boolean hasBody() {
        return true;
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.ALL;
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

        try {
	        recordVote(page, tableId);
	        return render(page, body, tableId, shouldSort);
        } catch (Exception e) {
            throw new MacroException(e);
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

    private String render(ContentEntityObject page, String body, String tableId, String shouldSort) {
        List<VoteItem> items = buildItemsFromBody(page, tableId, body);
        sortIf(shouldSort, items);
        List<String> headers = buildHeadersFromBody(body);
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

        contextMap.put("items", items);
        contextMap.put("tableId", tableId);
        contextMap.put("headers", headers);
        contextMap.put("content", page);
        contextMap.put("multiVote", multiVote);
        return VelocityUtils.getRenderedTemplate(TEMPLATE, contextMap);
    }

    private void sortIf(String shouldSort, List<VoteItem> items) {
        if ( "true".equals( shouldSort ) ) {
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
            @Override
            public boolean onNode(Jerry me, int index) {
                Jerry children = me.children();
                final List<String> fields = new ArrayList<String>();

                String itemId = children.get(0).getTextContent().trim();
                
		        for (Node node: children.gt(0).get()) {
		            fields.add(node.getInnerHtml().trim());
		        }
		        
                VoteItem item = new VoteItem(itemId, fields, multiVote.retrieveAudience(new ItemKey(page, tableId, itemId)));
                items.add(item);
                return true;
            }
        });
        return items;
    }
    
    /*
     * injected Services
     */
    private MultiVoteService multiVote;
    public void setMultiVote(MultiVoteService multiVote) {
        this.multiVote = multiVote;
    }
}
