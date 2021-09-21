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

package gg.solarmc.authplugin.test.auth.handler;

import com.velocitypowered.api.proxy.Player;
import gg.solarmc.authplugin.auth.Command;
import gg.solarmc.authplugin.auth.DataFulfillment;
import gg.solarmc.authplugin.auth.handler.CreateAccountHandler;
import gg.solarmc.authplugin.auth.handler.DisconnectedHandler;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;
import gg.solarmc.authplugin.auth.handler.ReleasedFromLimboHandler;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.authentication.AuthenticationCenter;
import gg.solarmc.loader.authentication.CreateAccountResult;
import gg.solarmc.loader.authentication.UserWithDataNotYetLoaded;
import gg.solarmc.loader.authentication.VerifiablePassword;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateAccountHandlerTest {

    private final Config config;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final DataFulfillment dataFulfillment;
    private final ReleasedFromLimboHandler releasedFromLimboHandler;
    private final DisconnectedHandler disconnectedHandler;

    private CreateAccountHandler createAccountHandler;

    public CreateAccountHandlerTest(@Mock Config config, @Mock DataCenter dataCenter,
                                    @Mock AuthenticationCenter authCenter, @Mock DataFulfillment dataFulfillment,
                                    @Mock ReleasedFromLimboHandler releasedFromLimboHandler, @Mock DisconnectedHandler disconnectedHandler) {
        this.config = config;
        this.dataCenter = dataCenter;
        this.authCenter = authCenter;
        this.dataFulfillment = dataFulfillment;
        this.releasedFromLimboHandler = releasedFromLimboHandler;
        this.disconnectedHandler = disconnectedHandler;
    }

    @BeforeEach
    public void setCreateAccountHandler() {
        createAccountHandler = new CreateAccountHandler(config, dataCenter, authCenter,
                dataFulfillment, releasedFromLimboHandler, disconnectedHandler);
        Transaction transaction = mock(Transaction.class);
        lenient().when(dataCenter.transact(any())).thenAnswer((invocation) -> {
            DataCenter.TransactionActor<?> actor = invocation.getArgument(0);
            return new IndifferentFactoryOfTheFuture().completedFuture(actor.transactUsing(transaction));
        });
    }

    @Test
    public void createAccount(@Mock Player player) {
        Component createdAccount = Component.text("Created account successfully");
        {
            Config.AccountCreation accountCreation = mock(Config.AccountCreation.class);
            when(config.accountCreation()).thenReturn(accountCreation);
            when(accountCreation.created()).thenReturn(createdAccount);
        }
        VerifiablePassword password = mock(VerifiablePassword.class);
        UserWithDataNotYetLoaded user = mock(UserWithDataNotYetLoaded.class);
        {
            when(authCenter.hashNewPassword("pass")).thenReturn(password);
            when(dataFulfillment.createUser(player)).thenReturn(user);
            when(authCenter.createAccount(any(), eq(user), eq(password))).thenReturn(CreateAccountResult.CREATED);
        }

        NextStageHandler nextHandler = createAccountHandler
                .onCommand(player, new Command(Command.Type.REGISTER, "pass"))
                .toCompletableFuture().join();
        verify(authCenter).createAccount(any(), eq(user), eq(password));
        verify(player).sendMessage(createdAccount);
        assertEquals(releasedFromLimboHandler, nextHandler);
    }

    @Test
    public void conflictingAccount(@Mock Player player) {
        Component conflictingAccount = Component.text("Conflicting account");
        {
            Config.AccountCreation accountCreation = mock(Config.AccountCreation.class);
            when(config.accountCreation()).thenReturn(accountCreation);
            when(accountCreation.conflicting()).thenReturn(conflictingAccount);
        }
        VerifiablePassword password = mock(VerifiablePassword.class);
        UserWithDataNotYetLoaded user = mock(UserWithDataNotYetLoaded.class);
        {
            when(authCenter.hashNewPassword("pass")).thenReturn(password);
            when(dataFulfillment.createUser(player)).thenReturn(user);
            when(authCenter.createAccount(any(), eq(user), eq(password))).thenReturn(CreateAccountResult.CONFLICT);
        }

        NextStageHandler nextHandler = createAccountHandler
                .onCommand(player, new Command(Command.Type.REGISTER, "pass"))
                .toCompletableFuture().join();
        verify(authCenter).createAccount(notNull(), eq(user), eq(password));
        verify(player).disconnect(conflictingAccount);
        assertEquals(disconnectedHandler, nextHandler);
    }
}
