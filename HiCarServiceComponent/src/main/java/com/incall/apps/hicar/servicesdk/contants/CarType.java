package com.incall.apps.hicar.servicesdk.contants;

/**
 * 车辆状态类型定义
 * @author Created by xiejunlin on 2019/8/12.
 */
public class CarType {

    /**
     * 无效车速
     */
    public static final int SPEED_INVALID = 0x1FFF;

    /**
     * 无效温度
     */
    public static final int TEMP_INVALID = 0xFF;

    /**
     * 无效PM2.5
     */
    public static final int PM25_INVALID = 0x3FF;

    /**
     * 档位定义
     */
    public static final class Gear {
        /**
         * 台架测试
         */
        public static final int GEAR_RACK = 0x0000;
        /**
         * 空档
         */
        public static final int GEAR_NEUTRAL = 0x0001;
        /**
         * 倒车档
         */
        public static final int GEAR_REVERSE = 0x0002;
        /**
         * 停车档
         */
        public static final int GEAR_PARK = 0x0004;
        /**
         * 自动档
         */
        public static final int GEAR_DRIVE = 0x0008;
        /**
         * 1档
         */
        public static final int GEAR_FIRST = 0x0010;
        /**
         * 2档
         */
        public static final int GEAR_SECOND = 0x0020;
        /**
         * 3档
         */
        public static final int GEAR_THIRD = 0x0040;
        /**
         * 4档
         */
        public static final int GEAR_FOURTH = 0x0080;
        /**
         * 5档
         */
        public static final int GEAR_FIFTH = 0x0100;
        /**
         * 6档
         */
        public static final int GEAR_SIXTH = 0x0200;
        /**
         * 7档
         */
        public static final int GEAR_SEVENTH = 0x0400;
        /**
         * 8档
         */
        public static final int GEAR_EIGHTH = 0x0800;
        /**
         * 9档
         */
        public static final int GEAR_NINTH = 0x1000;
        /**
         * 10档
         */
        public static final int GEAR_TENTH = 0x2000;
    }

    /**
     * 引擎状态定义
     */
    public static final class Ignition {
        /**
         * UNDEFINED状态
         */
        public static final int STATE_UNDEFINED = 0x0;
        /**
         * LOCK状态
         */
        public static final int STATE_LOCK = 0x1;
        /**
         * OFF状态
         */
        public static final int STATE_OFF = 0x2;
        /**
         * ACC状态
         */
        public static final int STATE_ACC = 0x3;
        /**
         * ON状态
         */
        public static final int STATE_ON = 0x4;
        /**
         * START档
         */
        public static final int STATE_START = 0x5;
    }
}
