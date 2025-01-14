package com.tangykiwi.kiwiclient.gui;

import com.google.common.io.Resources;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tangykiwi.kiwiclient.gui.mainmenu.dummy.DummyClientPlayNetworkHandler;
import com.tangykiwi.kiwiclient.gui.mainmenu.dummy.DummyClientPlayerEntity;
import com.tangykiwi.kiwiclient.util.ConfigManager;
import com.tangykiwi.kiwiclient.util.auth.LoginEncrypter;
import com.tangykiwi.kiwiclient.util.auth.LoginHelper;
import com.tangykiwi.kiwiclient.util.font.IFont;
import net.minecraft.client.gui.DrawContext;
import org.apache.commons.lang3.tuple.Pair;
import com.tangykiwi.kiwiclient.gui.window.Window;
import com.tangykiwi.kiwiclient.gui.window.WindowScreen;
import com.tangykiwi.kiwiclient.gui.window.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.Session;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;

public class AccountManagerScreen extends WindowScreen {

    private static final String NO_UUID = "00000000-0000-0000-0000-000000000000";
    private static final LoginEncrypter crypter = new LoginEncrypter(LoginEncrypter.PASS_PHRASE);

    public static List<Account> accounts;
    private static int selected = -1;
    private static int hovered = -1;

    private WindowScrollbarWidget scrollbar;

    private final List<WindowWidget> rightsideWidgets = new ArrayList<>();
    private final List<WindowTextFieldWidget> textFieldWidgets = new ArrayList<>();
    private final List<WindowTextWidget> textWidgets = new ArrayList<>();
    private WindowTextWidget loginResult;


    public AccountManagerScreen() {
        super(Text.literal("Account Manager"));
    }

    public void init() {
        super.init();

        Window mainWindow = addWindow(new Window(
        0,
        0,
        width,
        height, "Account Manager", new ItemStack(Items.PAPER), false, false));

        int w = mainWindow.x2 - mainWindow.x1;
        int h = mainWindow.y2 - mainWindow.y1;
        int listW = Math.max(140, w / 3);

        // Right side
        loginResult = mainWindow.addWidget(new WindowTextWidget(loginResult != null ? loginResult.getText() : "", true, listW + 11, 96, 0xc0c0c0));

        mainWindow.addWidget(new WindowButtonWidget(w - 70, h - 22, w - 3, h - 3, "Login", () -> {
            Account account = accounts.get(selected);
            for (int i = 0; i < textFieldWidgets.size(); i++) {
                account.input[i] = textFieldWidgets.get(i).textField.getText();
            }

            AuthenticationException exception = account.login();
            loginResult.setText(exception == null ? "Login Successful!" : exception.getMessage(), exception == null ? Color.GREEN.hashCode() : Color.RED.hashCode());
            account.success = exception == null ? 2 : 1;
            if(exception == null) {
                DummyClientPlayNetworkHandler.newInstance();
                DummyClientPlayerEntity.newInstance();
            }
            saveAccounts();
        }, Color.WHITE.hashCode()));

        rightsideWidgets.addAll(mainWindow.getWidgets());
        updateRightside();

        // Left side
        scrollbar = mainWindow.addWidget(
                new WindowScrollbarWidget(listW - 10, 28, accounts == null ? 0 : accounts.size() * 28 - 1, h - 29, 0));

        if (accounts == null) {
            accounts = new ArrayList<>();

            ConfigManager.createFile("logins.txt");

            for (String s : ConfigManager.readFileLines("logins.txt")) {
                addAccount(Account.deserialize(s.replace("\r", "").replace("\n", "").split(":", -1)));
            }
        }

        mainWindow.addWidget(new WindowTextWidget("Accounts", true, 6, 17, 0xf0f0f0));
        mainWindow.addWidget(new WindowButtonWidget(listW - 14, 14, listW - 2, 26, "+", () -> {
            selectWindow(1);
            removeWindow(2);
        }, Color.GREEN.hashCode()));
        mainWindow.addWidget(new WindowButtonWidget(listW - 29, 14, listW - 17, 26, "-", () -> {
            if (selected >= 0 && selected < accounts.size()) {
                accounts.remove(selected);
                selected = -1;
                scrollbar.setTotalHeight(accounts.size() * 28 - 1);
                updateRightside();
                saveAccounts();
            }
        }, Color.RED.hashCode()));

        // Select type to add window
        Window typeWindow = addWindow(new Window(
                width / 2 - 48,
                height / 2 - 17,
                width / 2 + 48,
                height / 2 + 21, "Add Account..", new ItemStack(Items.DIAMOND_BLOCK), true));

        typeWindow.addWidget(new WindowTexturedButtonWidget(48 - 40, 15, 48 - 20, 35, AccountButtonWidget.texture,
                () -> openAddAccWindow(AccountType.MICROSOFT, "Microsoft", new ItemStack(Items.EMERALD_BLOCK)), 0));
        typeWindow.addWidget(new WindowTexturedButtonWidget(48 - 10, 15, 48 + 10, 35, AccountButtonWidget.texture,
                () -> openAddAccWindow(AccountType.MOJANG, "Mojang", new ItemStack(Items.GRASS_BLOCK)), 20));
        typeWindow.addWidget(new WindowTexturedButtonWidget(48 + 20, 15, 48 + 40, 35, AccountButtonWidget.texture,
                () -> openAddAccWindow(AccountType.CRACKED, "Cracked", new ItemStack(Items.BARRIER)), 40));
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        IFont.CONSOLAS.drawString(context.getMatrices(), client.getSession().getUsername(), 5, this.height - IFont.CONSOLAS.getFontHeight() - 2, Color.WHITE.hashCode(), 1.0F);

        hovered = -1;
        super.render(context, mouseX, mouseY, delta);
    }

    public void onRenderWindow(DrawContext context, int window, int mouseX, int mouseY) {
        super.onRenderWindow(context, window, mouseX, mouseY);

        if (window == 0) {
            int x = getWindow(0).x1;
            int y = getWindow(0).y1;
            int w = getWindow(0).x2 - x;
            int h = getWindow(0).y2 - y;
            int listW = Math.max(140, w / 3);

            boolean shrink = accounts.size() * 28 >= h - 28;
            for (int c = 0; c < accounts.size(); c++) {
                int curY = y + 28 + c * 28 - scrollbar.getPageOffset();

                if (curY + 28 > y + h || curY < y + 27)
                    continue;

                boolean hover = getWindow(0).selected && mouseX >= x + 1 && mouseX <= x + listW - (shrink ? 12 : 1) && mouseY >= curY && mouseY <= curY + 27;
                drawEntry(context, accounts.get(c), x + 2, curY + 1, listW - (shrink ? 13 : 3), 26,
                        selected == c ? 0x6090e090 : hover ? 0x60b070f0 : 0x60606090);

                if (hover)
                    hovered = c;
            }

            context.fill(x + listW, y + 12, x + listW + 1, y + h - 1, 0xff606090);
        }
    }

    private void drawEntry(DrawContext context, Account acc, int x, int y, int width, int height, int color) {
        Window.fill(context, x, y, x + width, y + height, color);

        double pixelSize = (height - 6) / 8d;
        acc.bindSkin(context, pixelSize, x, y, height);

        pixelSize = ((height - 6) / 10d) * 0.625;
        boolean extendText = acc.bindCape(context, pixelSize, x, y, height);

        IFont.CONSOLAS.drawString(context.getMatrices(), acc.username, extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + 4, Color.WHITE.hashCode(), 1.0F);
        IFont.CONSOLAS.drawString(context.getMatrices(), (acc.type == AccountType.CRACKED ? "Cracked" : acc.type == AccountType.MOJANG ? "Mojang" : "Microsoft"), extendText ? (int) (x + height + pixelSize * 10 + 3) : x + height, y + height - 11, (acc.type == AccountType.CRACKED ? Color.YELLOW.hashCode() : acc.type == AccountType.MOJANG ? Color.GREEN.hashCode() : 0x55FFFF), 1.0F);

        if (acc.type != AccountType.CRACKED) {
            IFont.CONSOLAS.drawString(context.getMatrices(), (acc.success == 0 ? "?" : acc.success == 1 ? "x" : "+"),
                    x + width - 10, y + height - 11, (acc.success == 0 ? 0xFFAA00 : acc.success == 1 ? Color.RED.hashCode() : Color.GREEN.hashCode()), 1.0F);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered >= 0 && hovered < accounts.size()) {
            if (selected >= 0 && selected < accounts.size()) {
                for (int i = 0; i < textFieldWidgets.size(); i++) {
                    accounts.get(selected).input[i] = textFieldWidgets.get(i).textField.getText();
                }
            }

            selected = hovered;
            updateRightside();
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void saveAccounts() {
        ConfigManager.createEmptyFile("logins.txt");
        ConfigManager.appendFile("logins.txt", accounts.stream()
                .map(a -> {
                    try {
                        return a.type.ordinal() + ":" + a.success + ":"
                                + a.uuid + ":" + a.username + ":"
                                + IntStream.range(0, a.input.length).mapToObj(i -> {
                            try {
                                return a.type.inputs[i].getRight() ? crypter.encrypt(a.input[i]) : a.input[i];
                            } catch (Exception e) {
                                throw new RuntimeException();
                            }
                        }).collect(Collectors.joining(":"));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n")));
    }

    private void openAddAccWindow(AccountType type, String name, ItemStack item) {
        getWindow(1).closed = true;

        int h = 40 + type.inputs.length * 40;
        Window addWindow = addWindow(new Window(
                width / 2 - 80,
                height / 2 - h / 2,
                width / 2 + 80,
                height / 2 + h / 2, "Add " + name + " Account", item));

        WindowTextWidget result = addWindow.addWidget(new WindowTextWidget("", true, 10, h - 16, -1));
        List<WindowTextFieldWidget> tf = new ArrayList<>();
        for (int i = 0; i < type.inputs.length; i++) {
            addWindow.addWidget(new WindowTextWidget(type.getInputs()[i].getLeft(), true, 10, 20 + i * 40, 0xf0f0f0));

            if (type.getInputs()[i].getRight()) {
                tf.add(addWindow.addWidget(new WindowPassTextFieldWidget(10, 33 + i * 40, 140, 18, "")));
            } else {
                tf.add(addWindow.addWidget(new WindowTextFieldWidget(10, 33 + i * 40, 140, 18, "")));
            }
        }

        addWindow.addWidget(new WindowButtonWidget(100, h - 20, 157, h - 3, "Add", () -> {
            Account account = new Account(type, 0, null, null, tf.stream().map(t -> t.textField.getText()).toArray(String[]::new));
            try {
                Session session = account.getSession();
                account.uuid = session.getUuid();
                account.username = session.getUsername();
                addAccount(account);
                getWindow(2).closed = true;
            } catch (AuthenticationException e) {
                result.setText(e.getMessage(), Color.RED.hashCode());
            }
        }, Color.WHITE.hashCode()));
    }

    private void updateRightside() {
        getWindow(0).getWidgets().removeAll(textFieldWidgets);
        getWindow(0).getWidgets().removeAll(textWidgets);
        textFieldWidgets.clear();
        textWidgets.clear();
        loginResult.setText("", 0xFFFFFF);

        if (selected != -1) {
            Account a = accounts.get(selected);
            int w = getWindow(0).x2 - getWindow(0).x1;
            int listW = Math.max(140, w / 3);

            for (int i = 0; i < a.input.length; i++) {
                textWidgets.add(getWindow(0).addWidget(
                        new WindowTextWidget(a.type.getInputs()[i].getLeft(), true, listW + 10, 20 + i * 40, 0xf0f0f0)));

                if (a.type.getInputs()[i].getRight()) {
                    textFieldWidgets.add(getWindow(0).addWidget(
                            new WindowPassTextFieldWidget(listW + 10, 33 + i * 40, w - listW - 20, 18, a.input[i])));
                } else {
                    textFieldWidgets.add(getWindow(0).addWidget(
                            new WindowTextFieldWidget(listW + 10, 33 + i * 40, w - listW - 20, 18, a.input[i])));
                }
            }

            loginResult.y1 = 16 + a.input.length * 40;
            loginResult.y2 = loginResult.y1 + 10;
            rightsideWidgets.forEach(wg -> wg.visible = true);
        } else {
            rightsideWidgets.forEach(wg -> wg.visible = false);
        }
    }

    private void addAccount(Account account) {
        if (account == null)
            return;

        if (account.uuid == null) {
            try {
                Session session = account.getSession();

                account.uuid = session.getUuid();
                account.username = session.getUsername();

                account.textures.clear();
                client.getSkinProvider().loadSkin(session.getProfile(), (type, identifier, minecraftProfileTexture) -> account.textures.put(type, identifier), true);
            } catch (AuthenticationException ignored) { }
        } else {
            GameProfile profile = new GameProfile(UUID.fromString(account.uuid), account.username);

            account.textures.clear();
            client.getSkinProvider().loadSkin(profile, (type, identifier, minecraftProfileTexture) -> account.textures.put(type, identifier), true);
        }

        for (int i = 0; i <= accounts.size(); i++) {
            if (i == accounts.size() || String.CASE_INSENSITIVE_ORDER.compare(accounts.get(i).username, account.username) >= 0) {
                accounts.add(i, account);
                break;
            }
        }

        scrollbar.setTotalHeight(accounts.size() * 28 - 1);
    }

    public static class Account {

        public String[] input;
        public String uuid;
        public String username;
        public AccountType type;
        // 0 = ?, 1 = no, 2 = yes
        public int success;

        public Map<MinecraftProfileTexture.Type, Identifier> textures = new EnumMap<>(MinecraftProfileTexture.Type.class);

        public static Account deserialize(String[] data) {
            try {
                if (data.length == 4) { // Old 4-part accounts
                    return new Account(AccountType.MOJANG, 0, data[1], data[2], data[0], crypter.decrypt(data[3]));
                } else if (data.length > 4) {
                    AccountType type = AccountType.values()[Integer.parseInt(data[0])];
                    int success = Integer.parseInt(data[1]);

                    String[] inputs = new String[data.length - 4];
                    for (int i = 4; i < data.length; i++) {
                        inputs[i - 4] = type.inputs[i - 4].getRight() ? crypter.decrypt(data[i]) : data[i];
                    }

                    return new Account(type, success, data[2], data[3], inputs);
                }
            } catch (Exception e) {
                System.out.println("Unable to deserialize account " + data[0] + "\n" + e);
            }

            return null;
        }

        public Account(AccountType type, int success, String uuid, String username, String... input) {
            this.type = type;
            this.success = success;
            this.uuid = uuid;
            this.username = username;
            this.input = input;
        }

        public String getUUID() {
            return uuid;
        }

        public AuthenticationException login() {
            try {
                Session session = getSession();
                MinecraftClient.getInstance().session = session;

                if (!session.getUuid().equals(NO_UUID))
                    MinecraftClient.getInstance().getSessionProperties().clear();

                return null;
            } catch (AuthenticationException e) {
                return e;
            }
        }

        public Session getSession() throws AuthenticationException {
            return type.createSession(input);
        }

        public void bindSkin(DrawContext context, double pixelSize, int x, int y, int height) {
            if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                context.fill(
                        x + 2, y + 2,
                        x + height - 2, y + height - 2,
                        0x60d86ceb);
                context.drawTexture(textures.get(MinecraftProfileTexture.Type.SKIN),
                        x + 3, y + 3,
                        (int) (pixelSize * 8), (int) (pixelSize * 8),
                        (int) (pixelSize * 8), (int) (pixelSize * 8),
                        (int) (pixelSize * 64), (int) (pixelSize * 64));
            } else {
                context.fill(
                        x + 2, y + 2,
                        x + height - 2, y + height - 2,
                        0x60d86ceb);
                context.drawTexture(DefaultSkinHelper.getTexture(),
                        x + 3, y + 3,
                        (int) (pixelSize * 8), (int) (pixelSize * 8),
                        (int) (pixelSize * 8), (int) (pixelSize * 8),
                        (int) (pixelSize * 64), (int) (pixelSize * 64));
            }
        }

        public boolean bindCape(DrawContext context, double pixelSize, int x, int y, int height) {
            if (textures.containsKey(MinecraftProfileTexture.Type.CAPE)) {
                context.fill(
                        x + height - 1, y + 2,
                        (int) (x + height + pixelSize * 10 + 1) + 1, y + height - 2,
                        0x60d86ceb);
                context.drawTexture(textures.get(MinecraftProfileTexture.Type.CAPE),
                        x + height, y + 3,
                        (int) Math.floor(pixelSize), (int) Math.floor(pixelSize),
                        (int) (pixelSize * 11), (int) (pixelSize * 16),
                        (int) (pixelSize * 64), (int) (pixelSize * 32));

                return true;
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public enum AccountType {

        CRACKED(input -> {
            try {
                String id = JsonParser.parseString(
                                Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + input[0]), StandardCharsets.UTF_8))
                        .getAsJsonObject().get("id").getAsString();

                if (id.length() == 32)
                    id = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20);

                return new Session(input[0], id, "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
            } catch (Exception e) {
                return new Session(input[0], NO_UUID, "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
            }
        }, Pair.of("Username", false)),
        MOJANG(input -> {
            return LoginHelper.createMojangSession(input[0], input[1]);
        }, Pair.of("Email", false), Pair.of("Password", true)),
        MICROSOFT(input -> {
            return LoginHelper.createMicrosoftSession(input[0], input[1]);
        }, Pair.of("Email", false), Pair.of("Password", true));

        // Input name, Encrypted?
        private final Pair<String, Boolean>[] inputs;
        private final SessionCreator sessionCreator;

        AccountType(SessionCreator sessionCreator, Pair<String, Boolean>... inputs) {
            this.inputs = inputs;
            this.sessionCreator = sessionCreator;
        }

        public Pair<String, Boolean>[] getInputs() {
            return inputs;
        }

        public Session createSession(String... input) throws AuthenticationException {
            return sessionCreator.apply(input);
        }
    }

    @FunctionalInterface
    private interface SessionCreator {
        Session apply(String[] input) throws AuthenticationException;
    }
}
