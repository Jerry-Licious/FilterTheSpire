package FilterTheSpire.filters;

import FilterTheSpire.simulators.RelicRngSimulator;
import com.megacrit.cardcrawl.relics.PrismaticShard;

public class FirstShopRelicFilter extends AbstractFilter {
    private String shopRelicName;

    public FirstShopRelicFilter(String shopRelicName) {
        this.shopRelicName = shopRelicName;
    }

    @Override
    public boolean isSeedValid(long seed) {
        RelicRngSimulator simulator = new RelicRngSimulator(seed);
        System.out.println(simulator.shopRelicPool);
        return simulator.firstShopRelic().equals(shopRelicName);
    }
}
