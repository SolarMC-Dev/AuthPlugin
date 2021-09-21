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
import gg.solarmc.authplugin.auth.handler.DisconnectedHandler;
import gg.solarmc.authplugin.auth.handler.EnterPasswordHandlerFactory;
import gg.solarmc.authplugin.auth.handler.NextStageHandler;
import gg.solarmc.authplugin.auth.handler.ReleasedFromLimboHandler;
import gg.solarmc.authplugin.config.Config;
import gg.solarmc.loader.DataCenter;
import gg.solarmc.loader.Transaction;
import gg.solarmc.loader.authentication.AuthenticationCenter;
import gg.solarmc.loader.authentication.HashingInstructions;
import gg.solarmc.loader.authentication.PasswordSalt;
import gg.solarmc.loader.authentication.UserWithDataNotYetLoaded;
import gg.solarmc.loader.authentication.VerifiablePassword;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import space.arim.omnibus.util.concurrent.impl.IndifferentFactoryOfTheFuture;

import static gg.solarmc.loader.authentication.CompleteLoginResult.MIGRATED_TO_PREMIUM;
import static gg.solarmc.loader.authentication.CompleteLoginResult.NORMAL;
import static gg.solarmc.loader.authentication.CompleteLoginResult.USER_ID_MISSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnterPasswordHandlerTest {

    private final Config.AccountLogin accountLoginConf;
    private final DataCenter dataCenter;
    private final AuthenticationCenter authCenter;
    private final DataFulfillment dataFulfillment;
    private final ReleasedFromLimboHandler releasedFromLimboHandler;
    private final DisconnectedHandler disconnectedHandler;

    private EnterPasswordHandlerFactory enterPasswordHandlerFactory;

    public EnterPasswordHandlerTest(@Mock Config.AccountLogin accountLoginConf,
                                    @Mock DataCenter dataCenter,
                                    @Mock AuthenticationCenter authCenter,
                                    @Mock DataFulfillment dataFulfillment,
                                    @Mock ReleasedFromLimboHandler releasedFromLimboHandler,
                                    @Mock DisconnectedHandler disconnectedHandler) {
        this.accountLoginConf = accountLoginConf;
        this.dataCenter = dataCenter;
        this.authCenter = authCenter;
        this.dataFulfillment = dataFulfillment;
        this.releasedFromLimboHandler = releasedFromLimboHandler;
        this.disconnectedHandler = disconnectedHandler;
    }

    @BeforeEach
    public void setEnterPasswordHandlerFactory(@Mock Config config) {
        enterPasswordHandlerFactory = new EnterPasswordHandlerFactory(config, dataCenter, authCenter,
                dataFulfillment, releasedFromLimboHandler, disconnectedHandler);
        when(config.accountLogin()).thenReturn(accountLoginConf);
        Transaction transaction = mock(Transaction.class);
        lenient().when(dataCenter.transact(any())).thenAnswer((invocation) -> {
            DataCenter.TransactionActor<?> actor = invocation.getArgument(0);
            return new IndifferentFactoryOfTheFuture().completedFuture(actor.transactUsing(transaction));
        });
    }

    @Test
    public void correctPasswordNormal(@Mock Player player) {
        Component correctPasswordMessage = Component.text("Correct password entered");
        when(accountLoginConf.correctPassword()).thenReturn(correctPasswordMessage);

        VerifiablePassword requiredPassword = mock(VerifiablePassword.class);
        {
            PasswordSalt salt = mock(PasswordSalt.class);
            HashingInstructions instructions = new HashingInstructions(0, 0);
            when(requiredPassword.passwordSalt()).thenReturn(salt);
            when(requiredPassword.instructions()).thenReturn(instructions);

            VerifiablePassword enteredPassword = mock(VerifiablePassword.class);
            when(authCenter.hashPassword("pass", salt, instructions)).thenReturn(enteredPassword);
            lenient().when(requiredPassword.matches(enteredPassword)).thenReturn(true);
            lenient().when(enteredPassword.matches(requiredPassword)).thenReturn(true);
        }
        UserWithDataNotYetLoaded user = mock(UserWithDataNotYetLoaded.class);
        {
            when(dataFulfillment.createUser(player)).thenReturn(user);
            when(authCenter.completeLoginAndPossiblyMigrate(any(), eq(user))).thenReturn(NORMAL);
        }

        NextStageHandler enterPasswordHandler = enterPasswordHandlerFactory.createHandler(requiredPassword);
        NextStageHandler nextHandler = enterPasswordHandler
                .onCommand(player, new Command(Command.Type.LOGIN, "pass"))
                .toCompletableFuture().join();
        verify(authCenter).completeLoginAndPossiblyMigrate(notNull(), eq(user));
        verify(player).sendMessage(correctPasswordMessage);
        assertEquals(releasedFromLimboHandler, nextHandler, "Player should be released from limbo");
    }

    @Test
    public void correctPasswordMigrate(@Mock Player player) {
        Component migratedMessage = Component.text("Your account is now migrated");
        Component correctPasswordMessage = Component.text("Correct password entered");
        when(accountLoginConf.migrated()).thenReturn(migratedMessage);
        when(accountLoginConf.correctPassword()).thenReturn(correctPasswordMessage);

        VerifiablePassword requiredPassword = mock(VerifiablePassword.class);
        {
            PasswordSalt salt = mock(PasswordSalt.class);
            HashingInstructions instructions = new HashingInstructions(0, 0);
            when(requiredPassword.passwordSalt()).thenReturn(salt);
            when(requiredPassword.instructions()).thenReturn(instructions);

            VerifiablePassword enteredPassword = mock(VerifiablePassword.class);
            when(authCenter.hashPassword("pass", salt, instructions)).thenReturn(enteredPassword);
            lenient().when(requiredPassword.matches(enteredPassword)).thenReturn(true);
            lenient().when(enteredPassword.matches(requiredPassword)).thenReturn(true);
        }
        UserWithDataNotYetLoaded user = mock(UserWithDataNotYetLoaded.class);
        {
            when(dataFulfillment.createUser(player)).thenReturn(user);
            when(authCenter.completeLoginAndPossiblyMigrate(any(), eq(user))).thenReturn(MIGRATED_TO_PREMIUM);
        }

        NextStageHandler enterPasswordHandler = enterPasswordHandlerFactory.createHandler(requiredPassword);
        NextStageHandler nextHandler = enterPasswordHandler
                .onCommand(player, new Command(Command.Type.LOGIN, "pass"))
                .toCompletableFuture().join();
        verify(authCenter).completeLoginAndPossiblyMigrate(notNull(), eq(user));
        verify(player).sendMessage(migratedMessage);
        verify(player).sendMessage(correctPasswordMessage);
        assertEquals(releasedFromLimboHandler, nextHandler, "Player should be released from limbo");
    }

    @Test
    public void incorrectPassword(@Mock Player player) {
        Component incorrectPasswordMessage = Component.text("Incorrect password entered");
        when(accountLoginConf.incorrectPassword()).thenReturn(incorrectPasswordMessage);

        VerifiablePassword requiredPassword = mock(VerifiablePassword.class);
        {
            PasswordSalt salt = mock(PasswordSalt.class);
            HashingInstructions instructions = new HashingInstructions(0, 0);
            when(requiredPassword.passwordSalt()).thenReturn(salt);
            when(requiredPassword.instructions()).thenReturn(instructions);

            VerifiablePassword enteredPassword = mock(VerifiablePassword.class);
            when(authCenter.hashPassword("wrong pass", salt, instructions)).thenReturn(enteredPassword);
            lenient().when(requiredPassword.matches(enteredPassword)).thenReturn(false);
            lenient().when(enteredPassword.matches(requiredPassword)).thenReturn(false);
        }

        NextStageHandler enterPasswordHandler = enterPasswordHandlerFactory.createHandler(requiredPassword);
        NextStageHandler nextHandler = enterPasswordHandler
                .onCommand(player, new Command(Command.Type.LOGIN, "wrong pass"))
                .toCompletableFuture().join();
        verifyNoMoreInteractions(dataFulfillment);
        verify(player).sendMessage(incorrectPasswordMessage);
        assertEquals(enterPasswordHandler, nextHandler, "Handler should stay the same");
    }

    @Test
    public void userIdMissing(@Mock Player player) {
        Component userIdMissingMessage = Component.text("User id is missing");
        when(accountLoginConf.userIdMissing()).thenReturn(userIdMissingMessage);

        VerifiablePassword requiredPassword = mock(VerifiablePassword.class);
        {
            PasswordSalt salt = mock(PasswordSalt.class);
            HashingInstructions instructions = new HashingInstructions(0, 0);
            when(requiredPassword.passwordSalt()).thenReturn(salt);
            when(requiredPassword.instructions()).thenReturn(instructions);

            VerifiablePassword enteredPassword = mock(VerifiablePassword.class);
            when(authCenter.hashPassword("pass", salt, instructions)).thenReturn(enteredPassword);
            lenient().when(requiredPassword.matches(enteredPassword)).thenReturn(true);
            lenient().when(enteredPassword.matches(requiredPassword)).thenReturn(true);
        }
        UserWithDataNotYetLoaded user = mock(UserWithDataNotYetLoaded.class);
        {
            when(dataFulfillment.createUser(player)).thenReturn(user);
            when(authCenter.completeLoginAndPossiblyMigrate(any(), eq(user))).thenReturn(USER_ID_MISSING);
        }

        NextStageHandler enterPasswordHandler = enterPasswordHandlerFactory.createHandler(requiredPassword);
        NextStageHandler nextHandler = enterPasswordHandler
                .onCommand(player, new Command(Command.Type.LOGIN, "pass"))
                .toCompletableFuture().join();
        verify(authCenter).completeLoginAndPossiblyMigrate(notNull(), eq(user));
        verify(player).disconnect(userIdMissingMessage);
        assertEquals(disconnectedHandler, nextHandler, "Player should be disconnected as their user ID is missing");
    }
}
