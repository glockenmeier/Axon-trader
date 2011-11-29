/*
 * Copyright (c) 2011. Gridshore
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.samples.trader.app.command.trading;

import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.domain.UUIDAggregateIdentifier;
import org.axonframework.samples.trader.app.api.portfolio.CreatePortfolioCommand;
import org.axonframework.samples.trader.app.api.portfolio.PortfolioCreatedEvent;
import org.axonframework.samples.trader.app.api.portfolio.item.*;
import org.axonframework.samples.trader.app.api.portfolio.money.*;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jettro Coenradie
 */
public class PortfolioCommandHandlerTest {
    private FixtureConfiguration fixture;

    @Before
    public void setUp() {
        fixture = Fixtures.newGivenWhenThenFixture();
        PortfolioCommandHandler commandHandler = new PortfolioCommandHandler();
        commandHandler.setRepository(fixture.createGenericRepository(Portfolio.class));
        fixture.registerAnnotatedCommandHandler(commandHandler);
    }

    @Test
    public void testCreatePortfolio() {
        AggregateIdentifier userIdentifier = new UUIDAggregateIdentifier();
        CreatePortfolioCommand command = new CreatePortfolioCommand(userIdentifier);
        fixture.given()
                .when(command)
                .expectEvents(new PortfolioCreatedEvent(userIdentifier));
    }

    /* Items related test methods */
    @Test
    public void testAddItemsToPortfolio() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        AddItemsToPortfolioCommand command = new AddItemsToPortfolioCommand(portfolioIdentifier, orderbookIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()))
                .when(command)
                .expectEvents(new ItemsAddedToPortfolioEvent(orderbookIdentifier, 100));
    }

    @Test
    public void testReserveItems_noItemsAvailable() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()))
                .when(command)
                .expectEvents(new ItemToReserveNotAvailableInPortfolioEvent(orderbookIdentifier));
    }

    @Test
    public void testReserveItems_notEnoughItemsAvailable() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(orderbookIdentifier, 100))
                .when(command)
                .expectEvents(new NotEnoughItemsAvailableToReserveInPortfolio(orderbookIdentifier, 100, 200));
    }

    @Test
    public void testReserveItems() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(orderbookIdentifier, 400))
                .when(command)
                .expectEvents(new ItemsReservedEvent(orderbookIdentifier, 200));
    }

    @Test
    public void testConfirmationOfReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier itemIdentifier = new UUIDAggregateIdentifier();

        ConfirmItemReservationForPortfolioCommand command =
                new ConfirmItemReservationForPortfolioCommand(portfolioIdentifier, itemIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(itemIdentifier, 400),
                new ItemsReservedEvent(itemIdentifier, 100))
                .when(command)
                .expectEvents(new ItemReservationConfirmedForPortfolioEvent(itemIdentifier, 100));
    }

    @Test
    public void testCancellationOfReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier itemIdentifier = new UUIDAggregateIdentifier();

        CancelItemReservationForPortfolioCommand command =
                new CancelItemReservationForPortfolioCommand(portfolioIdentifier, itemIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(itemIdentifier, 400),
                new ItemsReservedEvent(itemIdentifier, 100))
                .when(command)
                .expectEvents(new ItemReservationCancelledForPortfolioEvent(itemIdentifier, 100));
    }

    /* Money related test methods */
    @Test
    public void testDepositingMoneyToThePortfolio() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        DepositMoneyToPortfolioCommand command = new DepositMoneyToPortfolioCommand(portfolioIdentifier, 2000l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()))
                .when(command)
                .expectEvents(new MoneyDepositedToPortfolioEvent(2000l));

    }

    @Test
    public void testWithdrawingMoneyFromPortfolio() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        WithdrawMoneyFromPortfolioCommand command = new WithdrawMoneyFromPortfolioCommand(portfolioIdentifier, 300l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(400))
                .when(command)
                .expectEvents(new MoneyWithdrawnFromPortfolioEvent(300l));
    }

    @Test
    public void testWithdrawingMoneyFromPortfolio_withoutEnoughMoney() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        WithdrawMoneyFromPortfolioCommand command = new WithdrawMoneyFromPortfolioCommand(portfolioIdentifier, 300l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(200))
                .when(command)
                .expectEvents(new MoneyWithdrawnFromPortfolioEvent(300l));
    }

    @Test
    public void testMakingMoneyReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        ReserveMoneyFromPortfolioCommand command = new ReserveMoneyFromPortfolioCommand(portfolioIdentifier, 300l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(400))
                .when(command)
                .expectEvents(new MoneyReservedFromPortfolioEvent(300l));
    }

    @Test
    public void testMakingMoneyReservation_withoutEnoughMoney() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        ReserveMoneyFromPortfolioCommand command = new ReserveMoneyFromPortfolioCommand(portfolioIdentifier, 600l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(400))
                .when(command)
                .expectEvents(new NotEnoughMoneyInPortfolioToMakeReservationEvent(600));
    }

    @Test
    public void testCancelMoneyReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        CancelMoneyReservationFromPortfolioCommand command = new CancelMoneyReservationFromPortfolioCommand(portfolioIdentifier, 200l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(400))
                .when(command)
                .expectEvents(new MoneyReservationCancelledFromPortfolioEvent(200l));
    }

    @Test
    public void testConfirmMoneyReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        ConfirmMoneyReservationFromPortfolionCommand command = new ConfirmMoneyReservationFromPortfolionCommand(portfolioIdentifier, 200l);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()), new MoneyDepositedToPortfolioEvent(400))
                .when(command)
                .expectEvents(new MoneyReservationConfirmedFromPortfolioEvent(200l));
    }
}