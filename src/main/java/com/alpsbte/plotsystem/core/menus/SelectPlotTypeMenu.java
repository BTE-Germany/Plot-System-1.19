/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.PlotType;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class SelectPlotTypeMenu extends AbstractMenu {
    private Builder builder;

    public SelectPlotTypeMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SELECT_PLOT_TYPE), player);
    }

    @Override
    protected void setPreviewItems() {
        super.setPreviewItems();

        builder = Builder.byUUID(getMenuPlayer().getUniqueId());
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot type items
        getMenu().getSlot(11).setItem(
                new ItemBuilder(Utils.HeadUtils.FOCUS_MODE_HEAD.getAsItemStack())
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_FOCUS_MODE))
                        .setLore(new LoreBuilder()
                                .addLines(AlpsUtils.createMultilineFromString(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_FOCUS_MODE), LoreBuilder.MAX_LINE_LENGTH, LoreBuilder.LINE_BAKER))
                                .build())
                        .setEnchanted(builder.getPlotTypeSetting().getId() == PlotType.FOCUS_MODE.getId())
                        .build());

        getMenu().getSlot(13).setItem(
                new ItemBuilder(Material.DARK_OAK_SAPLING, 1)
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_INSPIRATION_MODE))
                        .setLore(new LoreBuilder()
                                .addLines(AlpsUtils.createMultilineFromString(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_INSPIRATION_MODE), LoreBuilder.MAX_LINE_LENGTH, LoreBuilder.LINE_BAKER))
                                .build())
                        .setEnchanted(builder.getPlotTypeSetting().getId() == PlotType.LOCAL_INSPIRATION_MODE.getId())
                        .build());

        getMenu().getSlot(15).setItem(
                new ItemBuilder(Utils.HeadUtils.CITY_INSPIRATION_MODE_HEAD.getAsItemStack())
                        .setName("§6§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_CITY_INSPIRATION_MODE) + " §7§l[§c§lBETA§7§l]") // temporary BETA tag
                        .setLore(new LoreBuilder()
                                .addLines(AlpsUtils.createMultilineFromString(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_CITY_INSPIRATION_MODE), LoreBuilder.MAX_LINE_LENGTH, LoreBuilder.LINE_BAKER))
                                .build())
                        .setEnchanted(builder.getPlotTypeSetting().getId() == PlotType.CITY_INSPIRATION_MODE.getId())
                        .build());

        // Set selected glass pane
        int selectedPlotTypeSlot = 13;
        if(builder.getPlotTypeSetting() == PlotType.FOCUS_MODE)
            selectedPlotTypeSlot = 11;
        if(builder.getPlotTypeSetting() == PlotType.CITY_INSPIRATION_MODE)
            selectedPlotTypeSlot = 15;
        getMenu().getSlot(selectedPlotTypeSlot - 9).setItem(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE, 1).setName(" ").build());


        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for plot type items
        getMenu().getSlot(11).setClickHandler(((clickPlayer, clickInformation) -> {
            builder.setPlotTypeSetting(PlotType.FOCUS_MODE);
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
            reloadMenuAsync();
        }));

        getMenu().getSlot(13).setClickHandler(((clickPlayer, clickInformation) -> {
            builder.setPlotTypeSetting(PlotType.LOCAL_INSPIRATION_MODE);
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
            reloadMenuAsync();
        }));

        getMenu().getSlot(15).setClickHandler(((clickPlayer, clickInformation) -> {
            builder.setPlotTypeSetting(PlotType.CITY_INSPIRATION_MODE);
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
            reloadMenuAsync();
        }));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }
}
