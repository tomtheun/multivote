package com.tngtech.confluence.plugin;


import java.util.Map;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;

public class MultivoteMacro implements Macro {
    /*
     * config
     */
    @Override
    public BodyType getBodyType() {
        return BodyType.RICH_TEXT;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.BLOCK;
    }

    @Override
    @RequiresFormat(value = Format.View)
    public String execute(Map<String, String> parameters, String body, ConversionContext context)
            throws MacroExecutionException {
        try
        {
            return macroService.execute(parameters, body, context.getPageContext());
        }
        catch (Exception e)
        {
            throw new MacroExecutionException(e);
        }
    }

    MultiVoteMacroService macroService;
    public void setMultiVoteMacroService(MultiVoteMacroService macroService) {
        this.macroService = macroService;
    }
}
