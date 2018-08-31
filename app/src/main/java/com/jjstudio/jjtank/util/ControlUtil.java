package com.jjstudio.jjtank.util;

import com.jjstudio.jjtank.model.Switcher;

public class ControlUtil {

    public static byte[] calculateSwitchData(Switcher switcher) {
        byte[] switchData = new byte[5];
        switchData[0] = (byte) 0xA5;
        switchData[1] = (byte) 0xC6;
        int lightPosition = 0;
        int lightOn = 0;
        if (switcher.isSwitch1on()) {
            lightPosition += 2;
            lightOn += 2;
        }
        if (switcher.isSwitch2on()) {
            lightPosition += 4;
            lightOn += 4;
        }
        if (switcher.isSwitch3on()) {
            lightPosition += 8;
            lightOn += 8;
        }
        if (switcher.isSwitch4on()) {
            lightPosition += 16;
            lightOn += 16;
        }
        switchData[2] = (byte) lightPosition;
        switchData[3] = (byte) lightOn;
        switchData[4] = (byte) 0xAA;
        return switchData;
    }


    public static byte[] calculateSpeedDirectionData(int speed, int direction) {
        // 不响应 速度 speed<10
        //  speed>30 则满速
        // 不响应 方向 direction<15
        // direction >40 则满速转
        byte[] speedDirectionData = new byte[7];
        speedDirectionData[0] = (byte) 0xA5;
        speedDirectionData[1] = (byte) 0xC5;
        speedDirectionData[2] = (byte) 0x00;
        speedDirectionData[3] = (byte) 0x00;
        speedDirectionData[4] = (byte) 0xAA;
        int tankSpeed = speed;
        int tankDirection = direction;
        //不响应区 & 原地旋转区
        if (Math.abs(tankSpeed) < 10) {
            //不响应
            if (Math.abs(tankDirection) < 30) {
                speedDirectionData[2] = 0x00;
                speedDirectionData[3] = 0x00;
            }
            //原地旋转
            else {    //if (Math.abs(direction) >= 30)
                if (tankDirection > 0) {        //原地左转
                    speedDirectionData[2] = 0x70;
                    speedDirectionData[3] = 0x30;
                } else {                    //原地右转
                    speedDirectionData[2] = 0x30;
                    speedDirectionData[3] = 0x70;
                }
            }
        }
        //直线前进, 后退
        else if (Math.abs(tankDirection) < 10) {  //已经满足Math.abs(tankSpeed) >= 10
            if (tankSpeed > 0) {
                speedDirectionData[2] = (byte) tankSpeed;
                speedDirectionData[3] = (byte) tankSpeed;
            } else {
                speedDirectionData[2] = (byte) (0x40 - tankSpeed);
                speedDirectionData[3] = (byte) (0x40 - tankSpeed);
            }
        }
        //差速运动模式 1
            // ( 10<=Math.abs(tankDirection)<20) 且 Math.abs(tankSpeed) >= 10
        else if (Math.abs(tankDirection) < 20) {
            if (tankSpeed > 0) {
                if (tankDirection > 0) {        //差速前左转
                    speedDirectionData[2] = (byte) (speed / 3);
                    speedDirectionData[3] = (byte) speed;
                } else {                    //差速前右转
                    speedDirectionData[2] = (byte) speed;
                    speedDirectionData[3] = (byte) (speed / 3);
                }
            } else if (tankSpeed < 0) {
                if (tankDirection > 0) {        //差速后左转
                    speedDirectionData[2] = (byte) (0x40 - speed / 3);
                    speedDirectionData[3] = (byte) (0x40 - speed);
                } else {                    //差速后右转
                    speedDirectionData[2] = (byte) (0x40 - speed);
                    speedDirectionData[3] = (byte) (0x40 - speed / 3);
                }
            }
        }
        //差速运动模式 2
        // ( 20<=Math.abs(tankDirection)<30) 且 Math.abs(tankSpeed) >= 10
        else if (Math.abs(tankDirection) < 30) {
            if (tankSpeed > 0) {
                if (tankDirection > 0) {        //差速前左转
                    speedDirectionData[2] = (byte) (speed / 5);
                    speedDirectionData[3] = (byte) speed;
                } else {                    //差速前右转
                    speedDirectionData[2] = (byte) speed;
                    speedDirectionData[3] = (byte) (speed / 5);
                }
            } else if (tankSpeed < 0) {
                if (tankDirection > 0) {        //差速后左转
                    speedDirectionData[2] = (byte) (0x40 - speed / 5);
                    speedDirectionData[3] = (byte) (0x40 - speed);
                } else {                    //差速后右转
                    speedDirectionData[2] = (byte) (0x40 - speed);
                    speedDirectionData[3] = (byte) (0x40 - speed / 5);
                }
            }
        }
        //单边锁死转弯
        //( Math.abs(tankDirection)>=30) 且 Math.abs(tankSpeed) >= 10
        else {
            if (tankSpeed > 0) {
                if (tankDirection > 0) {        //左死右前
                    speedDirectionData[2] = 0x00;
                    speedDirectionData[3] = (byte) tankSpeed;
                } else {                    //左前右死
                    speedDirectionData[2] = (byte) tankSpeed;
                    speedDirectionData[3] = 0x00;
                }
            } else if (tankSpeed < 0) {
                if (tankDirection > 0) {        //左死右后
                    speedDirectionData[2] = 0x00;
                    speedDirectionData[3] = (byte) (0x40 - tankSpeed);
                } else {                    //左后右死
                    speedDirectionData[2] = (byte) (0x40 - tankSpeed);
                    speedDirectionData[3] = 0x00;
                }
            }

        }
        speedDirectionData[5] = (byte) tankSpeed;
        speedDirectionData[6] = (byte) tankDirection;
        return speedDirectionData;
    }

}

