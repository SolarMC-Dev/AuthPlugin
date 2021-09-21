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

package gg.solarmc.authplugin.auth.handler;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.Command;

import java.util.concurrent.CompletionStage;

/**
 * The main determinant of where a particular player is in the login process. Every
 * player will have a handler associated with their {@code Auth}. <br>
 * <br>
 * When a method is called relating to a certain event, the handler is able to choose
 * whether to switch to another handler, given by the return value. It will return itself
 * if it must remain the handler for the player in question.
 *
 */
public interface NextStageHandler {

    CompletionStage<NextStageHandler> onCommand(Player player, Command command);

    default CompletionStage<NextStageHandler> onLogin(LoginEvent loginEvent) {
        throw new UnsupportedOperationException("For handler " + getClass());
    }

    /**
     * Called after this handler has been firmly set on the player.
     *
     * @param player the player this handler is now attached to
     */
    void onTransition(Player player);

    boolean isAuthenticated();
}
