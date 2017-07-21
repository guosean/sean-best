package com.sean.business;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;

/**
 *
 * Created by zhenbin.guo on 2017/07/20.
 */
public class TestSlotMachine {

    private SlotMachine<SeanAward> slotMachine = new SlotMachine();
    private int amount = 100;

    @Before
    public void before(){
        List<SeanAward> seanAwards = Lists.newArrayListWithCapacity(amount);
        Random random = new Random();
        for(int i=0; i<amount; i++){
            seanAwards.add(new SeanAward(random.nextInt(10000),"code-"+i));
        }

        slotMachine.load(seanAwards);
    }

    @After
    public void after(){
        slotMachine.clear();
    }

    @Test
    public void testDrawOne(){
        SeanAward award = slotMachine.drawOne();
        Assert.assertNotNull(award);
    }

    @Test
    public void testDrawWithFilter(){
        SeanAward award = slotMachine.drawWithFilter(new Predicate<SeanAward>() {
            @Override
            public boolean apply(SeanAward seanAward) {
                return true;
            }
        });
        Assert.assertNotNull(award);
        award = slotMachine.drawWithFilter(new Predicate<SeanAward>() {
            @Override
            public boolean apply(SeanAward seanAward) {
                return false;
            }
        });
        Assert.assertNull(award);
    }

    @Test
    public void testDrawMore(){
        int drawAmount = amount * 2;
        List<SeanAward> seanAwards = slotMachine.drawMore(drawAmount);
        Assert.assertTrue(CollectionUtils.isNotEmpty(seanAwards));
        Assert.assertEquals(seanAwards.size(),drawAmount);
    }

    @Test
    public void testDrawWithRandom(){
        Random random = new Random();
        int totalWeight = slotMachine.getTotalWeight();

        int rdm = random.nextInt(totalWeight);
        SeanAward seanAward = slotMachine.drawWithRandom(rdm);
        Assert.assertNotNull(seanAward);

        rdm = 0;
        seanAward = slotMachine.drawWithRandom(rdm);
        Assert.assertNotNull(seanAward);

        rdm = totalWeight;
        seanAward = slotMachine.drawWithRandom(rdm);
        Assert.assertNotNull(seanAward);

        rdm = -1;
        seanAward = null;
        boolean throwException = false;
        try{
           seanAward = slotMachine.drawWithRandom(rdm);
        } catch (Exception e){
            throwException = true;
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getClass(),IllegalArgumentException.class);
            Assert.assertEquals(e.getMessage(),"random must between 0 and total weight");
        }

        Assert.assertTrue(throwException);
        Assert.assertNull(seanAward);

        rdm = totalWeight+1;
        throwException = false;
        try{
            seanAward = slotMachine.drawWithRandom(rdm);
        } catch (Exception e){
            throwException = true;
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getClass(),IllegalArgumentException.class);
        }
        Assert.assertTrue(throwException);
        Assert.assertNull(seanAward);
    }

    static class SeanAward implements SlotMachine.IAward{

        int weight;
        String code;

        public SeanAward(int weight,String code){
            this.weight = weight;
            this.code = code;
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

}
