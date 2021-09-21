/*
 * authplugin
 * Copyright © 2021 SolarMC Developers
 *
 * authplugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * authplugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with authplugin. If not, see <https://www.gnu.org/licenses/>
 * and navigate to version 3 of the GNU Affero General Public License.
 */

package gg.solarmc.authplugin.config;

import net.kyori.adventure.text.Component;
import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfHeader;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.SubSection;

public interface Config {

    @SubSection
    Logons logons();

    @ConfHeader("Login-related messages")
    interface Logons {

        @ConfKey("denied-premium-took-name")
        @ConfComments("Message sent to cracked players who attempt to join with the same name as an existing premium player")
        @ConfDefault.DefaultString("&cThis name is taken by another user. &7Please change your name and rejoin.")
        Component deniedPremiumTookName();

        @ConfKey("denied-case-sensitivity-of-name")
        @ConfComments({"Message sent to cracked players who attempt to join with the same name",
                "as an existing cracked player except in casing. This scenario is disallowed",
                "because there would exist two players with different UUIDs but the same name",
                "ignoring case, which is problematic both for our own software and plenty of plugins."})
        @ConfDefault.DefaultString("&cYou may not join with the same name, differing only in case, as an existing user. &7Please change your name and rejoin.")
        Component deniedCaseSensitivityOfName();

        @ConfComments({
                "The current Mojang API rate limit is 600 requests in 10 minutes, or 1 request per second.",
                "If we are rate-limited by the Mojang API, should we assume players are premium or cracked?",
                "True to assume premium, false to assume cracked.",
                "Note that during rate-limiting, there will also be messages logged to console."})
        @ConfDefault.DefaultBoolean(true)
        boolean assumePremiumIfRateLimited();
    }

    @SubSection
    Limbo limbo();

    @ConfHeader({
            "Limbo describes the state of players not yet logged in",
            "At least the players waiting here, mostly cracked, can speak to the philosophers (Inferno)"
    })
    interface Limbo {

        @ConfKey("server-while-in-limbo")
        @ConfComments({
                "The name of the backend server to place players in while they are in limbo",
                "A player in limbo cannot connect to any other backend servers than this one"
        })
        @ConfDefault.DefaultString("unset")
        String serverWhileInLimbo();

        @ConfKey("server-when-released-from-limbo")
        @ConfComments("The name of the heavenly or hellish backend server to move players to when released from limbo")
        @ConfDefault.DefaultString("unset")
        String serverWhenReleasedFromLimbo();

        @ConfKey("ready-to-play")
        @ConfDefault.DefaultString("&aYou are now ready to play!")
        Component readyToPlay();

        @ConfKey("unable-to-connect-to-release-server")
        @ConfDefault.DefaultString("&cWe were unable to connect you to the server where you will be free from limbo.")
        Component unableToConnectToReleaseServer();
    }

    @ConfKey("command-not-at-this-time")
    @ConfComments("Players can only use /login or /register when they indeed ought to login or register.")
    @ConfDefault.DefaultString("&cYou may not use this command at this time.")
    Component commandNotAtThisTime();

    @ConfKey("account-creation")
    @SubSection
    AccountCreation accountCreation();

    interface AccountCreation {

        @ConfKey("should-create")
        @ConfComments("Chat message to tell the player to create an account")
        @ConfDefault.DefaultString("&7Please create an account with /register <password>")
        Component shouldCreate();

        @ConfComments("Chat message when the account was successfully created")
        @ConfDefault.DefaultString("&7You successfully created an account. Make sure to remember the password.")
        Component created();

        @ConfComments("The kick message sent when a player tries to create an account with a taken name")
        @ConfDefault.DefaultString("&cSince you joined, someone else has created an account with that name. Please rejoin and try again.")
        Component conflicting();
    }

    @ConfKey("account-login")
    @SubSection
    AccountLogin accountLogin();

    interface AccountLogin {

        @ConfKey("should-login")
        @ConfComments("Chat message to tell the player to log in")
        @ConfDefault.DefaultString("&7Please login to your account with /login <password>. If you have never " +
                "joined SolarMC before, please re-join with a new name, because this name is taken.")
        Component shouldLogin();

        @ConfKey("incorrect-password")
        @ConfDefault.DefaultString("&cThe password you specified is incorrect")
        Component incorrectPassword();

        @ConfKey("correct-password")
        @ConfDefault.DefaultString("&7Correct password entered")
        Component correctPassword();

        @ConfComments("Chat message sent when a player migrates from a cracked to premium account.")
        @ConfDefault.DefaultString("Congratulations, your account is now migrated to premium. " +
                "From now on, you should use this account to log in.")
        Component migrated();

        @ConfKey("user-id-missing")
        @ConfComments({"If a joins as cracked on one proxy, and their account is migrated, their user ID will be missing.",
                "They will be kicked with the following message when that happens."})
        @ConfDefault.DefaultString("&cYour user ID is missing. &7Most likely this indicates you migrated your account to premium. Use your premium account instead.")
        Component userIdMissing();

    }

}
