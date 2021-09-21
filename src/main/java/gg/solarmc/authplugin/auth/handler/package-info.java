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

/**
 * Authentication is implemented using various handlers which are responsible
 * for dealing with different stages. There exists a handler for every
 * {@link gg.solarmc.authplugin.auth.Auth}. The handler for a particular
 * {@code Auth} may, when an event occurs, do one of two things: <br>
 * <br>
 * 1. Retain itself as the handler. The handler returns itself from the
 * relevant method.
 * 2. Switch the handler to another. This is done by returning a future
 * of the next handler.
 *
 */
package gg.solarmc.authplugin.auth.handler;