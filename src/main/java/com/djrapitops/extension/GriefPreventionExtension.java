/*
    Copyright(c) 2019 Risto Lahtela (Rsl1122)

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package com.djrapitops.extension;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * DataExtension for GriefPrevention.
 *
 * @author Rsl1122
 */
@PluginInfo(name = "GriefPrevention", iconName = "shield-alt", iconFamily = Family.SOLID, color = Color.BLUE_GREY)
@TabInfo(
        tab = "Claims",
        iconName = "map-marker",
        elementOrder = {ElementOrder.TABLE}
)
public class GriefPreventionExtension implements DataExtension {

    private DataStore dataStore;

    public GriefPreventionExtension() {
        dataStore = getPlugin(GriefPrevention.class).dataStore;
        if (dataStore == null) {
            throw new IllegalStateException();
        }
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_LEAVE
        };
    }

    @BooleanProvider(
            text = "SoftMuted",
            description = "Are the player's messages muted for others, but shown to them",
            iconName = "bell-slash",
            iconColor = Color.DEEP_ORANGE,
            iconFamily = Family.REGULAR
    )
    public boolean isSoftMuted(UUID playerUUID) {
        return dataStore.isSoftMuted(playerUUID);
    }

    @NumberProvider(
            text = "Claims",
            description = "How many claims the player has",
            iconName = "map-marker",
            iconColor = Color.BLUE_GREY
    )
    public long claimCount(UUID playerUUID) {
        return getClaimsOf(playerUUID).count();
    }

    @NumberProvider(
            text = "Claimed Area",
            description = "How large area the player has claimed",
            iconName = "map",
            iconColor = Color.BLUE_GREY,
            iconFamily = Family.REGULAR
    )
    public long claimedArea(UUID playerUUID) {
        return getClaimsOf(playerUUID)
                .mapToLong(Claim::getArea)
                .sum();
    }

    private Stream<Claim> getClaimsOf(UUID playerUUID) {
        return dataStore.getClaims().stream()
                .filter(Objects::nonNull)
                .filter(claim -> playerUUID.equals(claim.ownerID));
    }

    @TableProvider(tableColor = Color.BLUE_GREY)
    @Tab("Claims")
    public Table claimTable(UUID playerUUID) {
        Table.Factory table = Table.builder()
                .columnOne("Claim", Icon.called("map-marker").build())
                .columnTwo("Area", Icon.called("map").of(Family.REGULAR).build());

        getClaimsOf(playerUUID)
                .sorted((one, two) -> Integer.compare(two.getArea(), one.getArea()))
                .forEach(
                        claim -> table.addRow(formatLocation(claim.getGreaterBoundaryCorner()), claim.getArea())
                );

        return table.build();
    }

    private String formatLocation(Location greaterBoundaryCorner) {
        return "x: " + greaterBoundaryCorner.getBlockX() + " z: " + greaterBoundaryCorner.getBlockZ();
    }

}