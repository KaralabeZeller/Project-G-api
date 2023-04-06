package com.nter.projectg.games.blackjack;

import com.nter.projectg.games.common.cards.CardDeck;

import java.util.HashMap;
import java.util.List;

// TODO refactor to avoid array indexing
public class Assets {

    private CardDeck cardDeck;

    public final HashMap<Integer, Integer> playerMap;
    public final List<BlackJackPlayer> players;

    public Assets(List<BlackJackPlayer> players) {

        this.players = players;
        playerMap = new HashMap<>();
        setCardDeck(new CardDeck());

        initPlayers();
    }

    private void initPlayers() {
        for (int i = 0; i < players.size(); i++) {
            playerMap.put(i, 1);
        }
    }

    public CardDeck getCardDeck() {
        return cardDeck;
    }

    public void setCardDeck(CardDeck cardDeck) {
        this.cardDeck = cardDeck;
    }
}
