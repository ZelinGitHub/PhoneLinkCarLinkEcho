package com.wt.phonelink.constant;

import com.wt.phonelink.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String LINK_TYPE_TINNOVE_BOX = "tinnove_box";
    public static final String LINK_TYPE_HICAR = "hicar";
    public static final String LINK_TYPE_ICCOA = "iccoa";
    public static final String LINK_TYPE_HIHONOR = "hihonor";

    public static List<String> sLinkTypes = new ArrayList<String>() {
        {
            add(LINK_TYPE_TINNOVE_BOX);
            add(LINK_TYPE_HICAR);
            add(LINK_TYPE_ICCOA);
            add(LINK_TYPE_HIHONOR);
        }
    };


    private Constants() {
    }
}
