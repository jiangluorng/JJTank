package com.jjstudio.jjtank;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MainActivityTest {
    private
    MainActivity subject;
    private byte[] expected;

    @Before
    public void setup(){
        subject = new MainActivity();
        expected = new byte[5];
        expected[0] = (byte) 0xA5;
        expected[1] = (byte) 0xC5;
        expected[2] = (byte) 0x00;
        expected[3] = (byte) 0x00;
        expected[4] = (byte) 0xAA;
    }

    @Test
    public void calculateSpeedDirectionData1() {
        subject.calculateSpeedDirectionData(0x00,0x00);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }

    @Test
    public void calculateSpeedDirectionData2() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData3() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData4() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData5() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData6() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData7() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData8() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData9() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData10() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
    @Test
    public void calculateSpeedDirectionData11() {
        subject.calculateSpeedDirectionData(0x09,0x0E);
        Assert.assertArrayEquals(subject.speedDirectionData,expected);
    }
}