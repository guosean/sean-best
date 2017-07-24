package com.sean.business;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/**
 * 抽奖机
 * Created by zhenbin.guo on 2017/07/20.
 */
public class SlotMachine<T extends SlotMachine.IAward> {

    private TreeSet<SlotAward> awards;

    private Random random = new Random();

    private int totalWeight;

    /**
     * 加载奖品
     *
     * @param initAwards
     */
    public void load(Iterable<T> initAwards) {
        Preconditions.checkNotNull(initAwards, "awards cannot be null");

        totalWeight = 0;
        final SlotAward _tmp = new SlotAward(null, 0);

        Iterable<SlotAward> slotAwardIterator = Iterables.transform(initAwards, new Function<T, SlotAward>() {
            @Override
            public SlotAward apply(T t) {
                totalWeight += t.getWeight();
                SlotAward slotAward = new SlotAward(t, _tmp.getEnd());
                _tmp.range = slotAward.range;
                return slotAward;
            }
        });

        awards = Sets.newTreeSet(slotAwardIterator);
        checkTotalWeight();
    }

    /**
     * 抽奖一次
     *
     * @return
     */
    public T drawOne() {
        return drawWithFilter(null);
    }

    /**
     * 可用于检查抽中奖品的库存情况等合法性验证
     * @param predicate
     * @return
     */
    public T drawWithFilter(Predicate<T> predicate){
        checkAwards();
        checkTotalWeight();

        int rdm = random.nextInt(totalWeight);
        T award = drawWithRandom(rdm);

        if(null != predicate){
            return ((null != award) && predicate.apply(award)) ? award : null;
        }

        return award;
    }

    /**
     * 用于自定义随机码
     *
     * @param random
     * @return
     */
    public T drawWithRandom(final int random) {
        Preconditions.checkArgument(random >= 0 && random <= totalWeight, "random must between 0 and total weight");
        checkAwards();

        SlotAward cmp = new SlotAward() {
            @Override
            public int getStart() {
                return random;
            }
        };

        SlotAward slotAward = awards.floor(cmp);

        return (T) slotAward.award;
    }

    /**
     * 抽奖多次
     *
     * @param amount
     * @return
     */
    public List<T> drawMore(int amount) {
        Preconditions.checkArgument(amount > 0, "amount should gt 0");
        checkAwards();

        List<T> result = Lists.newArrayListWithCapacity(amount);
        for (int i = 0; i < amount; i++) {
            result.add(drawOne());
        }

        return result;
    }

    public void clear(){
        if(null != awards){
            awards.clear();
        }
        totalWeight = 0;
    }

    public int getTotalWeight(){
        return totalWeight;
    }

    private void checkAwards() {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(awards), "awards must be loaded and should not be empty");
    }

    private void checkTotalWeight(){
        Preconditions.checkArgument(totalWeight > 0, "the weight of total should be gt 0");
    }

    public interface IAward {

        /**
         * 权重
         *
         * @return
         */
        int getWeight();

        /**
         * 奖品编码
         *
         * @return
         */
        String getCode();

    }

    static class SlotAward implements IAward, Comparable<SlotAward> {
        IAward award;
        Range<Integer> range;

        SlotAward() {
        }

        SlotAward(IAward iAward, int start) {
            this.award = iAward;
            range = Range.range(start, BoundType.CLOSED,start + (null == iAward ? 0 : iAward.getWeight()), BoundType.CLOSED);
        }

        public int getStart() {
            return range.lowerEndpoint();
        }

        public int getEnd() {
            return range.upperEndpoint();
        }

        public int getWeight() {
            return null == award ? 0: award.getWeight();
        }

        public String getCode() {
            return null == award ? null : award.getCode();
        }

        public int compareTo(SlotAward o) {
            return o.getStart() > getStart() ? -1 : 1;
        }

        public String toString() {

            return MoreObjects.toStringHelper(SlotAward.class).add("weight",getWeight()).add("code",getCode()).toString();
        }

    }

}
