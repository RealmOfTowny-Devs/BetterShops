package max.hubbard.bettershops.Utils;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import max.hubbard.bettershops.Configurations.Config;
import max.hubbard.bettershops.Core;
import max.hubbard.bettershops.Events.ShopDeleteEvent;
import max.hubbard.bettershops.ShopManager;
import max.hubbard.bettershops.Shops.FileShop;
import max.hubbard.bettershops.Shops.Items.ShopItem;
import max.hubbard.bettershops.Shops.SQLShop;
import max.hubbard.bettershops.Shops.Shop;
import max.hubbard.bettershops.Shops.Types.Holo.DeleteHoloShop;
import max.hubbard.bettershops.Shops.Types.Holo.HologramManager;
import max.hubbard.bettershops.Shops.Types.NPC.DeleteNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.sql.SQLException;
import java.util.List;

/**
 * ***********************************************************************
 * Copyright Max Hubbard (c) 2015. All Rights Reserved.
 * Any code contained within this document, and any associated documents with similar branding
 * are the sole property of Max. Distribution, reproduction, taking snippets, or
 * claiming any contents as your own will break the terms of the license, and void any
 * agreements with you, the third party.
 * ************************************************************************
 */
public class ShopDeleter {

    public static void deleteShopExternally(final Shop shop) {
        ShopDeleteEvent ev = new ShopDeleteEvent(shop);
        Bukkit.getPluginManager().callEvent(ev);

        for (ShopItem item : shop.getShopItems()) {
            Stocks.throwItemsOnGroundInThread(item);
        }

        if ((boolean) Config.getObject("UseLWC") && Core.useLWC()) {
            Location chest = shop.getLocation();
            Protection existingChestProtection = LWC.getInstance().getPhysicalDatabase().loadProtection(chest.getWorld().getName(), chest.getBlockX(), chest.getBlockY(), chest.getBlockZ());
            if (existingChestProtection != null) {
                existingChestProtection.remove();

            }
            if (shop.getSign() != null) {
                Block sign = shop.getSign().getBlock();
                Protection existingSignProtection = LWC.getInstance().getPhysicalDatabase().loadProtection(sign.getWorld().getName(), sign.getX(), sign.getY(), sign.getZ());
                if (existingSignProtection != null) {
                    existingSignProtection.remove();
                }
            }
        }
        if (shop instanceof FileShop) {
            if (shop.useIcon()) {
                HologramManager.removeIcon(shop);
                shop.setObject("Icon", -1);
            }
            ((FileShop) shop).file.delete();
        } else {
            if (shop.useIcon()) {
                HologramManager.removeIcon(shop);
                shop.setObject("Icon", -1);
            }

            try {
                ((SQLShop) shop).statement.executeUpdate("DELETE FROM " + Config.getObject("prefix") + "Shops WHERE Name = '" + shop.getName() + "';");
                ((SQLShop) shop).statement.executeUpdate("DELETE FROM " + Config.getObject("prefix") + "Trades WHERE Shop = '" + shop.getName() + "';");
                ((SQLShop) shop).statement.executeUpdate("DELETE FROM " + Config.getObject("prefix") + "Keepers WHERE Shop = '" + shop.getName() + "';");
                ((SQLShop) shop).statement.executeUpdate("DELETE FROM " + Config.getObject("prefix") + "Items WHERE Shop = '" + shop.getName() + "';");
                ((SQLShop) shop).statement.executeUpdate("DELETE FROM " + Config.getObject("prefix") + "Blacklist WHERE Shop = '" + shop.getName() + "';");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (shop.isHoloShop()) {
            DeleteHoloShop.deleteHologramShop(shop.getHolographicShop());
        }

        if (shop.isNPCShop()) {
            DeleteNPC.deleteNPC(shop.getNPCShop());
        }


        ShopManager.locs.remove(shop.getLocation());
        ShopManager.names.remove(shop.getName());
        ShopManager.shops.remove(shop);

        ShopManager.signLocs.values().remove(shop);

        if (shop.getOwner() != null) {
            int amt = ShopManager.getLimits().get(shop.getOwner().getUniqueId());
            ShopManager.getLimits().put(shop.getOwner().getUniqueId(), amt - 1);

            List<Shop> li = ShopManager.getShopsForPlayer(shop.getOwner());
            li.remove(shop);
            ShopManager.playerShops.put(shop.getOwner().getUniqueId(), li);
        }

        ShopManager.loadingTotal = ShopManager.getShops().size();

        if (Core.getMetrics() != null) {
            Core.getCore().setUpMetrics();
        }
    }
}
