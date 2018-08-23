package com.jjstudio.jjtank.util;

public class ControlUtil {

    public static byte[] calculateSpeedDirectionData(int speed, int direction) {
        //
        byte[] speedDirectionData = new byte[5];
        speedDirectionData[0] = (byte) 0xA5;
        speedDirectionData[1] = (byte) 0xC5;
        speedDirectionData[2] = (byte) 0x00;
        speedDirectionData[3] = (byte) 0x00;
        speedDirectionData[4] = (byte) 0xAA;

        //不响应区
        if ((Math.abs(speed) < 10) && (Math.abs(direction) < 15)) {
            speedDirectionData[2] = 0;
            speedDirectionData[3] = 0;
            return speedDirectionData;
        }
//        speed = speed-10;
//        direction = direction-15;
        //直线前进, 后退
        if (speed != 0 && Math.abs(direction) < 10) {
            if (speed > 0) {
                speedDirectionData[2] = (byte) speed;
                speedDirectionData[3] = (byte) speed;
            } else {
                speedDirectionData[2] = (byte) (0x40 - speed);
                speedDirectionData[3] = (byte) (0x40 - speed);
            }

            //差速运动
        } else if (speed != 0 && Math.abs(direction) < 50) {
            if (speed > 0) {
                if (direction > 0) {        //差速前左转
                    speedDirectionData[2] = (byte) (speed * 2 / 3);
                    speedDirectionData[3] = (byte) speed;
                } else {                    //差速前右转
                    speedDirectionData[2] = (byte) speed;
                    speedDirectionData[3] = (byte) (speed * 2 / 3);
                }
            } else if (speed < 0) {
                if (direction > 0) {        //差速后左转
                    speedDirectionData[2] = (byte) (0x40 - speed * 2 / 3);
                    speedDirectionData[3] = (byte) (0 - speed);
                } else {                    //差速后右转
                    speedDirectionData[2] = (byte) (0x40 - speed);
                    speedDirectionData[3] = (byte) (0x40 - speed * 2 / 3);
                }
            }
        }

        //原地转弯
        else if (Math.abs(speed) < 10) {
            if (direction > 0) {        //原地左转
                speedDirectionData[2] = 0x7A;
                speedDirectionData[3] = 0x3A;
            } else {                    //原地右转
                speedDirectionData[2] = 0x7A;
                speedDirectionData[3] = 0x3A;
            }
        }

        //单边锁死转弯
        else {
            if (speed > 0) {
                if (direction > 0) {        //左死右前
                    speedDirectionData[2] = 0x00;
                    speedDirectionData[3] = (byte) speed;
                } else {                    //左前右死
                    speedDirectionData[2] = (byte) speed;
                    speedDirectionData[3] = 0x00;
                }
            } else if (speed < 0) {
                if (direction > 0) {        //左死右后
                    speedDirectionData[2] = 0x00;
                    speedDirectionData[3] = (byte) (0 - speed);
                } else {                    //左后右死
                    speedDirectionData[2] = (byte) (0 - speed);
                    speedDirectionData[3] = 0x00;
                }
            }

        }
        return speedDirectionData;
    }

}
