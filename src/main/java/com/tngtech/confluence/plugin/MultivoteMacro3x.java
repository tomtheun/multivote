package com.tngtech.confluence.plugin;

import java.util.Map;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;

public class MultivoteMacro3x extends BaseMacro {
    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean hasBody() {
        return true;
    }

    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.ALL;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        try {
            return macroService.execute(params, body, renderContext);
        } catch (Exception e) {
            throw new MacroException(e);
        }
    }

    MultiVoteMacroService macroService;
    public void setMultiVoteMacroService(MultiVoteMacroService macroService) {
        this.macroService = macroService;
    }
}
