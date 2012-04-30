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
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import com.tngtech.confluence.plugin.data.Header;
import com.tngtech.confluence.plugin.data.ItemKey;
import com.tngtech.confluence.plugin.data.VoteItem;

public class MultiVoteMacroService {
    private static final String VALID_ID_PATTERN = "^\\p{Alpha}\\p{Alnum}+$";
    private static final String TEMPLATE = "templates/extra/multivote.vm";

    @SuppressWarnings("rawtypes")
    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        ContentEntityObject page = ((PageContext)renderContext).getEntity();
        String tableId = (String)params.get("0");
        if (tableId == null) {
            tableId = (String)params.get("id");
        }
        String shouldSort = (String)params.get("sort");
        checkValidityOf(tableId);

        recordVote(page, tableId);
        return render(page, body, tableId, shouldSort);
    }

    /**
     * parse the table-body of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * | idName | column_1 | ( column_n | )+
     * </pre>
     *
     * @param page the Macro is on
     * @param tableId id of the table the Macro will generate
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
                checkItemId(itemId);

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

    private static void checkItemId(String itemId) {
        if (! itemId.matches("^\\p{Alnum}+$")) {
            throw new MultivoteMacroException("id is only allowed to contain alphanumeric characters, but was '" + itemId + '"');
        }
    }

    /**
     * parse the table-header of the macro. It assumes that the format is:
     * <pre>
     * |  ID    | header_1 | ( header_n | )+
     * </pre>
     * @param body of the Macro
     * @return list of {@link com.tngtech.confluence.plugin.data.VoteItem}
     */
    Header buildHeaderFromBody(String body) {
        final List<String> columns = new ArrayList<String>();

        final Jerry xhtml = jerry(body);
        final Jerry lines = xhtml.$("table").find("tr");

        for (Node node: lines.first().children().gt(0).get()) {
            columns.add(node.getInnerHtml().trim());
        }

        return new Header(columns);
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
        Header header = buildHeaderFromBody(body);
        Map<String, Object> contextMap = MacroUtils.defaultVelocityContext();

        contextMap.put("items", items);
        contextMap.put("tableId", tableId);
        contextMap.put("header", header);
        contextMap.put("content", page);
        contextMap.put("multiVote", multiVote);
        return VelocityUtils.getRenderedTemplate(TEMPLATE, contextMap);
    }

    private void sortIf(String shouldSort, List<VoteItem> items) {
        if ("true".equals(shouldSort)) {
            Collections.sort(items);
        }
    }

    private void checkValidityOf(String tableId) throws MacroException {
        if (tableId == null) {
            throw new MacroException("id is mandatory");
        } else if(!tableId.matches(VALID_ID_PATTERN)) {
            throw new MacroException("id is only allowed to contain alphanumeric characters and has to start with a letter, but was '" + tableId + "'");
        }
    }

    /*
     * injected Services
     */
    MultiVoteService multiVote;
    public void setMultiVoteService(MultiVoteService multiVote) {
        this.multiVote = multiVote;
    }
}
