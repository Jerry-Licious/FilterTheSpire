package FilterTheSpire.ui.screens;

import FilterTheSpire.FilterManager;
import FilterTheSpire.FilterTheSpire;
import FilterTheSpire.utils.ExtraColors;
import FilterTheSpire.utils.ExtraFonts;
import FilterTheSpire.utils.KeyHelper;
import FilterTheSpire.utils.SoundHelper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Consumer;

/*
    Shown when the user goes to Main Menu -> Mods -> Filter the Spire -> Config
 */
public class BossSwapFilterScreen {
    // TODO: hitboxes, etc.
    private static class RelicUIObject {
        private int size = 100;
        private int hbSize = 75;
        private int hbOffset = 50;

        private Hitbox hb;

        private String relicID;
        private float x, y;
        private Texture tex;
        private static final Texture TEX_SELECTED_BG = new Texture("FilterTheSpire/images/relic_bg.png");

        private boolean isEnabled = false;
        private BossSwapFilterScreen parent;

        public RelicUIObject(BossSwapFilterScreen parent, String relicID, float x, float y) {
            this.relicID = relicID;
            this.tex = ImageMaster.getRelicImg(relicID);
            this.x = x;
            this.y = y;
            this.parent = parent;

            hb = new Hitbox(hbSize * Settings.scale, hbSize * Settings.scale);
        }

        public void enableHitbox() {
            // Need to adjust them (hb are centered) -- this random guess is probably totally off
            hb.move((x + hbOffset) * Settings.scale, (y + hbOffset) * Settings.scale);
        }

        public void disableHitbox() {
            hb.move(-10000.0f, -10000.0f);
        }

        public void render(SpriteBatch sb) {
            // Grow a bit larger when hovered
            float s = (hb.hovered) ? size * 1.10f : size;

            if (isEnabled) {
                sb.setColor(ExtraColors.SEL_RELIC_BG);
                sb.draw(TEX_SELECTED_BG, x * Settings.scale, y * Settings.scale, s * Settings.scale, s * Settings.scale);

                sb.setColor(Color.WHITE);
            } else {
                sb.setColor(ExtraColors.DIM_RELIC);
            }


            sb.draw(tex, x * Settings.scale, y * Settings.scale, s * Settings.scale, s * Settings.scale);

            // DEBUG
            hb.render(sb);
        }

        private void handleClick() {
            if (KeyHelper.isShiftPressed()) {
                CardCrawlGame.sound.play("BLOOD_SPLAT");
                parent.selectAll();
            }
            else if (KeyHelper.isAltPressed()) {
                CardCrawlGame.sound.play("MAP_SELECT_3");
                parent.invertAll();
            }
            else {
                if (isEnabled) CardCrawlGame.sound.playA("UI_CLICK_1", 0.2f);
                else CardCrawlGame.sound.playA("UI_CLICK_1", -0.4f);

                isEnabled = !isEnabled;
                parent.refreshFilters();
            }
        }

        private void handleRightClick() {
            if (KeyHelper.isShiftPressed()) {
                CardCrawlGame.sound.play("APPEAR");
                parent.clearAll();
            }
            else {
                CardCrawlGame.sound.play("KEY_OBTAIN");
                parent.selectOnly(relicID);
            }
        }

        private boolean mouseDownRight = false;

        public void update() {
            hb.update();

            if (hb.justHovered) {
                CardCrawlGame.sound.playAV("UI_HOVER", -0.4f, 0.5f);
            }

            // Right clicks
            if (hb.hovered && InputHelper.isMouseDown_R) {
                mouseDownRight = true;
            } else {
                // We already had the mouse down, and now we released, so fire our right click event
                if (hb.hovered && mouseDownRight) {
                    handleRightClick();
                    mouseDownRight = false;
                }
            }

            // Left clicks
            if (this.hb.hovered && InputHelper.justClickedLeft) {
                this.hb.clickStarted = true;
            }

            if (hb.clicked) {
                hb.clicked = false;
                handleClick();
            }

        }
    }

    private TreeSet<String> bossRelics = new TreeSet<>();
    private HashMap<String, RelicUIObject> relicUIObjects = new HashMap<>();
    //private boolean alreadySetup = false;

    public BossSwapFilterScreen() {
        setup();
    }

    private void populateRelics() {
        ArrayList<String> relics = new ArrayList<>();

        RelicLibrary.populateRelicPool(relics, AbstractRelic.RelicTier.BOSS, AbstractPlayer.PlayerClass.IRONCLAD);
        RelicLibrary.populateRelicPool(relics, AbstractRelic.RelicTier.BOSS, AbstractPlayer.PlayerClass.THE_SILENT);
        RelicLibrary.populateRelicPool(relics, AbstractRelic.RelicTier.BOSS, AbstractPlayer.PlayerClass.DEFECT);
        RelicLibrary.populateRelicPool(relics, AbstractRelic.RelicTier.BOSS, AbstractPlayer.PlayerClass.WATCHER);

        bossRelics.addAll(relics);
        removeClassUpgradedRelics();
    }

    // Don't allow unswappable relics to enter the pool
    private void removeClassUpgradedRelics() {
        Consumer<String> remove = (relicName) -> {
            if (bossRelics.contains(relicName))
                bossRelics.remove(relicName);
        };

        remove.accept("Black Blood");
        remove.accept("Ring of the Serpent");
        remove.accept("FrozenCore");
        remove.accept("HolyWater");
    }

    private void makeUIObjects() {
        // Note: relic textures are 128x128 originally, with some internal spacing
        float left = 410.0f;
        float top = 587.0f;

        float spacing = 84.0f;

        int ix = 0;
        int iy = 0;
        final int perRow = 5;

        for (String id : bossRelics) {
            float tx = left + ix * spacing;
            float ty = top - iy * spacing;

            relicUIObjects.put(id, new RelicUIObject(this, id, tx, ty));

            ix++;
            if (ix > perRow) {
                ix = 0;
                iy++;
            }
        }
    }
    private void loadFromConfig() {
        ArrayList<String> loaded = FilterTheSpire.config.getBossSwapFilter();
        for (String relic : loaded) {
            if (relicUIObjects.containsKey(relic))
                relicUIObjects.get(relic).isEnabled = true;
        }

        refreshFilters();
    }

    private void setup() {
        populateRelics();
        makeUIObjects();
        loadFromConfig();
    }

    public void renderForeground(SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        for (RelicUIObject x : relicUIObjects.values())
            x.render(sb);

        // Title text
        float titleLeft = 386.0f;
        float titleBottom = 819.0f;
        FontHelper.renderFontLeftDownAligned(sb, ExtraFonts.configTitleFont(), "Neow Boss Swaps", titleLeft * Settings.scale, titleBottom * Settings.scale, Settings.GOLD_COLOR);

        float infoLeft = 1120.0f;
        float infoTopMain = 667.0f;
        float infoTopControls = 472.0f;

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                "This filter allows you to choose which Boss Relics will appear from Neow's swap option. If no relics are selected, it will choose from the entire pool.",
                infoLeft * Settings.scale,
                infoTopMain * Settings.scale,
                371.0f * Settings.scale,
                30.0f * Settings.scale,
                Settings.CREAM_COLOR);

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                "Controls: NL Click to toggle NL Right+Click to select just one NL NL Shift+Click to select all NL Shift+Right+Click to clear all NL Alt+Click to invert all",
                infoLeft * Settings.scale,
                infoTopControls * Settings.scale,
                371.0f * Settings.scale,
                30.0f * Settings.scale,
                Color.GRAY);
    }

    public void enableHitboxes(boolean enabled) {
        for (RelicUIObject obj : relicUIObjects.values()) {
            if (enabled)
                obj.enableHitbox();
            else
                obj.disableHitbox();
        }
    }

    public void render(SpriteBatch sb) {
        renderForeground(sb);
    }

    public void update() {
        for (RelicUIObject x : relicUIObjects.values())
            x.update();
    }

    // --------------------------------------------------------------------------------

    private void clearAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = false;
        }

        refreshFilters();
    }

    private void select(String id) {
        if (relicUIObjects.containsKey(id)) {
            relicUIObjects.get(id).isEnabled = true;
            refreshFilters();
        }
    }

    private void selectOnly(String id) {
        if (relicUIObjects.containsKey(id)) {
            clearAll();
            relicUIObjects.get(id).isEnabled = true;
            refreshFilters();
        }
    }

    private void invertAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = !obj.isEnabled;
        }

        refreshFilters();
    }

    private void selectAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = true;
        }

        refreshFilters();
    }

    // --------------------------------------------------------------------------------

    public ArrayList<String> getEnabledRelics() {
        ArrayList<String> list = new ArrayList<>();

        for (RelicUIObject obj : relicUIObjects.values()) {
            if (obj.isEnabled)
                list.add(obj.relicID);
        }

        return list;
    }

    public void refreshFilters() {
        ArrayList<String> enabledRelics = getEnabledRelics();
        FilterTheSpire.config.setBossSwapFilter(enabledRelics);
        FilterManager.setBossSwapFiltersFromValidList(enabledRelics);
    }
}
