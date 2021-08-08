package com.tangykiwi.kiwiclient.modules;

import com.google.common.eventbus.Subscribe;
import com.tangykiwi.kiwiclient.command.Command;
import com.tangykiwi.kiwiclient.event.KeyPressEvent;
import com.tangykiwi.kiwiclient.modules.client.ActiveMods;
import com.tangykiwi.kiwiclient.modules.client.ClickGui;
import com.tangykiwi.kiwiclient.modules.client.HUD;
import com.tangykiwi.kiwiclient.modules.client.Tooltips;
import com.tangykiwi.kiwiclient.modules.combat.AutoClicker;
import com.tangykiwi.kiwiclient.modules.combat.Criticals;
import com.tangykiwi.kiwiclient.modules.combat.TriggerBot;
import com.tangykiwi.kiwiclient.modules.movement.*;
import com.tangykiwi.kiwiclient.modules.player.*;
import com.tangykiwi.kiwiclient.modules.render.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ModuleManager {

    public static ArrayList<Module> moduleList = new ArrayList<Module>();
    public static MinecraftClient mc = MinecraftClient.getInstance();

    public void init() {
        //client
        moduleList.add(new ActiveMods());
        moduleList.add(new ClickGui());
        moduleList.add(new HUD());
        moduleList.add(new Tooltips());

        //combat
        moduleList.add(new AutoClicker());
        moduleList.add(new Criticals());
        moduleList.add(new TriggerBot());

        //movement
        moduleList.add(new FastBridge());
        moduleList.add(new Fly());
        moduleList.add(new NoClip());
        moduleList.add(new NoFall());
        moduleList.add(new SafeWalk());
        moduleList.add(new Speed());

        //player
        moduleList.add(new ArmorSwap());
        moduleList.add(new Cape());
        moduleList.add(new Deadmau5Ears());
        moduleList.add(new InventoryViewer());

        //render
        moduleList.add(new FullBright());
        moduleList.add(new ItemPhysics());
        moduleList.add(new NoPortal());
        moduleList.add(new XRay());
        moduleList.add(new Zoom());
    }

    public ArrayList<Module> getEnabledMods() {
        ArrayList<Module> enabledMods = new ArrayList<Module>();

        for(Module m : moduleList) {
            if(m.isEnabled()) enabledMods.add(m);
        }

        enabledMods.add(getModule(ClickGui.class));

        Collections.sort(enabledMods, new ModuleComparator());
        return enabledMods;
    }

    public static Module getModule(Class<? extends Module> c) {
        for (Module m : moduleList) {
            if (m.getClass().equals(c)) {
                return m;
            }
        }

        return null;
    }

    public Module getModuleByName(String name) {
        for (Module m : moduleList) {
            if (name.equalsIgnoreCase(m.getName()))
                return m;
        }
        return null;
    }

    public static ArrayList<Module> getModulesInCat(Category cat) {
        ArrayList<Module> modulesInCat = new ArrayList<Module>();
        for(Module m : moduleList) {
            if(m.getCategory().equals(cat)) modulesInCat.add(m);
        }
        return modulesInCat;
    }

    public static class ModuleComparator implements Comparator<Module> {
        @Override
        public int compare(Module a, Module b) {
            if(mc.textRenderer.getWidth(a.getName()) >
                    mc.textRenderer.getWidth(b.getName()))
                return -1;
            else if(mc.textRenderer.getWidth(a.getName()) <
                    mc.textRenderer.getWidth(b.getName()))
                return 1;
            return 0;
        }
    }

    @Subscribe
    public static void handleKeyPress(KeyPressEvent e) {
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_F3)) return;

        if(InputUtil.isKeyPressed(mc.getWindow().getHandle(), Command.KEY)) {
            mc.openScreen(new ChatScreen(""));
            return;
        }

        for(Module m : moduleList) {
            if(m.getKeyCode() == e.getKeyCode()) m.toggle();
        }
    }

    /**
    @Subscribe
    public static void handleMouseButton(MouseButtonEvent e) {
        for(Module m : moduleList) {
            if(m.getKeyCode() == e.getKey()) m.toggle();
        }
    } */
}
