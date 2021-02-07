package FilterTheSpire.simulators;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Collections;

public class RelicRngSimulator {
    public ArrayList<String> shopRelicPool = new ArrayList<>();
    public ArrayList<String> bossRelicPool = new ArrayList<>();

    private Random relicRng;
    private long seed;

    public RelicRngSimulator(long seed) {
        setSeed(seed);
    }

    public String nthBossSwap(int n) {
        return bossRelicPool.get(n);
    }
    public String firstShopRelic() {
        // Shop relics are picked from the end of the list despite the shop
        // being the only place to get them... for some reason.
        return shopRelicPool.get(shopRelicPool.size() - 1);
    }

    private void setSeed(long seed) {
        this.seed = seed;
        relicRng = new Random(seed);
        bossRelicPool = new ArrayList<>();

        // Skip past all these
        relicRng.randomLong(); // uncommon
        relicRng.randomLong(); // rare
        relicRng.randomLong(); // common
        // relicRng.randomLong(); // shop
        // this.relicRng.randomLong(); // boss <- this is the one needed (we perform it below)

        generateShopRelics();
        generateBossRelics();
    }

    private void generateShopRelics() {
        RelicLibrary.populateRelicPool(
                this.shopRelicPool,
                AbstractRelic.RelicTier.SHOP,
                AbstractDungeon.player.chosenClass
        );
        Collections.shuffle(this.shopRelicPool, new java.util.Random(relicRng.randomLong()));
    }

    private void generateBossRelics() {
        RelicLibrary.populateRelicPool(
                this.bossRelicPool,
                AbstractRelic.RelicTier.BOSS,
                AbstractDungeon.player.chosenClass
        );
        Collections.shuffle(this.bossRelicPool, new java.util.Random(relicRng.randomLong()));
    }
}
