package com.wt.phonelink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

/**
 * @Author: LuoXia
 * @Date: 2023/1/2 15:15
 * @Description:
 */
public class ContantsTest {

    @Test
    public void getInstance() {
        Contants constants = new Contants();
    }

    @Test
    public void testParams() {
        assertFalse(Contants.IS_FRONT);
        assertFalse(Contants.IS_BACKGROUND);
        assertFalse(Contants.IS_CARLINK_FRONT);
        assertFalse(Contants.IS_PHONE_LINK_FRONT);

        assertEquals(Contants.FragmentCon.TAG_AP_CONNECT, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_HICAR_FAILED, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_HICAR_SCREEN, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_BT_TIPS, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_HICAR_CONNECTING, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_HICAR_HELP, "hicar_ap_connect");
        assertNotEquals(Contants.FragmentCon.TAG_HICAR_INIT_FAILED, "hicar_ap_connect");
    }

}
