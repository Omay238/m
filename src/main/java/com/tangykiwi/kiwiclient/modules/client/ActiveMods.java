package com.tangykiwi.kiwiclient.modules.client;

import com.google.common.eventbus.Subscribe;
import com.tangykiwi.kiwiclient.KiwiClient;
import com.tangykiwi.kiwiclient.event.DrawOverlayEvent;
import com.tangykiwi.kiwiclient.modules.Category;
import com.tangykiwi.kiwiclient.modules.Module;
import com.tangykiwi.kiwiclient.modules.ModuleManager;
import com.tangykiwi.kiwiclient.util.render.color.ColorUtil;
import com.tangykiwi.kiwiclient.util.font.GlyphPageFontRenderer;
import com.tangykiwi.kiwiclient.util.font.IFont;
import net.minecraft.client.gui.DrawableHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActiveMods extends Module {

    public ArrayList<Module> currModules = new ArrayList<>();
    Map<Module, Integer> enablingMods = new HashMap<>();
    Map<Module, Integer> disablingMods = new HashMap<>();
    boolean firstDraw = true;

    public ActiveMods() {
        super("ActiveMods", "Display toggled modules", KEY_UNBOUND, Category.CLIENT);
        super.toggle();
    }

    @Subscribe
    public void onDrawOverlay(DrawOverlayEvent e) {
        if(!mc.options.debugEnabled) {
            GlyphPageFontRenderer textRenderer = IFont.CONSOLAS;

            //TextRenderer textRenderer = mc.textRenderer;

//            DrawableHelper.fill(e.getMatrix(), 0, 60, textRenderer.getWidth(enabledMods.get(0).getName()) + 5, 62, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
//            for (Module m : enabledMods) {
//
//                int offset = count * (textRenderer.fontHeight + 1);
//
//                DrawableHelper.fill(e.getMatrix(), textRenderer.getWidth(m.getName()) + 3, 62 + offset, textRenderer.getWidth(m.getName()) + 5, 63 + textRenderer.fontHeight + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
//                DrawableHelper.fill(e.getMatrix(), 0, 62 + offset, textRenderer.getWidth(m.getName()) + 3, 63 + textRenderer.fontHeight + offset, 0x90000000);
//                textRenderer.draw(e.getMatrix(), m.getName(), 2, 63 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
//
//                count++;
//            }

            int count = 0;
            ArrayList<Module> enabledMods = KiwiClient.moduleManager.getEnabledMods();

            if(!firstDraw && enabledMods.size() > currModules.size()) {
                ArrayList<Module> difference = new ArrayList<>(enabledMods);
                difference.removeAll(currModules);

                for(Module m : difference) {
                    if(disablingMods.containsKey(m)) {
                        enablingMods.put(m, disablingMods.get(m));
                        disablingMods.remove(m);
                    }
                    else {
                        enablingMods.put(m, -textRenderer.getStringWidth(m.getName()));
                    }
                }
            }

            if(enabledMods.size() < currModules.size()) {
                ArrayList<Module> difference = new ArrayList<>(currModules);
                difference.removeAll(enabledMods);

                for(Module m : difference) {
                    if(enablingMods.containsKey(m)) {
                        disablingMods.put(m, enablingMods.get(m));
                        enablingMods.remove(m);
                    }
                    else {
                        disablingMods.put(m, 0);
                    }
                }
            }

            ArrayList<Module> display = KiwiClient.moduleManager.getEnabledMods();
            for (Module m : disablingMods.keySet()) {
                display.add(m);
            }
            display.sort(new ModuleManager.ModuleComparator());

            Module firstMod = display.get(0);
            if(!firstDraw && enablingMods.containsKey(firstMod)) {
                int displace = enablingMods.get(firstMod);
                DrawableHelper.fill(e.getMatrix(), displace, 60, textRenderer.getStringWidth(firstMod.getName()) + 5 + displace, 62, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
            }
            else if(disablingMods.containsKey(firstMod)) {
                int displace = disablingMods.get(firstMod);
                DrawableHelper.fill(e.getMatrix(), displace, 60, textRenderer.getStringWidth(firstMod.getName()) + 5 + displace, 62, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
            }
            else {
                DrawableHelper.fill(e.getMatrix(), 0, 60, textRenderer.getStringWidth(firstMod.getName()) + 5, 62, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
            }

            for (Module m : display) {

                int offset = count * 8;

                if(!firstDraw && enablingMods.containsKey(m)) {
                    int displace = enablingMods.get(m);
                    DrawableHelper.fill(e.getMatrix(), textRenderer.getStringWidth(m.getName()) + 3 + displace, 62 + offset, textRenderer.getStringWidth(m.getName()) + 5 + displace, 62 + 8 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
                    DrawableHelper.fill(e.getMatrix(), displace, 62 + offset, textRenderer.getStringWidth(m.getName()) + 3 + displace, 62 + 8 + offset, 0x90000000);
                    textRenderer.drawString(e.getMatrix(), m.getName(), 0.2 + displace, 62.2 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150), 1);
                    if(displace + 1 >= 0) {
                        enablingMods.remove(m);
                    }
                    else {
                        enablingMods.replace(m, enablingMods.get(m) + 1);
                    }
                }
                else if(disablingMods.containsKey(m)) {
                    int displace = disablingMods.get(m);
                    DrawableHelper.fill(e.getMatrix(), textRenderer.getStringWidth(m.getName()) + 3 + displace, 62 + offset, textRenderer.getStringWidth(m.getName()) + 5 + displace, 62 + 8 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
                    DrawableHelper.fill(e.getMatrix(), displace, 62 + offset, textRenderer.getStringWidth(m.getName()) + 3 + displace, 62 + 8 + offset, 0x90000000);
                    textRenderer.drawString(e.getMatrix(), m.getName(), 0.2 + displace, 62.2 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150), 1);
                    if(displace - 2 <= -textRenderer.getStringWidth(m.getName())) {
                        disablingMods.remove(m);
                    }
                    else {
                        disablingMods.replace(m, disablingMods.get(m) - 2);
                    }
                }
                else{
                    DrawableHelper.fill(e.getMatrix(), textRenderer.getStringWidth(m.getName()) + 3, 62 + offset, textRenderer.getStringWidth(m.getName()) + 5, 62 + 8 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150));
                    DrawableHelper.fill(e.getMatrix(), 0, 62 + offset, textRenderer.getStringWidth(m.getName()) + 3, 62 + 8 + offset, 0x90000000);
                    textRenderer.drawString(e.getMatrix(), m.getName(), 0.2, 62.2 + offset, ColorUtil.getRainbow(4, 0.8f, 1, count * 150), 1);
                }

                count++;
            }

            currModules = enabledMods;
        }
        firstDraw = false;
    }
}
