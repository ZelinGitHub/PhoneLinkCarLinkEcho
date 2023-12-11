package com.wt.phonelink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import com.incall.apps.hicar.servicesdk.contants.Constants;

import org.junit.Test;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 15:15
 * @Description:
 */
public class ContantsTest {

    @Test
    public void getInstance() {
        Constants constants = new Constants();
    }

    @Test
    public void testParams() {
        assertFalse(Constants.IS_HICAR_FRONT);
        assertFalse(Constants.IS_HICAR_BACKGROUND_CONNECT);
        assertFalse(Constants.IS_CARLINK_FRONT);
        assertFalse(Constants.IS_PHONE_LINK_FRONT);

        assertEquals(Constants.FragmentCon.TAG_AP_CONNECT, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_HICAR_FAILED, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_HICAR_SCREEN, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_BT_TIPS, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_HICAR_CONNECTING, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_HICAR_HELP, "hicar_ap_connect");
        assertNotEquals(Constants.FragmentCon.TAG_HICAR_INIT_FAILED, "hicar_ap_connect");
    }

}
