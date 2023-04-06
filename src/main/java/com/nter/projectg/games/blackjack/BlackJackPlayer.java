package com.nter.projectg.games.blackjack;

import com.nter.projectg.games.common.Player;
import com.nter.projectg.model.blackjack.BlackJackMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;

import java.util.function.Consumer;

public class BlackJackPlayer extends Player<BlackJackMessage> {

    public BlackJackPlayer(String name, Consumer<BlackJackMessage> send) {
        super(name, send);
    }
}
