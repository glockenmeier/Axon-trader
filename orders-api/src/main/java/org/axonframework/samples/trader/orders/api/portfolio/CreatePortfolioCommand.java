/*
 * Copyright (c) 2010-2012. Axon Framework
 *
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

package org.axonframework.samples.trader.orders.api.portfolio;

import org.axonframework.samples.trader.tradeengine.api.order.PortfolioId;
import org.axonframework.samples.trader.users.api.UserId;

/**
 * @author Jettro Coenradie
 */
public class CreatePortfolioCommand {

    private PortfolioId portfolioId;
    private UserId userId;

    public CreatePortfolioCommand(PortfolioId portfolioId, UserId userId) {
        this.portfolioId = portfolioId;
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }

    public PortfolioId getPortfolioId() {
        return portfolioId;
    }
}
