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

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReviewMenu extends AbstractPaginatedMenu {
    private final List<Country> countries = Builder.byUUID(getMenuPlayer().getUniqueId()).getAsReviewer().getCountries();
    private Country filteredCountry = null;

    public ReviewMenu(Player player) throws SQLException {
        super(6, 4, LangUtil.getInstance().get(player, LangPaths.Review.MANAGE_AND_REVIEW_PLOTS), player);
    }

    @Override
    protected List<?> getSource() {
        List<Plot> plots = new ArrayList<>();
        try {
            plots.addAll(PlotManager.getPlots(countries, Status.unreviewed));
            plots.addAll(PlotManager.getPlots(countries, Status.unfinished));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        return plots;
    }

    @Override
    protected void setPreviewItems() {
        // Set close item
        getMenu().getSlot(49).setItem(MenuItems.closeMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        // Set unreviewed and unfinished plot items
        List<Plot> plots = getFilteredPlots(source);
        for(int i = 0; i < plots.size(); i++) {
            try {
                Plot plot = plots.get(i);
                List<String> lines = new ArrayList<>();
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.ID) + ": §f" + plot.getID());
                lines.add("");
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": §f" + plot.getPlotOwner().getName());
                if (!plot.getPlotMembers().isEmpty()) lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.MEMBERS) + ": §f" + plot.getPlotMembers().stream().map(m -> {
                            try { return m.getName(); } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex); }
                            return "";
                        }).collect(Collectors.joining(", "))
                );
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.CITY) + ": §f" + plot.getCity().getName());
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COUNTRY) + ": §f" + plot.getCity().getCountry().getName());
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase());

                getMenu().getSlot(i + 9).setItem(new ItemBuilder(plot.getStatus() == Status.unfinished ? Material.MAP : Material.FILLED_MAP, 1)
                        .setName("§b§l" + LangUtil.getInstance().get(getMenuPlayer(), plot.getStatus() == Status.unfinished ? LangPaths.Review.MANAGE_PLOT : LangPaths.Review.REVIEW_PLOT))
                        .setLore(lines)
                        .build());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                getMenu().getSlot(i).setItem(MenuItems.errorItem(getMenuPlayer()));
            }
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        // Set click event for unreviewed and unfinished plot items
        List<Plot> plots = getFilteredPlots(source);
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            getMenu().getSlot(i + 9).setClickHandler((player, info) -> {
                try {
                    if (plot.getStatus() == Status.unreviewed) {
                        if (!plot.getPlotOwner().getUUID().toString().equals(getMenuPlayer().getUniqueId().toString()) || PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                            Plot currentPlot = PlotManager.getCurrentPlot(Builder.byUUID(getMenuPlayer().getUniqueId()), Status.unreviewed);
                            if (currentPlot != null && currentPlot.getID() == plot.getID()) {
                                new ReviewPlotMenu(getMenuPlayer(), currentPlot);
                            } else plot.getWorld().teleportPlayer(getMenuPlayer());
                        } else {
                            getMenuPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
                        }
                    } else {
                        new PlotActionsMenu(getMenuPlayer(), plot);
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                    getMenuPlayer().closeInventory();
                }
            });
        }
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set filter item
        getMenu().getSlot(7).setItem(getFilterItem(getMenuPlayer()));

        // Set previous page item
        if (hasPreviousPage()) getMenu().getSlot(46).setItem(MenuItems.previousPageItem(getMenuPlayer()));

        // Set next page item
        if (hasNextPage()) getMenu().getSlot(52).setItem(MenuItems.nextPageItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for filter item
        getMenu().getSlot(7).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            if (countries.size() == 0) return;

            if (filteredCountry == null) {
                filteredCountry = countries.get(0);
            } else {
                int index = countries.indexOf(filteredCountry);
                filteredCountry = index + 1 >= countries.size() ? null : countries.get(index + 1);
            }

            reloadMenuAsync(false);
        });

        // Set click event for previous page item
        getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });

        // Set click event for close item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page item
        getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasNextPage()) {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    private List<Plot> getFilteredPlots(List<?> plots) {
        List<Plot> filteredPlots = plots.stream().map(p -> (Plot) p).collect(Collectors.toList());
        if (filteredCountry != null) filteredPlots = filteredPlots.stream().filter(p -> {
            try { return p.getCity().getCountry().getID() == filteredCountry.getID();
            } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex); }
            return false;
        }).collect(Collectors.toList());
        return filteredPlots;
    }

    private ItemStack getFilterItem(Player langPlayer) {
        LoreBuilder loreBuilder = new LoreBuilder();
        loreBuilder.addLine((filteredCountry == null ? "§b§l> §f§l" : "§7") + LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.FILTER));
        loreBuilder.emptyLine();

        countries.forEach(c -> {
            if (filteredCountry != null && filteredCountry.getID() == c.getID()) {
                loreBuilder.addLine("§b§l> §f§l" + filteredCountry.getName());
            } else loreBuilder.addLine("§7" + c.getName());
        });

        return new ItemBuilder(MenuItems.filterItem(getMenuPlayer()))
                .setLore(loreBuilder.build())
                .setEnchanted(filteredCountry != null)
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.BOOK, 1)
                .setName("§b§l" + LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOTS) + " §7(" + LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")")
                .setEnchanted(true)
                .build();
    }
}
