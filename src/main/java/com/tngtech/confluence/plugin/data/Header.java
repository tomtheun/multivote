package com.tngtech.confluence.plugin.data;

import java.util.List;

import com.atlassian.confluence.velocity.htmlsafe.HtmlSafe;

/**
 * this wrapper class around the List of header columns
 * has the sole purpose to be able to add an HtmlSafe Annotation
 */
public class Header {
    private List<String> columns;

    public Header(List<String> columns) {
        this.columns = columns;
    }

    @HtmlSafe
    public List<String> getColumns() {
        return columns;
    }
}
