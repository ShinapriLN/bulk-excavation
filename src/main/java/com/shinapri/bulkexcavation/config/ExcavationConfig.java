package com.shinapri.bulkexcavation.config;

public class ExcavationConfig {
    public static final int  DEFAULT_MAX_VOLUME   = 8192;
    public static final boolean DEFAULT_REQUIRE_TOOL = true;
    public static final boolean DEFAULT_DROP_LOOT   = true;
    public static final boolean DEFAULT_CONSOLE_LOG = true;

    public int  maxVolume   = DEFAULT_MAX_VOLUME;
    public boolean requireTool = DEFAULT_REQUIRE_TOOL;
    public boolean dropLoot    = DEFAULT_DROP_LOOT;
    public boolean consoleLog  = DEFAULT_CONSOLE_LOG;
}
