package com.abby.redditgo.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by gsshop on 2017. 1. 18..
 */

public interface SubredditColumn {
    @PrimaryKey
    @DataType(TEXT)
    String ID = "_id";

    @DataType(TEXT)
    String DISPLAY_NAME = "display_name";

}
