package com.jjstudio.jjtank.util;

import com.jjstudio.jjtank.model.Switcher;

public class ControlUtil {

    public static byte[] calculateSwitchData(Switcher switcher) {
        byte[] switchData = new byte[5];
        switchData[0] = (byte) 0xA5;
        switchData[1] = (byte) 0xC6;
        int lightPosition=0;
        int lightOn=0;
        if (switcher.isSwitch1on()){
            lightPosition+=2;
            lightOn+=2;
        }
        if (switcher.isSwitch2on()){
            lightPosition+=4;
            lightOn+=4;
        }
        if (switcher.isSwitch3on()){
            lightPosition+=8;
            lightOn+=8;
        }
        if (switcher.isSwitch4on()){
            lightPosition+=16;
            lightOn+=16;
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
        //不响应区
        if (Math.abs(speed) < 10) {
            tankSpeed = 0;
        } else {
            if (speed > 0) {
                tankSpeed = (int) ((speed - 10) * 1.25);
            } else {
                tankSpeed = (int) ((speed + 10) * 1.25);
            }
        }
        if (Math.abs(direction) < 15) {
            tankDirection = 0;
        } else if (Math.abs(direction) < 40) {
            if (direction > 0) {
                tankDirection = (direction - 15) * 2;
            } else {
                tankDirection = (direction + 15) * 2;
            }
        }
        //直线前进, 后退
        if (tankSpeed != 0 && tankDirection == 0) {
            if (tankSpeed > 0) {
                speedDirectionData[2] = (byte) tankSpeed;
                speedDirectionData[3] = (byte) tankSpeed;
            } else {
                speedDirectionData[2] = (byte) (0x40 - tankSpeed);
                speedDirectionData[3] = (byte) (0x40 - tankSpeed);
            }

            //差速运动
        } else if (tankSpeed != 0 && Math.abs(tankDirection) < 40) {
            if (tankSpeed > 0) {
                if (tankDirection > 0) {        //差速前左转
                    speedDirectionData[2] = (byte) (speed * 2 / 3);
                    speedDirectionData[3] = (byte) speed;
                } else {                    //差速前右转
                    speedDirectionData[2] = (byte) speed;
                    speedDirectionData[3] = (byte) (speed * 2 / 3);
                }
            } else if (tankSpeed < 0) {
                if (tankDirection > 0) {        //差速后左转
                    speedDirectionData[2] = (byte) (0x40 - speed * 2 / 3);
                    speedDirectionData[3] = (byte) (0 - speed);
                } else {                    //差速后右转
                    speedDirectionData[2] = (byte) (0x40 - speed);
                    speedDirectionData[3] = (byte) (0x40 - speed * 2 / 3);
                }
            }
        }

        //原地转弯
        else if (Math.abs(tankSpeed) < 0) {
            if (tankDirection > 0) {        //原地左转
                speedDirectionData[2] = 0x7A;
                speedDirectionData[3] = 0x3A;
            } else {                    //原地右转
                speedDirectionData[2] = 0x7A;
                speedDirectionData[3] = 0x3A;
            }
        }

        //单边锁死转弯
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
                    speedDirectionData[3] = (byte) (0 - tankSpeed);
                } else {                    //左后右死
                    speedDirectionData[2] = (byte) (0 - tankSpeed);
                    speedDirectionData[3] = 0x00;
                }
            }

        }
        speedDirectionData[5] = (byte) tankSpeed;
        speedDirectionData[6] = (byte) tankDirection;
        return speedDirectionData;
    }

}
